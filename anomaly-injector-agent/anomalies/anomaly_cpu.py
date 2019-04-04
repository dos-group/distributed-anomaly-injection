import sys

sys.path.insert(0, 'utils')

from anomalies.anomaly import Anomaly
from anomalies.CPUStressMonitor import MonitorThread
from anomalies.CPUStressController import ControllerThread
from anomalies.CPUStressClosedLoopActuator import closedLoopActuator


class CPUStressAnomaly(Anomaly):
    _cpuLoad = 0.5
    _cpuCore = 0
    _samplingInterval = 0.1
    _threads = []

    def _parseParameters(self, parms):
        self._cpuLoad = float(parms.split(" ")[0])
        if self._cpuCore >= 1:
            print("check max cpu cores")
        # self._cpuCore = int(parms.split(" ")[1])
        self._cpuCore = 1

    def inject(self, params, auto_revert_time):
        self._parseParameters(params)
        for cpuCore in range(3):
            monitor = MonitorThread(cpuCore, self._samplingInterval)
            monitor.start()

            control = ControllerThread(self._samplingInterval)
            control.start()
            control.setCpuTarget(self._cpuLoad)

            actuator = closedLoopActuator(control, monitor, self._cpuCore, self._cpuLoad)
            actuator.start()

            self._threads.extend([monitor, control, actuator])
        self.running = True
        self.auto_revert(auto_revert_time)

    def revert(self):
        for i in self._threads:
            i.stop()
        for i in self._threads:
            i.join()
        self.running = False

    def status(self):
        status = True
        if not self._threads:
            return False

        for thread in self._threads:
            if not thread.isAlive():
                status = False

        return status


if __name__ == "__main__":
    import time

    cs = CPUStressAnomaly()
    print(cs.status())
    cs.inject("0.5 1")
    time.sleep(50)
    print(cs.status())
    cs.revert()
    print(cs.status())
