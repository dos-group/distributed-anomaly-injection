import logging
from flask import Flask, Blueprint
from flask_restplus import Resource, Api
from api.endpoints.anomaly import ns as anomaly_namespace
from api.endpoints.status import ns as status_namespace
from api.endpoints.modus import ns as modus_namespace
from api.endpoints.timeplan import ns as timeplan_namespace
from api.restplus import api

def initialize(app):
    blueprint = Blueprint('api', __name__, url_prefix='/api')
    api.init_app(blueprint)
    api.add_namespace(anomaly_namespace)
    api.add_namespace(status_namespace)
    api.add_namespace(modus_namespace)
    api.add_namespace(timeplan_namespace)
    app.register_blueprint(blueprint)


def create_app(AnomalyEngine):
    app = Flask(__name__)
    app.config['AnomalyEngine'] = AnomalyEngine
    initialize(app)
    return app

if __name__ == "__main__":
    create_app()
