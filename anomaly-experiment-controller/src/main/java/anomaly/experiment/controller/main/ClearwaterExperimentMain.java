//package bitflow.distributedexperiments.main;
//
//import DistributedExperimentController;
//import bitflow.distributedexperiments.TargetFactory;
//import RunLoad;
//import RunLoadInterface;
//import AnomalySetUp;
//import bitflow.distributedexperiments.main.AnomalyLibrary.RemediationPaper_Scenarios;
//import bitflow.distributedexperiments.objects.targets.InjectorTarget;
//import bitflow.distributedexperiments.objects.targets.Target;
//import RequestSender;
//import RequestSenderJSON;
//import Config;
//import UnirestUtils;
//import Filter;
//import bitflow.distributedexperiments.utils.filter.TargetNameFilter;
//import com.mashape.unirest.http.exceptions.UnirestException;
//import org.apache.commons.cli.*;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.LogManager;
//import java.util.logging.Logger;
//
///**
// * Created by fschmidt and alex on 27.08.17.
// */
//public class ClearwaterExperimentMain {
//
//    static { Config.initializeLogger(); }
//    private static final Logger logger = Logger.getLogger(ClearwaterExperimentMain.class.getName());
//
//    private static final AnomalySetUp anomalySetup = new RemediationPaper_Scenarios();
//
//    private static final String EXCLUDE_TARGETS[] = {
//            "wally193", ".*@.*", "cw-sippstress.*", "cw-etcd.*", "cw-swarm_manager.*"
//    };
//
//    private static final String INCLUDE_ANOMALY_TARGETS[] = {
//            "cw-bono_node-2.ims4", "cw-bono_node-0.ims4",
//            "cw-sprout_node-2.ims4", "cw-sprout_node-0.ims4",
//            "cw-chronos_node-0.ims4", "cw-homesteadprov_node-0.ims4",
//            "wally194", "wally195", "wally198"
//    };
//
//
//    //Command script files
//    private static final String SCRIPT_START_LOAD = "/home/alexander.acker/testbed-scenarios/Clearwater/Scripts/start_sipp_stress.sh";
//    private static final String SCRIPT_STOP_LOAD = "/home/alexander.acker/testbed-scenarios/Clearwater/Scripts/stop_sipp_stress.sh";
//
//
//    public static void main(String... args)
//            throws IOException, ParseException, InterruptedException, UnirestException {
//        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(Level.INFO));
//        //TODO: Implement needed arguments
//        CommandLineParser parser = new DefaultParser();
//        Options o = new Options();
//        o.addOption(Option.builder("d").longOpt("dependencies").hasArg().required().desc("json-file for reading injector and collector endpoint information").build());
//        o.addOption(Option.builder("experiment_duration").hasArg().required().desc("Duration of the whole experiment. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days).").build());
//        o.addOption(Option.builder("t_anomaly").hasArg().desc("Runtime of each anomaly. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days). Default is 5 minutes").build());
//        o.addOption(Option.builder("t_load").hasArg().desc("Runtime of load between anomalies. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days). Default is 5 minutes").build());
//        o.addOption(Option.builder("t_initial_load").hasArg().desc("Runtime of initial load. Time at beginning of experiment, where no anomalies will be triggered. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days).").build());
//        final CommandLine flags = parser.parse(o, args);
//
//        //Get duration of the whole experiment in milliseconds
//        String tmp = flags.getOptionValue("experiment_duration");
//        long experimentDuration = getDurationInMS(
//                Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
//        //Get duration of initial load
//        long t_initialLoad = 0;
//        if(flags.hasOption("t_initial_load")) {
//            tmp = flags.getOptionValue("t_initial_load");
//            t_initialLoad = getDurationInMS(
//                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
//        }
//
//        //Restart collector services and remove previously collected data
//        //not needed anymore since collector is able to start/stop file output and creates new file each time
////        CommandExecuter.executeCommand(SCRIPT_COLLECTOR_SERVICE, "restart");
////        CommandExecuter.executeCommand(SCRIPT_CLEAN_REMOTE_DATA);
//
//
//        //Initialize unirest API. Only one time.
//        UnirestUtils.initUnirest();
//
//        //Create targets for collectors and injectors
//        String dependencyModelFile = flags.getOptionValue("d");
//        TargetFactory targetFactory = new TargetFactory(new TargetFactory.GroupByHostname());
//        Map<String, List<Target>> collectorTargets = targetFactory.parseJSONHostsFileForCollectorTargets(dependencyModelFile);
//        Map<String, List<Target>> injectorTargets = targetFactory.parseJSONHostsFileForInjectorTargets(dependencyModelFile);
//
//        //Filter relevant targets
//        TargetNameFilter filter1 = new TargetNameFilter(new Filter.ExcludeNameRegexSelection(EXCLUDE_TARGETS));
//        TargetNameFilter filter_injector = new TargetNameFilter(new Filter.IncludeNameSelection(INCLUDE_ANOMALY_TARGETS));
//        collectorTargets = filter1.filterTargets(collectorTargets);
//        injectorTargets = filter1.filterTargets(injectorTargets);
//        injectorTargets = filter_injector.filterTargets(injectorTargets);
//
//        //Initiate request controller (executes HTTP requests) and pass it to all targets
//        setRequestControllerAtTargets(collectorTargets, new RequestSender(3, 1000));
//        setRequestControllerAtTargets(injectorTargets, new RequestSenderJSON(3, 1000));
//
//        //Set custom defined anomalies
//        setAnomaliesForInjectorTargets(injectorTargets);
//
//
//        //Calculate end of experiment
//        long stopTime = new Date().getTime() + experimentDuration;
//        //Initialize distributed experiment controller
//        final DistributedExperimentController controller = new DistributedExperimentController(
//                injectorTargets, collectorTargets, null);
//
//        //Set injector type
//        DistributedExperimentController.Injector injector =
//                controller.new RoundRobinInjector();
//        controller.setAnomalyInjector(injector);
//
//        //Load controller
//        RunLoadInterface loadController = new RunLoad(SCRIPT_START_LOAD, SCRIPT_STOP_LOAD, experimentDuration,
//                900, 1000);
//        controller.setLoadController(loadController);
//
//        //Create time selection for anomaly and load
//        DistributedExperimentController.TimeSelector timeSelector_anomaly;
//        DistributedExperimentController.TimeSelector timeSelector_load;
//        if(flags.hasOption("t_anomaly")) {
//            tmp = flags.getOptionValue("t_anomaly");
//            timeSelector_anomaly = controller.new ConstantTimeSelector(getDurationInMS(
//                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1)));
//            controller.setAnomalyTimeSelector(timeSelector_anomaly);
//        }
//        if(flags.hasOption("t_load")) {
//            tmp = flags.getOptionValue("t_load");
//            timeSelector_load = controller.new ConstantTimeSelector(getDurationInMS(
//                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1)));
//            controller.setLoadTimeSelector(timeSelector_load);
//        }
//
//        //Set shutdown hook
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//            try {
//                logger.log(Level.INFO, "Shouting down ...");
//                shutdownHook(controller);
//                logger.log(Level.INFO, "Shut down successful.");
//            } catch (InterruptedException | IOException e) {
//                logger.log(Level.SEVERE, "Proper shut down failed.");
//            }
//            }
//        });
//        //Start experiment
//        controller.startExperiment(stopTime, t_initialLoad, false);
//        shutdownHook(null);
//    }
//
//    private static long getDurationInMS(long duration, char dimension){
//        TimeUnit t;
//        switch(dimension){
//            case 's':
//                t = TimeUnit.SECONDS;
//                break;
//            case 'm':
//                t = TimeUnit.MINUTES;
//                break;
//            case 'h':
//                t = TimeUnit.HOURS;
//                break;
//            case 'd':
//                t = TimeUnit.DAYS;
//                break;
//            default:
//                throw new IllegalArgumentException("Character " + dimension + " is not a valid dimension.");
//        }
//        return t.toMillis(duration);
//
//    }
//
//    private static void setRequestControllerAtTargets(Map<String, List<Target>> targets, RequestSender controller) {
//        for(List<Target> l : targets.values())
//            for(Target t : l)
//                t.setRequestController(controller);
//    }
//
//    private static void setAnomalyFilterForInjectorTargets(List<Target> injectorTargets,
//                                                           Filter.NameFilter filter) {
//        for(Target t : injectorTargets)
//            ((InjectorTarget) t).setAnomalyNameFilter(filter);
//    }
//
//    private static void setAnomaliesForInjectorTargets(Map<String, List<Target>> injectorTargets) {
//
//        for(List<Target> l : injectorTargets.values()) {
//            for (Target t : l)
//                ((InjectorTarget) t).setAnomalyGroups(
//                        anomalySetup.getAnomalyGroups(t.getGroup()));
//        }
//    }
//
//    private static synchronized void shutdownHook(DistributedExperimentController controller)
//            throws IOException, InterruptedException {
//        if(controller != null)
//            controller.shutdown();
//    }
//}