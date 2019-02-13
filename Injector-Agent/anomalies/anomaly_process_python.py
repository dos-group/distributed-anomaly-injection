import psutil
from anomalies.anomaly_process import ProcessAnomaly

class PythonProcessAnomaly(ProcessAnomaly):
    def __init__(self, name, command, python_command="python3"):
        super(PythonProcessAnomaly, self).__init__(name, command)
        self.python_command = python_command

    def inject(self, parameters, auto_revert_time):
        parameters = self.python_command + " " + parameters
        super(PythonProcessAnomaly, self).inject(parameters, auto_revert_time)

class RootCauseAnomaly(PythonProcessAnomaly):
    def __init__(self, name, command, python_command="python3"):
        super(RootCauseAnomaly, self).__init__(name, command, python_command=python_command)

    def inject(self, parameter, auto_revert_time):
        cmdline = [self.python_command, self.command, parameter]
        super(RootCauseAnomaly, self).start(cmdline)
        self.running = True
        self.auto_revert(auto_revert_time)

    def kill_pid(self, pid):
        process = psutil.Process(pid)
        try:
            process.terminate()
        except psutil.NoSuchProcess:
            print("Process no longer exists.")