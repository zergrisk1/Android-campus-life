import pathlib

from flask import Blueprint, jsonify, request, make_response
from . import db
from .models import *
import uuid, os
from .utils import *

users = Blueprint('users', __name__)


@users.route('/success', methods=['GET'])
def login_success():
    return jsonify('Success!')


@users.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        post_data = request.get_json()
        email = post_data.get('email')
        password = post_data.get('password')

    response_object = {}
    response_object['status'] = False

    pw = password
    pw = encrypt_password(str(password))
    user = User.query.filter_by(email=email, password=pw).first()
    # If user exists
    if user:
        response_object["status"] = True
        response_object["message"] = "Login success!"
        response_object["user_id"] = user.user_id
    else:
        response_object["message"] = "Incorrect email/password!"
    return jsonify(response_object)


@users.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        post_data = request.get_json()
        username = post_data.get('username')
        email = post_data.get('email')
        password = post_data.get('password')

    # Check if valid
    content = {}
    content['username'] = username
    content['email'] = email
    content['password'] = password

    response_object = {}
    message, status = checkregister(content)
    response_object["status"] = status
    response_object["message"] = message
    if status is not True:
        return jsonify(response_object)

    # Check if already exist
    pw = encrypt_password(password)
    user1 = User.query.filter_by(email=email).first()
    if user1:
        print("Email occupied!")
        response_object["status"] = False
        response_object["message"] = "Email occupied!"
    else:
        new_user = User(user_id=str(uuid.uuid4()), username=username, email=email, password=pw)
        try:
            db.session.add(new_user)
            db.session.commit()
            print("Registered!")
            response_object["message"] = "Registered!"
            response_object["user_id"] = new_user.user_id
        except Exception as e:
            print(e)
            response_object["status"] = False
            response_object["message"] = "Registered failed"
    return jsonify(response_object)


@users.route('/save-userinfo', methods=['GET', 'POST'])
def save_userinfo():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        username = post_data.get('username')
        description = post_data.get('description')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = 'Error: User does not exist!'
    else:
        msg, status = check_valid(username, description)
        if not status:
            response_object['message'] = msg
        else:
            user.username = username
            status_list = Status.query.filter_by(user_id=user_id)
            for status in status_list:
                status.username = username
            user.description = description
            try:
                db.session.commit()
                response_object['status'] = True
                response_object['message'] = "Info saved!"
            except Exception as e:
                print(e)
                response_object['message'] = "Failed to save!"
    return jsonify(response_object)


@users.route('/change-password', methods=['GET', 'POST'])
def change_password():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        old_password = post_data.get('old_password')
        new_password = post_data.get('new_password')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does ot exist!"
    else:
        old_pw = encrypt_password(old_password)
        if old_pw != user.password:
            response_object['message'] = "Password incorrect!"
        else:
            if not check_password(new_password):
                response_object['message'] = "password"
            else:
                user.password = encrypt_password(new_password)
                try:
                    db.session.commit()
                    response_object['status'] = True
                    response_object['message'] = "Password changed successfully!"
                except Exception as e:
                    response_object['message'] = "Failed to change password"
    return jsonify(response_object)


@users.route('/change-profile-pic', methods=['GET', 'POST'])
def change_profile_pic():
    if request.method == 'POST':
        image = request.files['image']
        user_id = request.form.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['messeage'] = "Error: User does not exist!"
    else:
        config = configparser.RawConfigParser()
        config.read('config.cfg')
        upload_dict = dict(config.items('UPLOAD'))
        upload_folder = upload_dict["upload_folder"]
        dir = os.path.join(upload_folder, "profile_pic")
        if not os.path.exists(dir):
            os.makedirs(dir)
        if image:
            file_ext = pathlib.Path(image.filename).suffix
            filename = uuid.uuid4().hex + file_ext
            path = os.path.join(dir, filename)
            image.save(path)
            if user.profile_photo:
                old_image = os.path.join(dir, user.profile_photo)
                if os.path.isfile(old_image):
                    os.remove(old_image)
            user.profile_photo = filename
            try:
                db.session.commit()
                response_object['status'] = True
                response_object['message'] = "Profile pic uploaded successfully"
                response_object['profile_pic'] = filename
            except Exception as e:
                print(e)
                response_object['message'] = "Failed to upload profile pic"
        else:
            response_object['message'] = "Error: No image!"
    return jsonify(response_object)


@users.route('/query-userinfo', methods=['GET', 'POST'])
def query_userinfo():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check if user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        response_object["status"] = True
        response_object["message"] = "Query success!"
        response_object["username"] = user.username
        response_object["email"] = user.email
        response_object["description"] = user.description
        response_object["profile_photo"] = user.profile_photo
    return jsonify(response_object)


@users.route('/profile-pic/<string:filename>', methods=['GET'])
def show_profile_pic(filename):
    if request.method == 'GET':
        response_object = {}
        response_object['status'] = False
        if not filename:
            response_object['message'] = "Error: Too few arguments!"
            return jsonify(response_object)
        else:
            config = configparser.RawConfigParser()
            config.read('config.cfg')
            upload_dict = dict(config.items('UPLOAD'))
            upload_folder = upload_dict["upload_folder"]
            dir = os.path.join(upload_folder, "profile_pic")
            image = os.path.join(dir, filename)
            if not os.path.isfile(image):
                response_object['message'] = "Error: File does not exist!"
                return jsonify(response_object)
            else:
                image_data = open(image, "rb").read()
                response = make_response(image_data)
                response.headers['Content-Type'] = "image/png"
                return response


@users.route('/follow-unfollow', methods=['GET', 'POST'])
def follow_unfollow():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        user_id_followed = post_data.get('user_id_followed')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    user_followed = User.query.filter_by(user_id=user_id_followed).first()
    # Check whether users exist
    if not user or not user_followed:
        response_object['message'] = "Error: User does not exist!"
    else:
        # Check whether followed
        following_list = user.following
        if user_followed not in user.following:
            following_list.append(user_followed)
        else:
            following_list.remove(user_followed)

        user.following = following_list
        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Followed/Unfollowed successfully!"
        except Exception as e:
            print(e)
            response_object['message'] = "Failed to unfollow!"
    return jsonify(response_object)


@users.route('/query-following', methods=['GET', 'POST'])
def query_following():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        following_list = []
        for following in user.following:
            info = {}
            info['user_id'] = following.user_id
            info['username'] = following.username
            following_list.append(info)
        response_object['status'] = True
        response_object['message'] = "Query success!"
        response_object['following'] = following_list
    return jsonify(response_object)


@users.route('/check-follow', methods=['GET', 'POST'])
def check_follow():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        user_id_followed = post_data.get('user_id_followed')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    user_followed = User.query.filter_by(user_id=user_id_followed).first()
    # Check whether users exist
    if not user or not user_followed:
        response_object['message'] = "Error: User does not exist!"
    else:
        # Check whether followed
        if user_followed not in user.following:
            response_object['following'] = False
        else:
            response_object['following'] = True
        response_object['status'] = True
        response_object['message'] = "Query success!"
    return jsonify(response_object)


@users.route('/block-unblock', methods=['GET', 'POST'])
def block_unblock():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        user_id_blocked = post_data.get('user_id_blocked')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    user_blocked = User.query.filter_by(user_id=user_id_blocked).first()
    # Check whether users exist
    if not user or not user_blocked:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocking_list = user.blocking
        # Check whether already blocked
        if user_blocked in user.blocking:
            blocking_list.remove(user_blocked)
        else:
            blocking_list.append(user_blocked)
        user.blocking = blocking_list
        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Blocked/Unblocked successfully!"
        except Exception as e:
            print(e)
            response_object['message'] = "Failed to block/unblock!"
    return jsonify(response_object)


@users.route('/query-blocking', methods=['GET', 'POST'])
def query_blocking():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocking_list = []
        for blocking in user.blocking:
            info = {}
            info['user_id'] = blocking.user_id
            info['username'] = blocking.username
            blocking_list.append(info)
        response_object['status'] = True
        response_object['message'] = "Query success!"
        response_object['blocking'] = blocking_list
    return jsonify(response_object)


@users.route('/check-block', methods=['GET', 'POST'])
def check_block():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        user_id_blocked = post_data.get('user_id_blocked')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    user_blocked = User.query.filter_by(user_id=user_id_blocked).first()
    # Check whether users exist
    if not user or not user_blocked:
        response_object['message'] = "Error: User does not exist!"
    else:
        # Check whether blocked
        if user_blocked not in user.blocking:
            response_object['blocking'] = False
        else:
            response_object['blocking'] = True
        response_object['status'] = True
        response_object['message'] = "Query success!"
    return jsonify(response_object)


@users.route('/register-token', methods=['GET', 'POST'])
def register_token():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        token = post_data.get('token')

    response_object = {}
    response_object['status'] = False
    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        user.token = token
        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Token successfully saved!"
        except Exception as e:
            print(e)
    return jsonify(response_object)


@users.route('/unregister-token', methods=['GET', 'POST'])
def unregister_token():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False
    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        user.token = None
        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Token successfully removed!"
        except Exception as e:
            print(e)
    return jsonify(response_object)


@users.route('/test-notification', methods=['GET', 'POST'])
def test_notification():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = sendNotification("Test", "Hello World", user_id)
    return jsonify(response_object)


@users.route('/test-followers-notification', methods=['GET', 'POST'])
def test_followers_notification():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        status_id = post_data.get('status_id')

    response_object = {}
    response_object['status'] = notifyFollowers(user_id, status_id)
    return jsonify(response_object)
