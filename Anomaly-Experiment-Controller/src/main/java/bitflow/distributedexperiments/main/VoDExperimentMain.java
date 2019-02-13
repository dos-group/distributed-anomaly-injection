package bitflow.distributedexperiments.main;

/**
 * Created by fschmidt and alex on 27.08.17.
 */
public class VoDExperimentMain {

//    static { Config.initializeLogger(); }
//    private static final Logger logger = Logger.getLogger(VoDExperimentMain.class.getName());
//
//    private static final AnomalySetUp anomalySetup = new HorizontalPropagationAnomalySetup();
//
//    private static final String EXCLUDE_TARGETS[] = {
//            "wally193", "wally194"
//    };
//
//    private static final String INCLUDE_TARGETS[] = {
//            "wally194", "wally183", "video-server-10-balancer-2", "video-server-10-balancer-4",
//            "video-server-10-video-13", "video-server-10-video-14"
//    };
//
//    private static final String EXCLUDE_REGEX_TARGETS_VOD[] = {
//            "video-server-10-client-.*", ".*@.*"
//    };
//
//    private static final String EXCLUDE_REGEX_TARGETS_CW[] = {
//            "cw-etcd.*", ".*@.*", "cw-swarm_manager.*", "cw-ellis.*", "cw-sippstress.*",
//            "cw-cassandra.*"
//    };
//
//    private static final String EXCLUDE_REGEX_WALLIES[] = {
//            "wally.*"
//    };
//
//    private static final String INCLUDE_REGEX_TARGETS_CW[] = {
//            "cw-bono.*", "cw-sprout.*", "cw-homer.*", "cw-homestead.*", "cw-cassandra.*", "wally.*"
//    };
//
//    //Command script files
//    public static final String SCRIPT_SET_LOAD = "/home/alexander.acker/testbed-scenarios/Clearwater/Scripts/start_sipp_stress.sh";
//    public static final String SCRIPT_STOP_LOAD = "/home/alexander.acker/testbed-scenarios/Clearwater/Scripts/stop_sipp_stress.sh";
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
//        o.addOption(Option.builder("fetch_remote_data").desc("Fetch remote data after experiment. Default is false").build());
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
//        TargetNameFilter filter1 = new TargetNameFilter(new Filter.ExcludeNameSelection(EXCLUDE_TARGETS));
//        TargetNameFilter filter2 = new TargetNameFilter(new Filter.IncludeNameRegexSelection(INCLUDE_REGEX_TARGETS_CW));
//        TargetNameFilter filter3 = new TargetNameFilter(new Filter.ExcludeNameRegexSelection(EXCLUDE_REGEX_WALLIES));
//        collectorTargets = filter1.filterTargets(collectorTargets);
//        injectorTargets = filter1.filterTargets(injectorTargets);
//        collectorTargets = filter2.filterTargets(collectorTargets);
//        injectorTargets = filter2.filterTargets(injectorTargets);
//        //collectorTargets = filter3.filterTargets(collectorTargets);
//        injectorTargets = filter3.filterTargets(injectorTargets);
//
//        //vod setup
//        /*injectorTargets.remove(Target.GROUP_DEFAULT); collectorTargets.remove(Target.GROUP_DEFAULT);
//        injectorTargets.remove("vod-host-client"); collectorTargets.remove("vod-host-client");
//        injectorTargets.remove("vod-client"); collectorTargets.remove("vod-client");
//        injectorTargets.remove("vod-host-client"); collectorTargets.remove("vod-host-client");
//        injectorTargets.remove("vod-client"); collectorTargets.remove("vod-client");
//        injectorTargets.remove(""); collectorTargets.remove("");*/
//
//        //Initiate request controller (executes HTTP requests) and pass it to all targets
//        setRequestControllerAtTargets(collectorTargets, new RequestSender(3, 1000));
//        setRequestControllerAtTargets(injectorTargets, new RequestSenderJSON(3, 1000));
//
//        //Set filter for anomalies. Define a list of anomalies which should not be executed.
//        //setAnomalyFilterForInjectorTargets(injectorTargets, new Filter.ExcludeNameSelection(EXCLUDE_ANOMALIES));
//        /*setAnomalyFilterForInjectorTargets(injectorTargets, new Filter.IncludeNameSelection(INCLUDE_ANOMALIES_PHYS),
//                Target.LAYER_PHYSICAL);
//        setAnomalyFilterForInjectorTargets(injectorTargets, new Filter.IncludeNameSelection(INCLUDE_ANOMALIES_VM),
//                Target.LAYER_VM);*/
//
//        //Set custom defined anomalies
//        //setAnomaliesForInjectorTargets(injectorTargets);
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
//        //RunLoadChanging loadController = new RunLoadChanging(10, TimeUnit.MINUTES);
//        RunLoadChanging loadController = new RunLoad(experimentDuration);
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
//            //loadController.setFrequency(timeSelector_load.getTime());
//            //loadController.setTimeUnit(TimeUnit.MILLISECONDS);
//        }
//
//        //Set shutdown hook
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//            try {
//                logger.log(Level.INFO, "Shouting down ...");
//                shutdownHook(controller, flags.hasOption("f"));
//                logger.log(Level.INFO, "Shut down successful.");
//            } catch (InterruptedException | IOException e) {
//                logger.log(Level.SEVERE, "Proper shut down failed.");
//            }
//            }
//        });
//        //Start experiment
//        controller.startExperiment(stopTime, t_initialLoad, false);
//        shutdownHook(null, flags.hasOption("f"));
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
//                ((InjectorTarget) t).setAnomalies(
//                        anomalySetup.getAnomalies(t.getGroup()));
//        }
//    }
//
//    private static synchronized void shutdownHook(DistributedExperimentController controller,
//                                                  boolean fetchRemoteData)
//            throws IOException, InterruptedException {
//        if(controller != null)
//            controller.shutdown();
//        if(fetchRemoteData)
//            CommandExecuter.executeCommand(SCRIPT_FETCH_REMOTE_DATA);
//    }
}