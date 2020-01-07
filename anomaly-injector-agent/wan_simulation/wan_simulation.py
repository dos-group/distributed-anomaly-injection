import logging
import time
from threading import Thread

class WanSimulation(object):
    def __init__(self, interface):
        self.interface = interface
        self.parameters = []
        self.running = False

    def start(self, params, time):
        raise NotImplementedError("Abstract method")

    def stop(self):
        raise NotImplementedError("Abstract method")

    def status(self):
        raise NotImplementedError("Abstract method")

    def is_running(self):
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
        return self.interface