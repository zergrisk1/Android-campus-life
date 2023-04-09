import pathlib

from flask import Blueprint, jsonify, request, make_response
from . import db
from .models import *
import uuid, os
from .utils import *

notifications = Blueprint('notifications', __name__)


@notifications.route('/query-notifications', methods=['GET', 'POST'])
def query_notifications():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        rels = user.relation_user_notifications
        ret = []
        for rel in rels:
            item = {}
            notifications_id = rel.notifications_id
            notify = Notifications.query.filter_by(id=notifications_id).first()
            item['notifications_id'] = notifications_id
            item['type'] = notify.type
            item['title'] = notify.title
            item['text'] = notify.text
            item['status_id'] = notify.status_id
            item['date_created'] = notify.date_created
            ret.append(item)
        ret.sort(key=lambda x:x['date_created'], reverse=True)
        response_object['status'] = True
        response_object['message'] = "Query success!"
        response_object['notifications_list'] = ret
    return jsonify(response_object)


@notifications.route('/test-add-notifications', methods=['GET', 'POST'])
def test_add_notifications():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        status_id = post_data.get('status_id')

    title = 'Test'
    message = 'Hello world'
    type = 'TEST'
    sendNotification(title, message, user_id)
    response_object = {}
    response_object['status'] = add_notifications(user_id, type, title, message, status_id)
    return jsonify(response_object)