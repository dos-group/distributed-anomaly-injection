#!/usr/bin/env python3

import logging, atexit, random, re, argparse, time, requests
import datetime

import threading

import logging_config
import anomaly_list
from timeplan import Timeplan, TimeplanItem
from anomalies import anomaly
from anomalies.anomaly import *
from anomalies.anomaly_network import *
from anomalies.anomaly_cpu import *
from anomalies.anomaly_process import *
from anomalies.anomaly_process_python import *

from api.rest_api import create_app


class AnomalyEngine(object):

    SHUTTING_DOWN_RETRIES = 3
    shutting_down = False
    http_tag_timeout = 30 # seconds. Since we do regular POSTs, pick a small timeout.

    def __init__(self, labelling_client, all_anomalies, own_hostnames, set_tags):
        self.labelling_client = labelling_client
        self.all_anomalies = all_anomalies
        self.own_hostnames = own_hostnames
        self.timeplan = None
        self.last_parameters = {}
        self.current_mode = "manual"
        self.mode_condition = threading.Condition()
        self.set_tags = set_tags

    def assert_correct_timeplan_anomaly(self):
        my_items = []
        remote_items = []
        for item in self.timeplan.all_current_items():
            if item is None: continue
            if item.is_global or item.target_host in self.own_hostnames:
                my_items.append(item)
            else:
                remote_items.append(item)

        tag = target = anomaly_name = parameters = None
        if len(my_items) >= 1:
            item = my_items[0]
            tag = item.tag
            target = item.target_host
            anomaly_name = item.anomaly_name
            parameters = item.parameters
            if len(my_items) > 1:
                logging.warn("Multiple items found for host(s) %s: %s. Only choosing %s for %s" % (self.own_hostname, my_items, tag, target))

        if self.label_data(tag, target, remote_items):
            self.assert_anomaly(anomaly_name, parameters)

    def label_data(self, tag, target, remote_items):
        retries = self.SHUTTING_DOWN_RETRIES if self.shutting_down else -1
        if tag is None and len(remote_items) == 0:
            return self.labelling_client.reset_label(retries)
        if len(remote_items) > 0:
            if tag is not None:
                logging.warn("Both local and remote anomalies detected. Local: %s, remote: %s" % (tag, remote_items))
            elif len(remote_items) > 1:
                logging.warn("Multiple remote anomalies detected: %s" % remote_items)
        return self.labelling_client.set_label(self.http_tag_timeout, tag, target, remote_items, retries)

    def assert_anomaly(self, anomaly_name, parameters):
        param_description = "no parameters" if (parameters is None or parameters == "") else "parameters " + parameters
        current = self.current_running_anomalies()
        if len(current) == 0:
            if anomaly_name is not None:
                logging.info("No anomaly is running, now injecting %s" % anomaly_name)
                self.inject_anomaly_name(anomaly_name, parameters)
            else:
                logging.info("No anomaly running as expected")
        elif len(current) == 1:
            current_anomaly = current[0]
            current_parameters = self.last_parameters[current_anomaly.name]
            if self.correctAnomalyRunning(current_anomaly, current_parameters, anomaly_name, parameters):
                logging.info("Anomaly %s (%s) is running as expected" % (anomaly_name, current_parameters))
            else:
                logging.info("Anomaly %s (%s) is running instead of %s (%s)... Reverting the first, injecting the second." % (current_anomaly, current_parameters, anomaly_name, parameters))
                if self.revert_anomaly(current_anomaly):
                    self.inject_anomaly_name(anomaly_name, parameters)
                else:
                    logging.warn("Failed to revert %s, not injecting %s for now..." % (current_anomaly, anomaly_name))
        else:
            logging.warn("Multiple anomalies are running: %s. Reverting them all, then injecting %s" % (current, anomaly_name))
            success = True
            for anomaly in current:
                success = success and self.revert_anomaly(anomaly)
            if success:
                self.inject_anomaly_name(anomaly_name, parameters)
            else:
                logging.warn("Failed to revert at least one of the anomalies, not injecting %s for now..." % (anomaly_name))

    def current_running_anomalies(self):
        current = []
        for anomaly in self.all_anomalies:
            injected = self.check_anomaly_status(anomaly)
            if injected:
                current.append(anomaly)
            else:
                self.last_parameters.pop(anomaly.name, None)
        return current

    def correctAnomalyRunning(self, current_anomaly, current_parameters, expected_name, expected_parameters):
        if expected_name is None and current_anomaly is None:
            return True
        return current_anomaly.name == expected_name and current_parameters == expected_parameters

    def lookup_anomaly(self, name):
        if name is None:
            return None
        for anomaly in self.all_anomalies:
            if anomaly.name == name:
                return anomaly
        return None

    def inject_anomaly_name(self, name, parameters):
        if name is None:
            return
        anomaly = self.lookup_anomaly(name)
        if anomaly is None:
            logging.warn("Could not find anomaly with name %s, not injecting anything!" % name)
            return
        return self.inject_anomaly(anomaly, parameters)

    def check_anomaly_status(self, anomaly):
        # TODO add timeout for this invokation and in inject_anomaly(), revert_anomaly()
        try:
            return anomaly.status()
        except KeyboardInterrupt as err:
            raise err
        except:
            logging.exception("Failed to check status of anomaly %s, assuming not running..." % anomaly.name)
            return False

    def inject_anomaly(self, anomaly, parameters, time, prevent_set_tags=None):
        self.last_parameters[anomaly.name] = parameters
        try:
            new_parameters = self.replace_parameter_template(parameters)
            if parameters != new_parameters:
                logging.info("Replaced parameters for %s: %s" % (anomaly, new_parameters))
            anomaly.inject(new_parameters, time)
            if self.check_anomaly_status(anomaly):
                if self.set_tags and not prevent_set_tags:
                    print("Setting Injector Tags.")
                    try:
                        self.labelling_client.reset_label(retries=1)
                        self.labelling_client.set_label(3, anomaly.name, self.own_hostnames[0], retries=1)
                    except:
                        logging.exception("Failed to set tags.")
                return True
            else:
                logging.warn("Anomaly %s injected successfully but is reporting that it's not running!" % anomaly.name)
        except KeyboardInterrupt as err:
            raise err
        except:
            logging.exception("Failed to inject anomaly %s" % (anomaly.name))
        return False

    def revert_anomaly(self, anomaly):
        try:
            anomaly.revert()
            if not self.check_anomaly_status(anomaly):
                if self.set_tags:
                    try:
                        self.labelling_client.reset_label(retries=1)
                        self.labelling_client.set_label(3, "load", None, retries=1)
                    except:
                        logging.exception("Failed to set tags.")
                return True
            else:
                logging.exception("Anomaly %s reverted successfully but is reporting that it's still running!" % (anomaly.name))
        except KeyboardInterrupt as err:
            raise err
        except:
            logging.exception("Failed to revert anomaly %s, retrying in %s seconds..." % (anomaly.name, self.revert_retry))
        return False

    regex = re.compile(re.escape("{{") + "(?P<start>[0-9]+) (?P<end>[0-9]+)" + re.escape("}}")) # {{123 456}}

    def replace_parameter_template(self, parameters):
        def repl(match):
            start = int(match.group("start"))
            end = int(match.group("end"))
            return str(random.randint(start, end))
        return self.regex.sub(repl, parameters)

    def register_atexit_cleanup(self):
        atexit.register(self.atexit_cleanup)

    def atexit_cleanup(self):
        logging.info(">>>>>>>>> Shutting down...")
        self.timeplan = None
        self.shutting_down = True
        self.labelling_client.reset_label(self.SHUTTING_DOWN_RETRIES)
        self.assert_anomaly(None, "")

    def set_mode(self, mode):
        if self.current_running_anomalies():
            return False

        self.current_mode = mode
        cv = self.mode_condition
        if mode == "timeplan":
            with cv:
                cv.notifyAll()
        logging.info("Changed injection mode to %s" % mode)
        return True

    def set_timeplan(self, timeplan):
        self.timeplan = Timeplan.fromCsv(timeplan)
        logging.info("Timeplan set to:")
        print(self.timeplan)


class DataLabellingClient(object):

    def __init__(self, host, port):
        self.url = "http://%s:%s/api/tags" % (host, port)
        print("Collector client" + self.url)
        self.retry_timeout = 3 # Can be modified

    # Return True if successful
    def request(self, timeout, tags, retries=-1, delete=False):
        url = self.url
        first = True
        for tag, value in tags.items():
            from urllib.parse import quote_plus as quote
            if first:
                url += "?%s=%s" % (quote(str(tag)), quote(str(value)))
                first = False
            else:
                url += "&%s=%s" % (quote(str(tag)), quote(str(value)))
            print("Target URL " + url)
        while True:
            if retries >= 0:
                if retries == 0:
                    logging.info("WARNING: maximum number of HTTP retries exceeded, failed to set anomaly to %s (timeout %s)" % (anomaly, timeout))
                    break
                retries -= 1
            try:
                if delete:
                    res = requests.delete(url)
                else:
                    res = requests.post(url)
                if res.status_code == 200:
                    logging.debug("HTTP POST %s success, response: %s" % (url, res.text))
                    return True
                else:
                    logging.info("HTTP POST %s failed. Status: %s, response: %s" % (url, str(res.status_code), res.text))
            except requests.exceptions.ConnectionError as err:
                logging.info(str(err))
            except KeyboardInterrupt as err:
                raise err
            except:
                logging.exception("Error POSTing %s" % (url))
            logging.info("Sleeping %s seconds until retry..." % (str(self.retry_timeout)))
            if retries != 0:
                time.sleep(self.retry_timeout)
        return False

    def set_label(self, timeout, label, hostname, other_injections=dict(), retries=-1):
        tags = {}
        num = 0
        if label is not None:
            if hostname is None:
                tags["cls"] = label
            else:
                tags["target"] = "%s|%s" % (hostname, label)
                num += 1
        for injection in other_injections:
            host, anomaly = injection.target_host, injection.tag
            key = "target" + ("" if num == 0 else str(num))
            tags[key] = "%s|%s" % (host, anomaly)
            num += 1
        return self.request(timeout, tags, retries)

    def reset_label(self, retries=-1):
        return self.request(0, dict(), retries, delete=True)

    def __str__(self):
        return "Data Labelling (%s)" % (self.url)

symbolic_names = { \
    "VNF": ".+@.+", \
    "VM": "^[^@]+\.ims4$|^[^@]+.+balancer.+|^[^@]+.+video.+|^CORD.+", \
    "PHYS": "^wally...$", \
    "HOST": "^([^@]+\.ims4)|(wally...)$" \
}

def get_all_anomalies():
    return [\
        NoopAnomaly("noop"),\
        CPUStressAnomaly("python_cpu"),\
        ProcessAnomaly("mem_leak", "anomalies/mem-alloc/leak"),\
        ProcessAnomaly("vnf_mem_leak", "anomalies/mem-alloc/vnf_leak"),\
        ProcessAnomaly("stress", "anomalies/binaries/stress"),\
        ProcessAnomaly("vnf_stress", "anomalies/binaries/vnf_stress"),\
        ProcessAnomaly("download", "anomalies/download/download.sh"),\
        ProcessAnomaly("vnf_download", "anomalies/download/vnf_download.sh"),\
        TrafficControlAnomaly.PacketLoss("packet_loss"),\
        TrafficControlAnomaly.Bandwidth("bandwidth"),\
        TrafficControlAnomaly.Latency("latency"),\
    ]

def get_all_parameterized_anomalies(hostname, api_port):
    parameterized = anomaly_list.get_parametrized_anomalies()

    result = []
    for name, anomalyList in parameterized.items():
        for anomaly in anomalyList:
            if anomaly.allowed is None:
                anomaly.anomaly.default_parameters = anomaly.default_parameters
                result.append(anomaly.anomaly)
                break
            else:
                regex = re.compile(anomaly.allowed)
                if regex.match(hostname) is not None:
                    print(anomaly.anomaly.name + ": " + anomaly.anomaly.default_parameters)

                    anomaly.anomaly.default_parameters = anomaly.default_parameters
                    result.append(anomaly.anomaly)
                    break
    return result

class ParameterizedAnomaly(object):
    def __init__(self, anomaly, allowed, default_parameters):
        self.anomaly = anomaly
        self.allowed = allowed
        self.default_parameters = default_parameters


class TimeplanThread(threading.Thread):

    def __init__(self, engine, interval):
        threading.Thread.__init__(self)
        self.engine = engine
        self.interval = interval

    def run(self):
        cv = self.engine.mode_condition
        while True:
            with cv:
                cv.wait()
                while self.engine.current_mode == "timeplan":
                    self.engine.assert_correct_timeplan_anomaly
                    time.sleep(self.interval)
                logging.info("Timeplan mode stopped")

def injector_service_main(args):
    global symbolic_names
    logging_config.configure_logging()
    plan = None

    parser = argparse.ArgumentParser()
    parser.add_argument("-host", dest='own_host', required=True, help="Own hostname.")
    parser.add_argument("-delay", type=int, default=0, help="The scenario will start this many seconds from now")
    parser.add_argument("-start", type=parse_date_parameter, default=None, help="Specify a datetime (%s) or time (%s) when to start the scenario" %
                                (TimeplanItem.time_format.replace("%", ""), TimeplanItem.duration_format.replace("%", "")))
    parser.add_argument("-scenario", dest='scenario', type=str, required=False, help="Scenario CSV file")
    parser.add_argument("-cooldown", type=positive_int, default=30, help="Number of seconds after each injected anomaly")
    parser.add_argument("-collector-host", dest='collector_host', type=str, default="localhost", help="Host of data-collection-agent for configuring running anomaly")
    parser.add_argument("-collector-port", dest='collector_port', type=int, default=7777, help="Port of data-collection-agent for configuring running anomaly")
    parser.add_argument("-all-hosts", dest='all_hosts', type=str, help="Comma separated list of all hosts")
    parser.add_argument("-interval", type=positive_int, default=3, help="Number of seconds between asserting that the correct anomaly is running")
    parser.add_argument("-loop-scenario", dest='loop_scenario', action='store_true', help="Let the scenario loop indefinitely")
    parser.add_argument("-force-future-start", dest='force_future', action='store_true', help="Fail if the scenario starts in the future")
    parser.add_argument("-simulate", action='store_true', help="Do not execute the anomalies, but show which host should be injected at each time")
    parser.add_argument("-pre-inject-sleep", dest="pre_inject_sleep", type=positive_int, default=0, help="Make certain anomalies sleep before the actual injection")
    parser.add_argument("-api-port", dest="api_port", type=positive_int, default=7888, help="Port on which the REST API should be running")
    parser.add_argument("-set-collector-tags", dest="set_tags", type=bool, default=False, help="Set and reset local target tags on each injection")
    args = parser.parse_args()

    if args.scenario:
        plan = Timeplan.fromCsv(args.scenario)
        if args.start is None:
            if args.force_future:
                raise ValueError("-delay must be positive: %s" % args.delay)
            plan.set_start_time_future(datetime.timedelta(seconds=args.delay))
        else:
            if args.delay != 0:
                raise ValueError("Cannot specify both -delay and -start")
            if args.force_future and args.start < datetime.datetime.now():
                raise ValueError("Specified -start time is already in the past: %s" % args.start)
            plan.set_start_time(args.start)
        if args.loop_scenario:
            plan.loop = True
        print(plan)
    if args.all_hosts is not None:
        if plan == None:
            raise(ValueError("The -all-hosts parameter only works in combination with -scenario.\n Please provide -scenario:"))
        plan.extend_for_hosts(args.all_hosts.split(","), datetime.timedelta(seconds=args.cooldown), symbolic_names, "FILLER")
        if len(plan.plan) == 0:
            raise(ValueError("The given list of all hosts did not match any item in the plan: %s" % args.all_hosts))
    if args.pre_inject_sleep > 0:
        anomaly.pre_inject_sleep_millis = args.pre_inject_sleep

    client = DataLabellingClient(args.collector_host, args.collector_port)
    # all_anomalies = get_all_anomalies()
    all_anomalies = get_all_parameterized_anomalies(args.own_host, args.api_port)
    logging.info("Available anomalies: %s" % [ x.name for x in all_anomalies ] )

    if args.simulate:
        logging.info("Simulating which host should be injected...")
        while True:
            items = plan.getAllCurrentItems()
            if len(items) == 0:
                logging.info("Nothing injected")
            else:
                for i in items:
                    logging.info("Injected now: [" + str(i.index) + "] " + i.print_in_current_iteration(plan))
            time.sleep(args.interval)
    else:
        engine = AnomalyEngine(client, all_anomalies, [args.own_host], args.set_tags)
        if args.scenario:
            engine.timeplan = plan
        engine.register_atexit_cleanup()
        timeplan_thread = TimeplanThread(engine, args.interval)
        timeplan_thread.start()
        logging.info("Starting REST API#")
        app = create_app(engine)
        app.run(host='0.0.0.0', port=args.api_port, threaded=True)


def parse_date_parameter(arg):
    try:
        t = datetime.datetime.strptime(arg, TimeplanItem.duration_format)
        return datetime.datetime.combine(datetime.datetime.now(), t.time())
    except ValueError:
        try:
            return datetime.datetime.strptime(arg, TimeplanItem.time_format)
        except ValueError:
            pass
    raise argparse.ArgumentTypeError("Not a valid date or time: %s" % arg)


def positive_int(arg):
    ivalue = int(arg)
    if ivalue <= 0:
         raise argparse.ArgumentTypeError("Must be positive: %s" % arg)
    return ivalue

if __name__ == "__main__":
    import sys

    injector_service_main(sys.argv[1:])
