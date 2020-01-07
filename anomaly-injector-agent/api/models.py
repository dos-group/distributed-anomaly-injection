from .restplus import api
from flask_restplus import fields

inject_anomaly = api.model('InjectAnomaly', {
    'parameter': fields.String(description="Parameter for the anomaly.", required=False)
})

wan_simulation = api.model('SimulateWAN', {
    'rate': fields.String(description="Network bandwidth rate", required=False),
    'delay': fields.String(description="Round trip network delay in ms", required=False),
    'delay-distro': fields.String(description="Distribution of network latency", required=False ),
    'loss': fields.String(description="Round trip packet loss rate", required=False),
    'duplicate': fields.String(description="Round trip packet duplicate rate", required=False),
    'corrupt': fields.String(description="Packet corruption rate", required=False),
    'reordering': fields.String(description="packet reordering rate", required=False),
    'network': fields.String(description="Destination IP-address/network", required=False),
    'src-network': fields.String(description="Source IP-address/network", requirered=False),
    'port': fields.String(description="Destination port", required=False),
    'src-port': fields.String(description="Source port", required=False),
    'exclude-dst-network': fields.String(description="Exclude destination IP-address/network from WAN simulation", required=False),
    'exclude-src-network': fields.String(description="Exclude source IP-address/network from WAN simulation", required=False),
    'exclude-dst-port': fields.String(description="Exclude destination port from WAN simulation", required=False),
    'exclude-src-port': fields.String(description="Exclude source port from WAN simulation", required=False)
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