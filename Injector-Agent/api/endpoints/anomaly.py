from flask_restplus import Resource
from flask import current_app as app
from api.restplus import api
from ..models import inject_anomaly
import logging

ns = api.namespace('anomalies', description='Operations related to injecting and reverting anomalies')

@ns.route('/')
class AnomalyCollection(Resource):

    @api.doc(responses={200: 'No running anomalies',
                        201: 'There are some anomalies running'})
    def get(self):
        """
        Returns list of running anomalies.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        res = []
        for anomaly in AnomalyEngine.current_running_anomalies():
            tmp = {
                "name": anomaly.name,
                "parameter": AnomalyEngine.last_parameters[anomaly.name],
                "default_param": anomaly.default_parameters == AnomalyEngine.last_parameters[anomaly.name]
            }
            res.append(tmp)
        res_code = 201 if res else 200
        return res, res_code


    @api.doc(responses={200: 'No running anomalies',
                        201: 'All running anomalies reverted'})
    def delete(self):
        """
        Reverts all running anomalies.
        """
        AnomalyEngine = app.config['AnomalyEngine']

        # No manual reverting of anomalies when in timeplan mode
        if AnomalyEngine.current_mode == "timeplan":
            api.abort(403, "no manual reverting of anomalies when in timeplan mode")

        current_anomalies = AnomalyEngine.current_running_anomalies()
        if not current_anomalies:
            return {'message': 'no running anomalies'}, 200
        for anomaly in current_anomalies:
            AnomalyEngine.revert_anomaly(anomaly)
        return {'message': 'all running anomalies reverted'}, 201


@ns.route('/<string:name>/')
@api.doc(params={'name': 'Name of the anomaly'})
class Anomaly(Resource):

    @api.doc(responses={200: 'Anomaly running',
                        202: 'Anomaly not running',
                        404: 'Anomaly not found'})
    def get(self, name):
        """
        Returns the status of the given anomaly.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        anomaly = AnomalyEngine.lookup_anomaly(name)
        if anomaly != None:
            if AnomalyEngine.check_anomaly_status(anomaly):
                res = {
                    'name': anomaly.name,
                    'parameter': AnomalyEngine.last_parameters[anomaly.name],
                    'running': AnomalyEngine.check_anomaly_status(anomaly)
                }
                return res, 200
            else:
                res = {
                    'name': anomaly.name,
                    'running': AnomalyEngine.check_anomaly_status(anomaly)
                }
                return res, 202
        api.abort(404, "Anomaly {} doesn't exist".format(name))


    @api.expect(inject_anomaly)
    @api.doc(responses={201: 'Anomaly has been created',
                        200: 'Anomaly already running',
                        400: 'Request Error'})
    def post(self, name):
        """
        Inject the given anomaly on the host.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        anomaly = AnomalyEngine.lookup_anomaly(name)

        if anomaly == None:
            api.abort(400, "Anomaly {} doesn't exist".format(name))

        if AnomalyEngine.current_mode == "timeplan":
            api.abort(403, "no manual injection of anomalies when in timeplan mode")

        logging.info("Received params: " + str(api.payload))

        if "parameter" in api.payload:
            parameter = api.payload['parameter']
        else:
            parameter = anomaly.default_parameters

        if "time" in api.payload:
            time = api.payload['time']
        else:
            time = -1

        if "prevent_collector_tagging" in api.payload:
            prevent_tagging = True
        else:
            prevent_tagging = False

        if AnomalyEngine.check_anomaly_status(anomaly):
            if AnomalyEngine.last_parameters[anomaly.name] == parameter:
                return {"message": "Anomaly is already running"}, 200
            AnomalyEngine.revert_anomaly(anomaly) #Anomaly with new parameters will be started. Suspend old one.

        if AnomalyEngine.inject_anomaly(anomaly, parameter, time, prevent_tagging):
            return {"message": "Anomaly injected with parameter: %s" % parameter}, 201
        api.abort(500, "Error")

    @api.doc(responses={201: 'Anomaly was reverted',
                        200: 'Anomaly is not running',
                        404: 'Anomaly not known'})
    def delete(self, name):
        """
        Reverts the given anomaly on the host.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        anomaly = AnomalyEngine.lookup_anomaly(name)
        if anomaly == None:
            api.abort(404, "Anomaly {} now known".format(name))

        if AnomalyEngine.current_mode == "timeplan":
            api.abort(403, "no manual reverting of anomalies when in timeplan mode")

        if not AnomalyEngine.check_anomaly_status(anomaly):
            return {"message": "Anomaly is not running"}, 200
        if AnomalyEngine.revert_anomaly(anomaly):
            return {"message": "Anomaly has been reverted"}, 201
        api.abort(500, "Error")
