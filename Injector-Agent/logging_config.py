
import logging

def configure_logging(level=logging.INFO, library_level=logging.INFO):
	logging.basicConfig(format='%(asctime)s %(message)s', level=level)
	#logging.basicConfig(filename='/var/tmp/injector.log', filemode='w', format='%(asctime)s %(message)s', level=level)
	logging.getLogger("requests").setLevel(library_level)
	logging.getLogger("urllib3").setLevel(library_level)
