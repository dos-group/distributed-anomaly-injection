import subprocess, netifaces, logging
from anomalies.anomaly import Anomaly


class TrafficControlAnomaly(Anomaly):
    _ifaces = []
    _injected = False  # Set if no interfaces are injected, but inject() has been called... Pretend the injection happened.

    def __init__(self, name, inject_command, status_keyword):
        super(TrafficControlAnomaly, self).__init__(name)
        self.inject_command = inject_command
        self.status_keyword = status_keyword

    @staticmethod
    def PacketLoss(name):
        return TrafficControlAnomaly(name, "--loss", b"loss")

    @staticmethod
    def Bandwidth(name):
        return TrafficControlAnomaly(name, "--rate", b"rate")

    @staticmethod
    def Latency(name):
        return TrafficControlAnomaly(name, "--delay", b"delay")

    @staticmethod
    def PacketDuplicate(name):
        return TrafficControlAnomaly(name, "--duplicate", b"duplicate")

    @staticmethod
    def PacketCorruption(name):
        return TrafficControlAnomaly(name, "--corrupt", b"corrupt")

    @staticmethod
    def PacketReordering(name):
        return TrafficControlAnomaly(name, "--reordering", b"reorder")

    def networkInterfaceExists(self, name):
        try:
            netifaces.ifaddresses(name)
            return True
        except ValueError as exc:
            return False

    def splitParams(self, params):
        params = params.split(" ")
        self._ifaces = []
        for i, interface in enumerate(params):
            if i >= len(params) - 1:
                # The last parameter is always an extra parameter
                break
            if self.networkInterfaceExists(interface):
                self._ifaces.append(interface)
        self._ifaces = set(self._ifaces)
        logging.info("Found network interfaces: %s" % (self._ifaces))
        return params[len(params) - 1]

    def status(self):
        if len(self._ifaces) == 0:
            return self._injected
        for i in self._ifaces:
            output = subprocess.check_output(["tcshow", "--device", i])
            if self.status_keyword in output:
                return True
        return False

    def inject(self, params, auto_revert_time):
        param = self.splitParams(params)
        self.pre_inject_sleep()
        for i in self._ifaces:
            try:
                # Hack: tcset --reordering is only applied when used together with --delay
                if self.status_keyword == b"reorder":
                    subprocess.call(["tcset", "--device", i, "--delay", "1", self.inject_command, param,
                                     "--overwrite", "--direction", "outgoing"])
                    subprocess.call(["tcset", "--device", i, "--delay", "1", self.inject_command, param,
                                     "--direction", "incoming"])
                else:
                    subprocess.call(["tcset", "--device", i, self.inject_command, param,
                                     "--overwrite", "--direction", "outgoing"])
                    subprocess.call(["tcset", "--device", i, self.inject_command, param,
                                     "--direction", "incoming"])
            except:
                self.revert()
                raise Exception("Failed to inject anomaly")
        if len(self._ifaces) == 0:
            self._injected = True
        if self.status():
            self.running = True
            self.auto_revert(auto_revert_time)

    def revert(self):
        self._injected = False
        for i in self._ifaces:
            subprocess.call(["tcdel", "--device", i, "--all"])
        self.running = False


if __name__ == "__main__":
    import time

    nl = TrafficControlAnomaly.Latency()
    print(nl.status())
    nl.inject("enp0s25 50")
    print(nl.status())
    nl.revert()
    print(nl.status())

    nb = TrafficControlAnomaly.Bandwidth()
    print(nb.status())
    nb.inject("enp0s25 1000k")
    print(nb.status())
    time.sleep(50)
    nb.revert()
    print(nb.status())

    nb = TrafficControlAnomaly.PacketLoss()
    print(nb.status())
    nb.inject("enp0s25 10.0")
    print(nb.status())
    nb.revert()
    print(nb.status())
