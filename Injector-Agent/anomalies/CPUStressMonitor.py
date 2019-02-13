#Authors: Gaetano Carlucci
#         Giuseppe Cofano

import os, psutil
import threading
import time

class MonitorThread(threading.Thread):
	"""
	   Monitors the CPU status
	"""
	def __init__(self, cpu_core, interval):
		self.sampling_interval = interval; # sample time interval
		self.sample = 0.5; # cpu load measurement sample
		self.cpu = 0.5; # cpu load filtered
		self.running = 1; # thread status
		self.alpha = 1; # filter coefficient
		self.sleepTimeTarget = 0.03
		self.sleepTime = 0.03
		self.cpuTarget = 0.5
		self.cpu_core = cpu_core
		super(MonitorThread, self).__init__()
	
	def stop(self):
		self.running = 0 
	
	def getCpuLoad(self):
		return self.cpu

	def setSleepTimeTarget(self, sleepTimeTarget):
		self.sleepTimeTarget = sleepTimeTarget

	def setSleepTime(self, sleepTime):
		self.sleepTime = sleepTime

	def setCPUTarget(self, cpuTarget):
		self.cpuTarget = cpuTarget
		
	def run(self):
		start_time = time.time()
		p = psutil.Process(os.getpid())
		p.cpu_affinity([self.cpu_core]) #the process is forced to run only on the selected CPU
		while self.running:
			self.sample = p.cpu_percent(self.sampling_interval)
			self.cpu = self.alpha * self.sample + (1 - self.alpha)*self.cpu # first order filter on the measurement samples
