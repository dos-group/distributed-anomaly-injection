import subprocess, netifaces, logging
from wan_simulation.wan_simulation import WanSimulation
from ast import literal_eval
import json


class TrafficControlSimulation(WanSimulation):

    _network_keys = ['network', 'src-network', 'exclude-dst-network', 'exclude-src-network']

    def __init__(self, interface, payload=[]):
        self.interface = interface
        self.running = False
        self.parameters = payload

    def start(self, params, update=False, add_rule=False):
        tcset_cmd = ["tcset", "--device", self.interface]
        logging.info(params)

        for opt in params.keys():
            tmp = ["--" + opt, params[opt]]
            tcset_cmd.extend(tmp)
        if update:
            logging.info("Updating rule of simulation")
            tcset_cmd.append("--change")
        if add_rule:
            logging.info("Adding rule to simulation")
            tcset_cmd.append("--add")
        logging.info("Starting the simulation with cmd: %s", tcset_cmd)
        try:
            subprocess.check_output(tcset_cmd)
        except subprocess.CalledProcessError as e:
            return e.output
        self.running = True
        self.set_parameters(params, update)

    def status(self):
        if self.is_running():
            show_cmd = ["tcshow", "--device", self.interface]
            output = subprocess.check_output(show_cmd)
            res = {
                'interface': self.interface,
                'running': self.running,
                'parameters': self.parameters,
                'tcshow': json.loads(output)
            }
            return res
        else:
            return None

    def stop(self):
        logging.info("Stopping WAN simulation on interface " + self.interface)
        try:
            subprocess.check_output(["tcdel", "--device", self.interface, "--all"])
        except subprocess.CalledProcessError as e:
            return e.output
        self.running = False
        self.parameters = []

    def is_running(self):
        return self.running

    def get_payload(self):
        return self.parameters

    def network_conflict(self, params):
        param_networks = [params[opt] for opt in self._network_keys if opt in params]
        used_networks = self.used_networks()
        logging.info("Networks in new parameter: " + str(param_networks))
        logging.info("Used networks in the simulation: " + str(used_networks))
        if len(param_networks) == len(used_networks) == 0:
            return True
        if any(network in param_networks for network in used_networks):
            return True
        return False

    def used_networks(self):
        networks = []
        for rule in self.parameters:
            net = [rule[key] for key in self._network_keys if key in rule]
            networks.extend(net)
        return networks

    def set_parameters(self, params, update):
        if not update:
            logging.info("Appending parameter " + json.dumps(params))
            self.parameters.append(params)
            return
        param_networks = [params[opt] for opt in self._network_keys if opt in params]
        # find rule to update
        for rule in self.parameters:
            rule_networks = [rule[opt] for opt in self._network_keys if opt in rule]
            no_networks = len(param_networks) == len(rule_networks) == 0
            if no_networks or any(network in rule_networks for network in param_networks):
                logging.info("Updating parameter " + json.dumps(rule) + "with " + json.dumps(params))
                rule.update(params)
                return
            else:
                logging.info("Could not found parameter to update. Appending parameter " + json.dumps(params))
                self.parameters.append(params)