from flask_restplus import Resource, reqparse
from flask import current_app as app
from api.restplus import api
from ..models import wan_simulation
import logging
import json


ns = api.namespace('wan_simulation', description='Simulate a Wide Area Network')

simulation_parser = api.parser()
simulation_parser.add_argument('rate', type=str, required=False, help="Network bandwidth rate")
simulation_parser.add_argument('delay', type=str, required=False, help="Round trip network delay in ms")
simulation_parser.add_argument('delay-distro', type=str, required=False, help="Distribution of network latency")
simulation_parser.add_argument('loss', type=str, required=False, help="Round trip packet loss rate")
simulation_parser.add_argument('duplicate', type=str, required=False, help="Round trip packet duplicate rat")
simulation_parser.add_argument('corrupt', type=str, required=False, help="Packet corruption rate")
simulation_parser.add_argument('reordering', type=str, required=False, help="packet reordering rate")
simulation_parser.add_argument('network', type=str, required=False, help="Destination IP-address/network")
simulation_parser.add_argument('src-network', type=str, required=False, help="Source IP-address/network")
simulation_parser.add_argument('port', type=str, required=False, help="Destination port")
simulation_parser.add_argument('src-port', type=str, required=False, help="Source port")
simulation_parser.add_argument('exclude-dst-network', type=str, required=False, help="Exclude destination IP-address/network from WAN simulation")
simulation_parser.add_argument('exclude-src-network', type=str, required=False, help="Exclude source IP-address/network from WAN simulation")
simulation_parser.add_argument('exclude-dst-port', type=str, required=False, help="Exclude destination port from WAN simulation")
simulation_parser.add_argument('exclude-src-port', type=str, required=False, help="Exclude source port from WAN simulation")


@ns.route('/')
class WANSimulationCollection(Resource):

    @api.doc(responses={200: 'No running simulations',
                        201: 'There are WAN simulations running'})
    def get(self):
        """
        Returns list of running WAN simulations
        """
        AnomalyEngine = app.config['AnomalyEngine']
        simulations = AnomalyEngine.current_running_simulations()
        res = []
        for sim in simulations:
            res.append(sim.status())
        if not res:
            return res, 200
        return res, 201

    @api.doc(responses={200: 'No running simulations',
                        201: 'All running simulations reverted',
                        500: 'Error during stop operation'})
    def delete(self):
        """
        Reverts all running simulations.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        simulations = AnomalyEngine.current_running_simulations()

        if not simulations:
            return {"message": "No running simulations"}, 200
        for sim in simulations:
            error = sim.stop()
            if error:
                api.abort(500, "Error during stop operation: {}".format(error))
        return {"message": "All simulations stopped"}, 201


@ns.route('/<string:interface>/')
@api.doc(params={'interface': 'WAN simulated interface'})
class WAN(Resource):

    @api.doc(responses={200: 'Simulation is not running',
                        201: 'Simulation is running',
                        400: 'Request Error'})
    def get(self, interface):
        """
        Returns status of WAN simulation on the given interface.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        simulation = AnomalyEngine.lookup_simulation(interface)

        if simulation == None:
            logging.info("Can't find interface " + interface)
            api.abort(400, "Interface {} doesn't exist".format(interface))

        if simulation.is_running():
            return simulation.status(), 201
        else:
            return {"message": "No simulation running"}, 200


    @api.doc(responses={200: 'Simulation has been started',
                        201: 'Rule has been added to the simulation',
                        406: 'Given network is already used in a simulation',
                        500: 'Error during execution'})
    @api.expect(wan_simulation)
    def post(self, interface):
        """
        Start WAN simulation on given interface or add rule to running WAN simulation.
        """
        AnomalyEngine = app.config['AnomalyEngine']
        interface_simulation = AnomalyEngine.lookup_simulation(interface)
        args = simulation_parser.parse_args()
        params = {key:value for key,value in args.items() if value}

        if interface_simulation == None:
            logging.info("Can't find interface " + interface)
            api.abort(400, "Interface {} doesn't exist".format(interface))

        logging.info("Using interface " + interface + " for the WAN simulation")
        logging.info("Using the following simulation params: " + json.dumps(params))

        if AnomalyEngine.simulation_running(interface_simulation):
            logging.info("There is already a simulation running on interface " + interface + ". Trying to add rule.")
            if not interface_simulation.network_conflict(params):
                error = AnomalyEngine.start_simulation(interface_simulation, params, add_rule=True)
                if error:
                    api.abort(500, "Error during start of simulation: {}".format(error))
            else:
                api.abort(406, "There is already a running simulation using the network you specified")
            return interface_simulation.status(), 201
        AnomalyEngine.start_simulation(interface_simulation, params)
        return interface_simulation.status(), 200

    @api.doc(responses={201: 'Simulation has been updated',
                        400: 'Given interface does not exist',
                        405: 'No running simulation on the given interface',
                        500: 'Error during execution'})
    @api.expect(wan_simulation)
    def put(self, interface):
        """
        Update a WAN simulation on given interface
        """
        AnomalyEngine = app.config['AnomalyEngine']
        simulation = AnomalyEngine.lookup_simulation(interface)
        args = simulation_parser.parse_args()
        params = {key:value for key,value in args.items() if value}

        if simulation == None:
            logging.info("Can't find interface " + interface)
            api.abort(400, "Interface {} doesn't exist".format(interface))

        if not simulation.is_running():
            api.abort(405, "There is no simulation running on interface {}".format(interface))

        logging.info("Updating WAN simulation on " + interface)
        logging.info("Using the following simulation params: " + json.dumps(params))
        error = AnomalyEngine.start_simulation(simulation, params, update=True)
        if error:
            api.abort(500, "Error during execution: {}".format(error))
        return simulation.status(), 201


    @api.doc(responses={201: 'Simulation has been stopped',
                        400: 'Given interface does not exist',
                        404: 'No running simulation on the given interface',
                        500: 'Error during stop operation'})
    def delete(self, interface):
        """
        Stop WAN Simulation on given interface
        """
        AnomalyEngine = app.config['AnomalyEngine']
        simulation = AnomalyEngine.lookup_simulation(interface)

        if simulation == None:
            logging.info("Can't find interface " + interface)
            api.abort(400, "Interface {} doesn't exist".format(interface))

        if simulation.is_running():
            error = simulation.stop()
            if error:
                api.abort(500, "Error during stop operation: {}".format(error))
        else:
            api.abort(404, "There is no simulation running on interface {}".format(interface))




