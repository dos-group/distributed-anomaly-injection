from .restplus import api
from flask_restplus import fields

inject_anomaly = api.model('InjectAnomaly', {
    'parameter': fields.String(description="Parameter for the anomaly.", required=False)
})

timeplan_item = api.model('Timeplan', {
    'start_time': fields.String,
    'duration': fields.String,
    'target_host': fields.String,
    'anomaly_name': fields.String,
    'parameters': fields.String,
    'tag': fields.String,
    'is_global': fields.Boolean,
    'index': fields.String
})