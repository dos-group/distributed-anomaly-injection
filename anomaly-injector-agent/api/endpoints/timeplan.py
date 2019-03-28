from flask_restplus import Resource
from werkzeug.datastructures import FileStorage
from flask import current_app as app
from ..models import timeplan_item

from api.restplus import api

ns = api.namespace('timeplan', description='Operations related to the injection timeplan of the host')




timeplan_parser = api.parser()
timeplan_parser.add_argument('timeplan', location='files',
                             type=FileStorage, required=True)


@ns.route('/')
class HostTimePlan(Resource):

    @api.doc(responses={200: 'Timeplan has been returned.',
                        400: 'No Timeplan exists.'})
    @api.marshal_with(timeplan_item)
    def get(self):
        """
        Returns the currently injected timeplan as JSON Object.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        timeplan = AnomalyEngine.timeplan
        if timeplan:
            return timeplan.plan, 200
        else:
            api.abort(400, "no timeplan")


    @api.expect(timeplan_parser)
    @api.doc(responses={200: 'Timeplan has been injected',
                        400: 'Request Error'})
    @api.marshal_with(timeplan_item)
    def post(self):
        """
        Inject timeplan into the host.
        """
        #
        # curl -X POST  -F timeplan=@cpu_stress.csv 'http://localhost:7888/api/timeplan/'
        #
        # TODO: probably some input valdation
        #
        AnomalyEngine = app.config['AnomalyEngine']
        args = timeplan_parser.parse_args()

        timeplan = args['timeplan']
        timeplan.save('/tmp/timeplan')
        AnomalyEngine.set_timeplan('/tmp/timeplan')
        return AnomalyEngine.timeplan.plan, 200
