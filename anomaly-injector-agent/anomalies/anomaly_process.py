import shlex, subprocess, logging, psutil
from anomalies.anomaly import Anomaly
from time import sleep


class ProcessAnomaly(Anomaly):
    def __init__(self, name, command, always_set_params="", termination_routine=None):
        super(ProcessAnomaly, self).__init__(name)
        self.process = None
        self.command = command
        self.always_set_params = always_set_params
        self.termination_routine = termination_routine

    def inject(self, parameters, auto_revert_time):
        cmdline = [self.command]
        cmdline.extend(shlex.split(self.always_set_params))
        cmdline.extend(shlex.split(parameters))
        self.pre_inject_sleep()
        self.start(cmdline)
        self.running = True
        self.auto_revert(auto_revert_time)

    def revert(self):
        if self.process is None:
            logging.info("Warning: ProcessAnomaly.revert() called for %s, although process is not running!" % (self))
            return
        self.kill_pid(self.process.pid)
        self.process.wait()
        self.process = None
        self.running = False

    def status(self):
        if self.process is None:
            return False
        return self.process.poll() is None

    def start(self, cmdline):
        if self.process is not None:
            if self.status():
                logging.info("Warning: Process for anomaly %s is already running!" % (self))
            else:
                self.process = None
        self.process = subprocess.Popen(cmdline)
        logging.info("Process started: %s" % self.process)

    def kill_pid(self, pid):
        process = psutil.Process(pid)
        children = process.children(recursive=True)
        logging.info("Killing: " + str(pid) + "Children: " + str(children))
        try:
            for proc in children:
                proc.kill()
            if process:
                process.kill()
        except psutil.NoSuchProcess:
            print("Process no longer exists.")
        self.on_termination()

    def on_termination(self):
        if self.termination_routine is not None:
            self.termination_routine.on_termination()


class ProcessAnomalyTerminating(ProcessAnomaly):
    def __init__(self, name, command, always_set_params="", termination_routine=None):
        super(ProcessAnomalyTerminating, self).__init__(name, command, always_set_params, termination_routine)

    def kill_pid(self, pid):
        process = psutil.Process(pid)
        children = process.children(recursive=True)
        try:
            for proc in children:
                proc.terminate()
            process.terminate()
        except psutil.NoSuchProcess:
            print("Process no longer exists.")
        self.on_termination()


class AnomalyOnTermination(object):
    def on_termination(self):
        raise NotImplementedError("Abstract method. Needs to be implemented.")


class ClearTempDirectories(AnomalyOnTermination):
    def __init__(self, path_wildcard):
        super(ClearTempDirectories, self).__init__()
        self.path_wildcard = path_wildcard

    def on_termination(self):
        import glob, shutil
        dirs = glob.glob(self.path_wildcard)
        for dir in dirs:
            shutil.rmtree(dir)
