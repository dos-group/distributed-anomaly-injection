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

    def __str__(self):
        return self.interface