import threading, requests, json, time, sys, signal


class AnomalyTrigger(threading.Thread):
    #URL path to anomaly REST API
    ANOMALY_URL = "/api/anomalies/"
    #JSON identifier names in payload
    ANMLY_NAME = "name"
    ANMLY_PARAMETER = "parameter"
    ANMLY_PAYLOAD = "payload"
    ANMLY_TERMINATE = "terminate"
    ANMLY_DELAY = "delay"
    ANMLY_DELAY_FINISHED = "delay_finished"
    #Own name
    ANMLY_SELF = "root_cause"

    def __init__(self, target_host, anomalies, interval=1000):
        self.running = 1
        self.terminated = 0
        self.target_host = target_host + self.ANOMALY_URL
        self.anomalies = anomalies
        self.interval = interval
        super(AnomalyTrigger, self).__init__()

    def stop(self):
        """
        Triggers regular stopping of the running thread.
        If enabled, triggers terminating of remote anomalies.
        """
        self.running = 0
        for anomaly in self.anomalies:
            #Check if anomaly was already triggered
            if self.__check_anomaly_delay_finished(anomaly):
                #Check if triggered anomaly should be terminated
                if self.ANMLY_TERMINATE in anomaly:
                    if anomaly[self.ANMLY_TERMINATE]:
                        requests.delete(self.target_host + anomaly[self.ANMLY_NAME] + "/")

    def run(self):
        """
        Performs cyclic triggering of remote anomalies.
        Allows recursive triggering of another root_cause anomaly.
        Supports initial delay for each anomaly.
        """
        #Initial delay
        self.__check_anomalies_for_delay()
        while self.running or not self.terminated:
            for anomaly in self.anomalies:
                if self.running:
                    #Trigger only if initial delay is finished
                    if self.__check_anomaly_delay_finished(anomaly):
                        if anomaly[self.ANMLY_NAME] == self.ANMLY_SELF:
                            self.__handle_recursion(anomaly[self.ANMLY_PAYLOAD])
                        else:
                            requests.post(self.target_host + anomaly[self.ANMLY_NAME] + "/", \
                                          json=anomaly[self.ANMLY_PAYLOAD])
                else:
                    self.terminated = 1
                    break
            time.sleep(self.interval / 1000)

    def __check_anomaly_delay_finished(self, anomaly):
        """
        Checks whether the initial delay for an anomaly was already performed.

        Args:
            anomaly:
                Anomaly which should be checked.

        Returns:
            true if the deleay for the given anomaly is finished.
            false otherwise.
        """
        if self.ANMLY_DELAY_FINISHED in anomaly:
            if anomaly[self.ANMLY_DELAY_FINISHED]:
                return True
        return False

    def __handle_recursion(self, payload):
        """
        Handles recursive triggering of another root_cause anomaly.

        Args:
            payload:
                Payload parameter for the roo_cause which should be
                triggered.
        """
        p = {self.ANMLY_PARAMETER:json.dumps(payload[self.ANMLY_PARAMETER])}
        requests.post(self.target_host + self.ANMLY_SELF + "/", \
                      json=p)

    def __check_anomalies_for_delay(self):
        """
        Checks and executes the delay of each anomaly.
        Each anomaly's delay is handled in own thread.
        Each thread looks out for termination of parent.
        """
        def waiting_loop(root, anomaly):
            time_counter = 0
            print(anomaly[root.ANMLY_DELAY])
            while time_counter < anomaly[root.ANMLY_DELAY] and root.running:
                time.sleep(0.1)
                time_counter = time_counter + 100
            anomaly[root.ANMLY_DELAY_FINISHED] = True

        for anomaly in self.anomalies:
            if self.ANMLY_DELAY in anomaly:
                t = threading.Thread(target=waiting_loop, args=(self, anomaly))
                t.start()
            else:
                anomaly[self.ANMLY_DELAY_FINISHED] = True


if __name__ == "__main__":
    anomaly_trigger = []
    #Signal handler for SIGINT and SIGTERM
    def signal_handler(signal, frame):
        for anomaly in anomaly_trigger:
            anomaly.stop()
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    #Process JSON argument
    data = json.loads(sys.argv[1])
    #Start own thread for each target
    for target in data["targets"]:
        at = AnomalyTrigger(target["host"], target["anomalies"])
        anomaly_trigger.append(at)
        at.start()

    for a in anomaly_trigger:
        a.join()