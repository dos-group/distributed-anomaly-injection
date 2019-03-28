
import csv, copy, logging, re
from datetime import datetime, timedelta

class TimeplanItem(object):
	time_format = "%Y-%m-%d %H:%M:%S"
	duration_format = "%H:%M:%S"

	def __init__(self, start_time, duration, target_host, tag, anomaly_name, parameters):
		self.start_time = datetime.strptime(start_time, self.time_format)
		t = datetime.strptime(duration, self.duration_format)
		self.duration = timedelta(hours=t.hour, minutes=t.minute, seconds=t.second)
		self.target_host = target_host
		self.anomaly_name = anomaly_name
		self.parameters = parameters
		self.tag = tag
		self.is_global = False
		self.index = -1

	@staticmethod
	def fromDict(d):
		return TimeplanItem(d["start"], d["duration"], d["target"], d["tag"], d["anomaly"], d["parameters"])

	def format(self, time):
		return time.strftime(self.time_format)

	def end_time(self):
		return self.start_time + self.duration

	def format_duration(self, dur):
		hours, rem = divmod(dur.seconds, 3600)
		minutes, seconds = divmod(rem, 60)
		hours += dur.days * 24
		return "%.2i:%.2i:%.2i" % (hours, minutes, seconds)

	def clone(self):
		return copy.copy(self)

	def str_items(self):
		target = "all" if self.is_global else self.target_host
		return [ self.tag, target, self.anomaly_name, self.parameters, self.format(self.start_time), self.format(self.end_time()), self.format_duration(self.duration) ]

	def __str__(self):
		items = self.str_items()
		if len(items[3]) > 0:
			items[3] = ", " + items[3]
		return "%s on %s (%s%s) %s - %s (%s)" % tuple(items)

	def print_in_current_iteration(self, plan):
		items = self.str_items()
		if len(items[3]) > 0:
			items[3] = ", " + items[3]
		start_time = plan.move_to_current_iteration(self.start_time)
		items[4] = self.format(start_time)
		end = start_time + self.duration
		items[5] = self.format(end)
		items.append(self.format_duration(end - datetime.now()))
		return "%s on %s (%s%s) %s - %s (%s total, %s left)" % tuple(items)

# Contains a list of TimeplanItems
class Timeplan(object):
	def __init__(self):
		self.plan = []
		self.loop = False

	@staticmethod
	def fromCsv(filename):
		t = Timeplan()
		with open(filename, "r") as f:
			t.parseCsv(f)
		return t

	def parseCsv(self, csv_text):
		reader = csv.DictReader(csv_text)
		for row in reader:
			item = TimeplanItem.fromDict(row)
			self.plan.append(item)
		self.plan.sort(key = lambda i: i.start_time)
		for i, item in enumerate(self.plan):
			item.index = i

	def current_items(self, hostname):
		return [ item for item in self.get_current_items() if item.target_host == hostname ]

	def all_current_items(self):
		now = self.current_time()
		return [ item for item in self.plan if item.start_time <= now and item.end_time() > now ]

	def current_time(self):
		now = datetime.now()
		if self.loop:
			projected = self.move_within_timerange(now)
			logging.debug("Projecting current time %s to %s" % (now, projected))
			now = projected
		return now

	def move_within_timerange(self, a_date):
		# Move a_date to inside the timeplan
		start = self.start_time()
		end = self.end_time()
		length = end - start
		offset = (a_date - start) % length
		return start + offset

	def move_to_current_iteration(self, a_date):
		# Move a_date to inside the current instance of the timeplan, if it were looped
		start = self.start_time()
		end = self.end_time()
		if a_date < start or a_date > end:
			raise ValueError("a_date (%s) must be within start/end of timeplan (%s - %s)" % (a_date, start, end))
		length = end - start
		now = datetime.now()
		now_offset = (now - start) % length
		offset = a_date - start
		return now - now_offset + offset

	def getAllCurrentItems(self):
		now = self.current_time()
		items = []
		for item in self.plan:
			if item.start_time > now:
				continue
			if item.end_time() < now:
				continue
			items.append(item)
		return items

	def __str__(self):
		from tabulate import tabulate
		intro = "Timeplan with %s item(s), %s - %s (duration %s):\n" % (len(self.plan), self.start_time(), self.end_time(), self.end_time() - self.start_time())
		tab = [ p.str_items() for p in self.plan ]
		headers = [ "Nr", "Label", "Target", "Anomaly", "Parameters", "Start", "End", "Duration" ]
		return intro + tabulate(tab, headers=headers, showindex="always") + ("\n... repeated indefinitely" if self.loop else "")

	def start_time(self):
		earliest = None
		if len(self.plan) == 0:
			raise ValueError("plan is empty")
		for item in self.plan:
			if earliest is None or item.start_time < earliest:
				earliest = item.start_time
		return earliest

	def end_time(self):
		latest = None
		for item in self.plan:
			end = item.end_time()
			if latest is None or end > latest:
				latest = end
		return latest

	def set_start_time(self, start):
		delta = start - self.start_time()
		for item in self.plan:
			item.start_time += delta

	def set_start_time_future(self, delta):
		self.set_start_time(datetime.now() + delta)

	# Replicate items in this plan for the given list of all hosts.
	# Treat target_host field in the items as regexes and replicate every item for all hosts matching that regex.
	# The old items are removed in that process.
	# Extras:
	# - cooldown_time is added as padding between each item in the new plan
	# - symbolic_names can contain abbreviations for regexes that will be used when a target_host field starts with the dollar sign.
	#   For example, the map { "XXX": "^hello1*$" } will apply all items with target_host="$XXX" to hosts matching "^hello1*$"
	# - All items with target_host=filler_host will be used as additional padding between all items generated from the item right before the filler_host item.
	#   These additional padding items will be marked as global, i.e. they apply to every host.
	def extend_for_hosts(self, all_hosts, cooldown_time=None, symbolic_names={}, filler_host=None):
		class Builder:
			def __init__(bldr, timeplan, cooldown_time):
				bldr.cooldown_time = cooldown_time
				bldr.start = timeplan.start_time()
				bldr.plan = timeplan.plan
				bldr.new_plan = []
				bldr.index = 0

			def clone_item(bldr, item, target_host, is_global=False):
				new = item.clone()
				new.target_host = target_host
				new.start_time = bldr.start
				new.is_global = is_global
				new.index = bldr.index
				bldr.index += 1
				bldr.new_plan.append(new)
				bldr.start += new.duration
				if bldr.cooldown_time != None:
					bldr.start += bldr.cooldown_time

			def build(bldr, all_hosts, symbolic_names, filler_host):
				for i, item in enumerate(bldr.plan):
					if item.target_host == filler_host:
						# Filler items are only used in previous ALLHOSTS items
						continue
					else:
						# Replicate item for all matching hosts, optionally filling up with follow-up FILLER item
						filler_item = None
						if len(bldr.plan) > i + 1 and bldr.plan[i + 1].target_host == filler_host:
							filler_item = bldr.plan[i + 1]

						# Replace the symbolic name with the regex defined in the symbolic_names map
						target = item.target_host
						if target.startswith("$"):
							symbol = target[1:]
							if symbol in symbolic_names:
								target = symbolic_names[symbol]
							else:
								raise ValueError("Symbolic name %s not found in given map of symbolic names: %s" % (symbol, symbolic_names))

						for host in all_hosts:
							if re.match(target, host):
								bldr.clone_item(item, host)
								if filler_item is not None:
									bldr.clone_item(filler_item, None, is_global=True)
				return bldr.new_plan

		# Use the local class to create the new, extended plan
		self.plan = Builder(self, cooldown_time).build(all_hosts, symbolic_names, filler_host)

if __name__ == "__main__":
	plan = Timeplan.fromCsv("example.csv")
	plan.set_start_time_future(timedelta(hours=1))
	print(plan)
