from flask_restplus import Api

api = Api( version='0.1.0', title='Distributed Anomaly Injection API',
          description="REST API for the bitflow Distributed Anomaly Injection")


# TODO: logging, errorhandler