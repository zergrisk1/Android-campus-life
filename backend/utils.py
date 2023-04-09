import scrypt, base64, configparser, random, json
import re
from datetime import datetime, timedelta
from . import db
from .models import *
from fuzzywuzzy import process
import random, string, jwt, requests, json, uuid


def encrypt_password(pw):
    config = configparser.RawConfigParser()
    config.read('config.cfg')
    db_dict = dict(config.items('DATABASE'))
    salt = db_dict["salt"]
    key = scrypt.hash(pw, salt, 32768, 8, 1, 32)
    return base64.b64encode(key).decode("ascii")


def check_username(username):
    return (len(username) >= 6) and (len(username) <= 10)


def check_password(password):
    if (re.match("^(?![0-9]+$)(?![A-Z]+$)(?![a-z]+$)(?![a-zA-Z]+$)(?![0-9A-Z]+$)(?![0-9a-z]+$)[0-9A-Za-z]{6,18}$",
                 password) is None):
        return False
    else:
        return True


def check_len255(text):
    return (len(text) >= 0) and (len(text) <= 255)


def checkregister(content):
    # Username (Length between 6-10)
    if not check_username(content['username']):
        return "username", False

    # Password (Length between 6-18, containing lower-case, upper-case and numbers)
    if not check_password(content['password']):
        return "password", False

    # Email
    if re.match(
            "[a-zA-Z0-9]{1,63}@(([a-zA-Z0-9]+[a-zA-Z0-9-]*[a-zA-Z0-9]+\.)|([a-zA-Z0-9]*\.)|(\.))*(([a-zA-Z]+[a-zA-Z-]*[a-zA-Z]+)|([a-zA-Z]+)|([a-zA-Z0-9]+([a-zA-Z0-9-]*[a-zA-Z-]+[a-zA-Z0-9-]*)+[a-zA-Z0-9]+)|())$",
            content['email']) is None:
        return "email", False

    place = int((content['email']).find('@'))
    if len(content['email']) - place > 63:
        return "email", False

    return "ok", True


def check_valid(username, description):
    # Username (Length between 6-10)
    if not check_username(username):
        return "username", False

    # Description (Length between 0-255):
    if not check_len255(description):
        return "description", False

    return "ok", True


def fuzzysearch(key, options, threshold=75):
    ratios = process.extract(key, options)
    selected = []
    for i in ratios:
        if i[1] > threshold:
            selected.append(i[0])
    return selected


def sendNotification(title, message, user_id):
    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        return False
    else:
        config = configparser.RawConfigParser()
        config.read('config.cfg')
        db_dict = dict(config.items('NOTIFICATIONS'))
        server_token = db_dict["server_token"]
        client_token = user.token
        if client_token is None:
            return False
        else:
            headers = {
                'Content-Type': 'application/json',
                'Authorization': 'key=' + server_token,
            }
            body = {
                'notification': {'title': title, 'body': message},
                'to': client_token,
                'priority': 'high',
            }
            response = requests.post("https://fcm.googleapis.com/fcm/send", headers=headers, data=json.dumps(body))
            if response.status_code == 200:
                return True
            else:
                return False


def notifyFollowers(user_id, status_id):
    title = '新动态'
    message = '你关注的作者更新了'
    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        return False
    else:
        for followers in user.followers:
            sendNotification(title, message, followers.user_id)
            type = 'NEW_STATUS'
            add_notifications(followers.user_id, type, title, message, status_id)
        return True


def add_notifications(user_id, type, title, text, status_id):
    user = User.query.filter_by(user_id=user_id).first()
    status = Status.query.filter_by(id=status_id).first()
    if not user:
        return False
    elif not status:
        return False
    else:
        notifications = Notifications()
        notifications_id = str(uuid.uuid4())
        notifications.id = notifications_id
        notifications.user_id = user_id
        notifications.type = type
        notifications.title = title
        notifications.text = text
        notifications.status_id = status_id
        notifications.date_created = datetime.now()

        rel = RelationUserNotifications()
        rel.user_id = user_id
        rel.notifications_id = notifications_id

        db.session.add(notifications)
        db.session.add(rel)
        try:
            db.session.commit()
            return True
        except Exception as e:
            print(e)
            return False

