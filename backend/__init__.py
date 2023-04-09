from flask import Flask, current_app
from flask_sqlalchemy import SQLAlchemy
from flask_apscheduler import APScheduler
from flask.json import JSONEncoder
from datetime import date
import configparser

db = SQLAlchemy()

class CustomJSONEncoder(JSONEncoder):
    def default(self, obj):
        try:
            if isinstance(obj, date):
                return obj.isoformat()
            iterable = iter(obj)
        except TypeError:
            pass
        else:
            return list(iterable)
        return JSONEncoder.default(self, obj)


def create_app():
    app = Flask(__name__)
    app.json_encoder = CustomJSONEncoder

    config = configparser.RawConfigParser()
    config.read('config.cfg')
    db_dict = dict(config.items('DATABASE'))
    app.config["SECRET_KEY"] = db_dict["secret_key"]
    app.config["SQLALCHEMY_DATABASE_URI"] = db_dict["db"]
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

    db.init_app(app)

    from .users import users as users_blueprint
    app.register_blueprint(users_blueprint)

    from .main import main as main_blueprint
    app.register_blueprint(main_blueprint)

    from .status import status as status_blueprint
    app.register_blueprint(status_blueprint)

    from .notifications import notifications as notifications_blueprint
    app.register_blueprint(notifications_blueprint)

    return app