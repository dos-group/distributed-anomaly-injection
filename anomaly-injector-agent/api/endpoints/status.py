from flask_restplus import Resource
from api.restplus import api
from flask import current_app as app

ns = api.namespace('status', description='Operations related to the status of the host')

@ns.route('/')
class HostStatus(Resource):

    def get(self):
        """
        Return list of known anomalies with default parameters.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        anomalies = []
        for anomaly in AnomalyEngine.all_anomalies:
            tmp = {
                "name": anomaly.name,
                "default_param": anomaly.default_parameters,
                "currently_running": AnomalyEngine.check_anomaly_status(anomaly)
            }
            anomalies.append(tmp)
            res = {
                "Hostnames": AnomalyEngine.own_hostnames,
                "Injection Mode": AnomalyEngine.current_mode,
                "known anomalies": anomalies
            }
        return res

