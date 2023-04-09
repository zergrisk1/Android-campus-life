import pathlib

from flask import Blueprint, jsonify, request, make_response
from . import db
from .models import *
from .utils import *
from datetime import datetime
from sqlalchemy import desc, or_
import uuid, os, json, configparser

status = Blueprint('status', __name__)


@status.route('/create-status', methods=['GET', 'POST'])
def create_status():
    if request.method == 'POST':
        user_id = request.form.get('user_id')
        type = request.form.get('type')
        title = request.form.get('title')
        text = request.form.get('text')
        location = request.form.get('location')
        if type != "TEXT":
            media = request.files['media']

    response_object = {}
    response_object['status'] = False

    if not check_len255(title):
        response_object['message'] = "Error: Title too long!"
        return jsonify(response_object)

    config = configparser.RawConfigParser()
    config.read('config.cfg')
    upload_dict = dict(config.items('UPLOAD'))
    upload_folder = upload_dict["upload_folder"]

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        # Text only status
        if type == "TEXT":
            status = Status()
            status_id = str(uuid.uuid4())
            status.id = status_id
            status.user_id = user_id
            status.username = user.username
            status.type = type
            status.title = title
            status.text = text
            status.location = location
            status.date_created = datetime.now()
            status.like = 0
            like_users = []
            status.like_users = json.dumps(like_users)
            db.session.add(status)

            # Notify followers
            notifyFollowers(user_id, status_id)
        # Image status
        elif type == "IMAGE" or type == "AUDIO" or type == "VIDEO":
            if media:
                status = Status()
                status_id = str(uuid.uuid4())
                status.id = status_id
                status.user_id = user_id
                status.username = user.username
                status.type = type
                status.title = title
                status.text = text
                status.location = location
                status.date_created = datetime.now()
                status.like = 0
                like_users = []
                status.like_users = json.dumps(like_users)

                dir = os.path.join(upload_folder, type.lower())
                if not os.path.exists(dir):
                    os.makedirs(dir)

                file_ext = pathlib.Path(media.filename).suffix
                filename = uuid.uuid4().hex + file_ext
                path = os.path.join(dir, filename)
                media.save(path)

                status.media = filename
                db.session.add(status)

                # Notify followers
                notifyFollowers(user_id, status_id)
            else:
                response_object['message'] = "Error: No media included!"
        else:
            response_object['message'] = "Error: Invalid status type!"
        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Status created successfully!"
        except Exception as e:
            print(e)
            response_object['message'] = "Failed to create status"
    return jsonify(response_object)


@status.route('/image/<string:filename>', methods=['GET'])
def get_image(filename):
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
            dir = os.path.join(upload_folder, "image")

            image = os.path.join(dir, filename)
            if not os.path.isfile(image):
                response_object['message'] = "Error: File does not exist!"
                return jsonify(response_object)
            else:
                image_data = open(image, "rb").read()
                response = make_response(image_data)
                response.headers['Content-Type'] = "image/png"
                return response


@status.route('/audio/<string:filename>', methods=['GET'])
def get_audio(filename):
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
            dir = os.path.join(upload_folder, "audio")

            audio = os.path.join(dir, filename)
            if not os.path.isfile(audio):
                response_object['message'] = "Error: File does not exist!"
                return jsonify(response_object)
            else:
                audio_data = open(audio, "rb").read()
                response = make_response(audio_data)
                response.headers['Content-Type'] = "audio/mpeg"
                return response


@status.route('/video/<string:filename>', methods=['GET'])
def get_video(filename):
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
            dir = os.path.join(upload_folder, "video")

            video = os.path.join(dir, filename)
            if not os.path.isfile(video):
                response_object['message'] = "Error: File does not exist!"
                return jsonify(response_object)
            else:
                video_data = open(video, "rb").read()
                response = make_response(video_data)
                response.headers['Content-Type'] = "video/mp4"
                return response


@status.route('/query-user-status', methods=['GET', 'POST'])
def query_user_status():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        status_list = Status.query.filter_by(user_id=user_id).order_by(desc(Status.date_created)).all()
        ret = []
        for status in status_list:
            item = {}
            item['status_id'] = status.id;
            item['creator_id'] = status.user_id
            item['creator_username'] = status.username
            item['type'] = status.type
            item['title'] = status.title
            item['text'] = status.text
            item['date_created'] = status.date_created
            item['like'] = status.like
            ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-all-status', methods=['GET', 'POST'])
def query_all_status():
    if request.method == 'POST':
        post_data = request.get_json()
        order_by_like = post_data.get('order_by_like')
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocked_users = user.blocking
        blocked_users_id = []
        for blocked_user in blocked_users:
            blocked_users_id.append(blocked_user.user_id)

        if order_by_like == 'true':
            status_list = Status.query.order_by(desc(Status.like)).all()
        else:
            status_list = Status.query.order_by(desc(Status.date_created)).all()

        ret = []
        for status in status_list:
            if status.user_id in blocked_users_id:
                continue
            item = {}
            item['status_id'] = status.id;
            item['creator_id'] = status.user_id
            item['creator_username'] = status.username
            item['type'] = status.type
            item['title'] = status.title
            item['text'] = status.text
            item['date_created'] = status.date_created
            item['like'] = status.like
            ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-followed-status', methods=['GET', 'POST'])
def query_followed_status():
    if request.method == 'POST':
        post_data = request.get_json()
        order_by_like = post_data.get('order_by_like')
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocked_users = user.blocking
        blocked_users_id = []
        for blocked_user in blocked_users:
            blocked_users_id.append(blocked_user.user_id)

        following_users = user.following
        ret = []

        if len(following_users) != 0:
            following_users_id = []
            for following_user in following_users:
                following_users_id.append(following_user.user_id)

            if order_by_like == 'true':
                status_list = Status.query.filter(or_(*[Status.user_id.like(i) for i in following_users_id])) \
                    .order_by(desc(Status.like)).all()
            else:
                status_list = Status.query.filter(or_(*[Status.user_id.like(i) for i in following_users_id])) \
                    .order_by(desc(Status.date_created)).all()

            for status in status_list:
                if status.user_id in blocked_users_id:
                    continue
                item = {}
                item['status_id'] = status.id
                item['creator_id'] = status.user_id
                item['creator_username'] = status.username
                item['type'] = status.type
                item['title'] = status.title
                item['text'] = status.text
                item['date_created'] = status.date_created
                item['like'] = status.like
                ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-status-type', methods=['GET', 'POST'])
def query_status_by_type():
    if request.method=='POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        type = post_data.get('type')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocked_users = user.blocking
        blocked_users_id = []
        for blocked_user in blocked_users:
            blocked_users_id.append(blocked_user.user_id)

        status_list = Status.query.filter_by(type=type).order_by(desc(Status.date_created)).all()
        ret = []
        for status in status_list:
            if status.user_id in blocked_users_id:
                continue
            item = {}
            item['status_id'] = status.id
            item['creator_id'] = status.user_id
            item['creator_username'] = status.username
            item['type'] = status.type
            item['title'] = status.title
            item['text'] = status.text
            item['date_created'] = status.date_created
            item['like'] = status.like
            ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-status-creator', methods=['GET', 'POST'])
def query_status_by_creator():
    if request.method=='POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        creator = post_data.get('creator')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocked_users = user.blocking
        blocked_users_id = []
        for blocked_user in blocked_users:
            blocked_users_id.append(blocked_user.user_id)

        # Extract username
        username_list = db.session.query(Status.username)
        username_list = list(set(username_list))
        username_list = [i[0] for i in username_list]

        # Select usernames similar to creator
        selected_username = fuzzysearch(creator, username_list)

        if len(selected_username) != 0:
            status_list = Status.query.filter(or_(*[Status.username.like(i) for i in selected_username])) \
                .order_by(desc(Status.date_created)).all()
        else:
            status_list = []
        ret = []
        for status in status_list:
            if status.user_id in blocked_users_id:
                continue
            item = {}
            item['status_id'] = status.id
            item['creator_id'] = status.user_id
            item['creator_username'] = status.username
            item['type'] = status.type
            item['title'] = status.title
            item['text'] = status.text
            item['date_created'] = status.date_created
            item['like'] = status.like
            ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-status-title', methods=['GET', 'POST'])
def query_status_title():
    if request.method=='POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        title = post_data.get('title')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocked_users = user.blocking
        blocked_users_id = []
        for blocked_user in blocked_users:
            blocked_users_id.append(blocked_user.user_id)

       # Extract title
        title_list = db.session.query(Status.title)
        title_list = list(set(title_list))
        title_list = [i[0] for i in title_list]

        # Select titles similar to title
        selected_title = fuzzysearch(title, title_list)

        if len(selected_title) != 0:
            status_list = Status.query.filter(or_(*[Status.title.like(i) for i in selected_title]))\
                .order_by(desc(Status.date_created)).all()
        else:
            status_list = []
        ret = []
        for status in status_list:
            if status.user_id in blocked_users_id:
                continue
            item = {}
            item['status_id'] = status.id
            item['creator_id'] = status.user_id
            item['creator_username'] = status.username
            item['type'] = status.type
            item['title'] = status.title
            item['text'] = status.text
            item['date_created'] = status.date_created
            item['like'] = status.like
            ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-status-text', methods=['GET', 'POST'])
def query_status_by_text():
    if request.method=='POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        text = post_data.get('text')

    response_object = {}
    response_object['status'] = False

    user = User.query.filter_by(user_id=user_id).first()
    # Check whether user exist
    if not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        blocked_users = user.blocking
        blocked_users_id = []
        for blocked_user in blocked_users:
            blocked_users_id.append(blocked_user.user_id)

        # Extract text
        text_list = db.session.query(Status.text)
        text_list = list(set(text_list))
        text_list = [i[0] for i in text_list]

        # Select texts similar to text
        selected_text = fuzzysearch(text, text_list)

        if len(selected_text) != 0:
            status_list = Status.query.filter(or_(*[Status.text.like(i) for i in selected_text])) \
                .order_by(desc(Status.date_created)).all()
        else:
            status_list = []
        ret = []
        for status in status_list:
            if status.user_id in blocked_users_id:
                continue
            item = {}
            item['status_id'] = status.id
            item['creator_id'] = status.user_id
            item['creator_username'] = status.username
            item['type'] = status.type
            item['title'] = status.title
            item['text'] = status.text
            item['date_created'] = status.date_created
            item['like'] = status.like
            ret.append(item)
        response_object['status'] = True
        response_object['message'] = 'Query success!'
        response_object['status_list'] = ret
    return jsonify(response_object)


@status.route('/query-status', methods=['GET', 'POST'])
def query_status():
    if request.method == 'POST':
        post_data = request.get_json()
        status_id = post_data.get('status_id')

    response_object = {}
    response_object['status'] = False

    status = Status.query.filter_by(id=status_id).first()
    # Check whether status exist
    if not status:
        response_object['message'] = "Error: Status does not exist!"
    else:
        response_object['status'] = True
        response_object['message'] = "Query success!"
        response_object['creator_id'] = status.user_id
        response_object['creator_username'] = status.username
        response_object['type'] = status.type
        response_object['title'] = status.title
        response_object['text'] = status.text
        response_object['media'] = status.media
        response_object['location'] = status.location
        response_object['date_created'] = status.date_created
        response_object['like'] = status.like

        like_users = []
        tmp = json.loads(status.like_users)
        for user in tmp:
            tmp_dict = {}
            tmp_dict['user_id'] = user
            tmp_dict['username'] = User.query.filter_by(user_id=user).first().username
            like_users.append(tmp_dict)

        response_object['like_users'] = like_users
    return jsonify(response_object)


@status.route('/like-unlike', methods=['GET', 'POST'])
def like_unlike():
    if request.method == 'POST':
        post_data = request.get_json()
        status_id = post_data['status_id']
        user_id = post_data['user_id']

    response_object = {}
    response_object['status'] = False

    status = Status.query.filter_by(id=status_id).first()
    user = User.query.filter_by(user_id=user_id).first()
    if not status:
        response_object['message'] = "Error: Status does not exist!"
    elif not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        liked = False
        like_users = json.loads(status.like_users)
        for like_user in like_users:
            if user_id == like_user:
                liked = True
                break

        # If user already liked, unlike instead
        if liked:
            like_users.remove(user_id)
            status.like -= 1
            status.like_users = json.dumps(like_users)
        else:
            status.like += 1
            like_users.append(user_id)
            status.like_users = json.dumps(like_users)

            # Notify creator
            creator_id = status.user_id
            if user_id != creator_id:
                title = "点赞"
                message = "有人赞了你的动态"
                type = "LIKE"
                sendNotification(title, message, creator_id)
                add_notifications(creator_id, type, title, message, status_id)

        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Liked/Unliked!"
        except Exception as e:
            print(e)
            response_object['message'] = "Failed!"
    return jsonify(response_object)


@status.route('/query-like', methods=['GET', 'POST'])
def query_like():
    if request.method == 'POST':
        post_data = request.get_json()
        user_id = post_data.get('user_id')
        status_id = post_data.get('status_id')

    response_object = {}
    response_object['status'] = False

    status = Status.query.filter_by(id=status_id).first()
    user = User.query.filter_by(user_id=user_id).first()
    if not status:
        response_object['message'] = "Error: Status does not exist!"
    elif not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        like_users = json.loads(status.like_users)
        liked = False
        for like_user in like_users:
            if like_user == user_id:
                liked = True
                break
        response_object['status'] = True
        response_object['message'] = "Query success!"
        response_object['liked'] = liked
    return jsonify(response_object)


@status.route('/add-comment', methods=['GET', 'POST'])
def add_comment():
    if request.method == 'POST':
        post_data = request.get_json()
        status_id = post_data.get('status_id')
        user_id = post_data.get('user_id')
        content = post_data.get('content')

    response_object = {}
    response_object['status'] = False

    status = Status.query.filter_by(id=status_id).first()
    user = User.query.filter_by(user_id=user_id).first()
    # Check whether status or user exist
    if not status:
        response_object['message'] = "Error: Status does not exist!"
    elif not user:
        response_object['message'] = "Error: User does not exist!"
    else:
        comment = Comments()
        comment.id = str(uuid.uuid4())
        comment.user_id = user_id
        comment.content = content
        comment.date_created = datetime.now()

        rel_status_comment = RelationStatusComments(status_id=status_id, comment_id=comment.id)
        db.session.add(comment)
        db.session.add(rel_status_comment)

        creator_id = status.user_id
        # Notify creator
        if user_id != creator_id:
            title = "评论"
            message = "有人评论了你的动态"
            type = "COMMENT"
            sendNotification(title, message, creator_id)
            add_notifications(creator_id, type, title, message, status_id)

        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Comment added successfully!"
        except Exception as e:
            print(e)
            response_object['message'] = "Failed to add comment"
    return jsonify(response_object)


@status.route('/query-comment', methods=['GET', 'POST'])
def query_comment():
    if request.method == 'POST':
        post_data = request.get_json()
        status_id = post_data.get('status_id')

    response_object = {}
    response_object['status'] = False

    status = Status.query.filter_by(id=status_id).first()
    # Check if status exist
    if not status:
        response_object['message'] = "Error: Status does not exist!"
    else:
        rels = status.relation_status_comments
        ret = []
        for rel in rels:
            item = {}
            comment_id = rel.comment_id
            comment = Comments.query.filter_by(id=comment_id).first()
            item['comment_id'] = comment_id
            item['user_id'] = comment.user_id
            user = User.query.filter_by(user_id=comment.user_id).first()
            if user:
                item['username'] = user.username
            item['content'] = comment.content
            item['date_created'] = comment.date_created
            ret.append(item)
        ret.sort(key=lambda x:x['date_created'])
        response_object['status'] = True
        response_object['message'] = "Query success!"
        response_object['comment_list'] = ret
    return jsonify(response_object)


@status.route('/delete-comment', methods=['GET', 'POST'])
def delete_comment():
    if request.method == 'POST':
        post_data = request.get_json()
        status_id = post_data.get('status_id')
        comment_id = post_data.get('comment_id')
        user_id = post_data.get('user_id')

    response_object = {}
    response_object['status'] = True

    status = Status.query.filter_by(id=status_id).first()
    comment = Comments.query.filter_by(id=comment_id).first()
    user = User.query.filter_by(user_id=user_id).first()
    # Check if status, comment and user exist
    if not status:
        response_object['message'] = "Error: Status does not exist!"
    elif not comment:
        response_object['message'] = "Error: Comment does not exist!"
    elif not user:
        response_object['message'] = "Error: User does not exist!"
    elif user_id != comment.user_id:
        response_object['message'] = "Error: User not creator of comment!"
    else:
        for rel in comment.relation_status_comments:
            db.session.delete(rel)
        db.session.delete(comment)
        try:
            db.session.commit()
            response_object['status'] = True
            response_object['message'] = "Comment deleted successfully!"
        except Exception as e:
            print(e)
            response_object['message'] = "Failed to delete comment"
    return jsonify(response_object)


