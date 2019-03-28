from flask_restplus import Resource
from api.restplus import api
from flask import current_app as app


ns = api.namespace('mode', description='Operations related to the injection mode of the host')

@ns.route('/')
@api.doc(responses={200: 'Success'})
class InectionModus(Resource):

    def get(self):
        """
        Returns current injection mode of the host.
        """
        AnomalyEngine = app.config['AnomalyEngine']

        res = {
                "Injection mode": AnomalyEngine.current_mode
            }
        return res, 200


@ns.route('/<string:mode>/')
@api.doc(responses={200: 'Injector Agent is already running in given mode',
                    201: 'Mode has been changed',
                    400: 'Error',
                    409: 'Anomalies are running'})
class ChangeModus(Resource):

    def post(self, mode):
        """
        Set Injection mode of the host.
        """
        AnomalyEngine = app.config['AnomalyEngine']

        if mode != "timeplan" and mode != "manual":
            api.abort(400, "Please chose 'timeplan' or 'manual' as mode ")

        if AnomalyEngine.current_mode == mode:
            return {"message": "Injector Agent is already in %s mode" % mode}, 200

        if AnomalyEngine.current_running_anomalies():
            api.abort(409, "Cannot change mode when anomalies are running.")

        if AnomalyEngine.set_mode(mode):
            return {"message": "mode changed to %s" % mode}, 201
        else:
            return {"message": "mode not changed"}, 400
