#Authors: Gaetano Carlucci
#         Giuseppe Cofano

import time
import threading

class closedLoopActuator(threading.Thread):
	"""
		Generates CPU load by tuning the sleep time
	"""
	def __init__(self, controller, monitor, cpu_core, target):
		self.controller = controller
		self.monitor = monitor
		self.target = target
		self.controller.setCpu(self.monitor.getCpuLoad())
		self.period = 0.05 # actuation period  in seconds
		self.start_time = time.time()
		self.is_running = True
		super(closedLoopActuator,self).__init__()

	def generate_load(self, sleep_time):
		interval = time.time() + self.period - sleep_time
		# generates some getCpuLoad for interval seconds
		while (time.time() < interval):
			pr = 213123  # generates some load
			pr * pr
			pr = pr + 1
		time.sleep(sleep_time) # controller actuation

	def stop(self):
		self.is_running = False

	def run(self):
		while self.is_running:
			cpu=self.monitor.getCpuLoad()
			self.controller.setCpu(cpu)
			sleep_time = self.controller.getSleepTime()
			self.generate_load(sleep_time)
		return sleep_time

	def run_sequence(self, sequence):       
		for cpuTarget in sequence:
			stepPeriod = time.time() + 4
			self.controller.setCpuTarget(cpuTarget)
			self.monitor.setCPUTarget(cpuTarget)
			while (time.time() < stepPeriod):
				self.controller.setCpu(self.monitor.getCpuLoad())
				sleep_time = self.controller.getSleepTime()
				self.generate_load(sleep_time)
				self.monitor.setSleepTime(sleep_time)
