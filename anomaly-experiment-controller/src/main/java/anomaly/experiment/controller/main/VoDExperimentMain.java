package anomaly.experiment.controller.main;

import anomaly.experiment.controller.objects.InjectorAgentController;
import anomaly.experiment.controller.utils.UnirestUtils;
import anomaly.experiment.controller.requests.RequestSender;
import anomaly.experiment.controller.utils.Config;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by fschmidt and alex on 27.08.17.
 */
public class VoDExperimentMain {

    static { Config.initializeLogger(); }
    private static final Logger logger = Logger.getLogger(VoDExperimentMain.class.getName());


    public static void main(String... args)
            throws IOException, InterruptedException {
        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(Level.INFO));
        //TODO: Implement needed arguments
        CommandLineParser parser = new DefaultParser();
        Options o = new Options();
        o.addOption(Option.builder("i").longOpt("inventory-file").hasArg().required().desc("YAML file containing anomaly experiment information.").build());
        o.addOption(Option.builder("t").hasArg().required().desc("Duration of the whole experiment. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days).").build());
        o.addOption(Option.builder("t-anomaly").hasArg().desc("Runtime of each anomaly. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days). Default is 5 minutes").build());
        o.addOption(Option.builder("t-load").hasArg().desc("Runtime of load between anomalies. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days). Default is 5 minutes").build());
        o.addOption(Option.builder("t-initial-load").hasArg().desc("Runtime of initial load. Time at beginning of experiment, where no anomalies will be triggered. Last character determines dimension. Possible dimensions: s, m, h, d (seconds, minutes, hours, days).").build());
        final CommandLine flags;
        try {
            flags = parser.parse(o, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        //Get duration of the whole experiment in milliseconds
        String tmp = flags.getOptionValue("t");
        long experimentDuration = getDurationInMS(
                Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        //Get duration of initial load
        long t_initialLoad = 0;
        if(flags.hasOption("t-initial-load")) {
            tmp = flags.getOptionValue("t-initial-load");
            t_initialLoad = getDurationInMS(
                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        }
        //Get duration of each injected anomaly
        long t_anomaly = 0;
        if(flags.hasOption("t-anomaly")) {
            tmp = flags.getOptionValue("t_anomaly");
            t_anomaly = getDurationInMS(Integer.parseInt(
                    tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        }
        //Get time between each anomaly injection
        long t_load = 0;
        if(flags.hasOption("t_load")) {
            tmp = flags.getOptionValue("t_load");
            t_load = getDurationInMS(Integer.parseInt(
                    tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        }

        //Initialize unirest API. Only one time.
        UnirestUtils.initUnirest();




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
//
//        timeSelector_anomaly = controller.new ConstantTimeSelector(, tmp.charAt(tmp.length() - 1)));
//        controller.setAnomalyTimeSelector(timeSelector_anomaly);
//
//        timeSelector_load = controller.new ConstantTimeSelector(, );
//        controller.setLoadTimeSelector(timeSelector_load);
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
    }

    private static long getDurationInMS(long duration, char dimension){
        TimeUnit t;
        switch(dimension){
            case 's':
                t = TimeUnit.SECONDS;
                break;
            case 'm':
                t = TimeUnit.MINUTES;
                break;
            case 'h':
                t = TimeUnit.HOURS;
                break;
            case 'd':
                t = TimeUnit.DAYS;
                break;
            default:
                throw new IllegalArgumentException("Character " + dimension + " is not a valid dimension.");
        }
        return t.toMillis(duration);

    }

    private static void setRequestControllerAtTargets(Map<String, List<InjectorAgentController>> targets, RequestSender controller) {
        for(List<InjectorAgentController> l : targets.values())
            for(InjectorAgentController t : l)
                t.setRequestController(controller);
    }
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
//    private static synchronized void shutdownHook(DistributedExperimentController controller,
//                                                  boolean fetchRemoteData)
//            throws IOException, InterruptedException {
//        if(controller != null)
//            controller.shutdown();
//        if(fetchRemoteData)
//            CommandExecuter.executeCommand(SCRIPT_FETCH_REMOTE_DATA);
//    }
}