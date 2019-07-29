import logging
import time
from multiprocessing import Process
from threading import Thread

# TODO HACK this is used by a few anomalies to add an artificial sleep phase before the actual injection
pre_inject_sleep_millis = 0


class Anomaly(object):
    def __init__(self, name):
        self.name = name
        self.default_parameters = ""  # HACK used only by REST API
        self.running = False

    def inject(self, params, time):
        raise NotImplementedError("Abstract method")

    def revert(self):
        raise NotImplementedError("Abstract method")

    # Return true, if currently injected, false otherwise
    def status(self):
        raise NotImplementedError("Abstract method")

    def auto_revert(self, auto_revert_time):
        p = Thread(target=self.__auto_revert, args=(auto_revert_time,))
        p.start()

    def __auto_revert(self, auto_revert_time):
        if int(auto_revert_time) > 0:
            logging.info("Started auto-reverter. Reverting after " + auto_revert_time + " seconds")
            time.sleep(int(auto_revert_time))
            if self.running:
                logging.info("Auto-reverting...")
                self.revert()
                if not self.status():
                    logging.info("Auto-revert successful")

    def __str__(self):
        return self.name

    def pre_inject_sleep(self):
        global pre_inject_sleep_millis
        if pre_inject_sleep_millis > 0:
            sleep_time = pre_inject_sleep_millis / 1000.0
            logging.info("Sleeping for %s seconds...", sleep_time)
            import time
            time.sleep(sleep_time)

class NoopAnomaly(Anomaly):
    def __init__(self, name):
        super(NoopAnomaly, self).__init__(name)

    def inject(self, params, auto_revert_time):
        print("Noop injecting with params: " + params)
        self.running = True
        self.auto_revert(auto_revert_time)

    def revert(self):
        print("Noop reverting")
        self.running = False

    def status(self):
        return self.running