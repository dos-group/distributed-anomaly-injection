package anomaly.experiment.controller.main;

import anomaly.experiment.controller.DistributedExperimentController;
import anomaly.experiment.controller.objects.AnomalyGroup;
import anomaly.experiment.controller.objects.CollectorAgentController;
import anomaly.experiment.controller.objects.Host;
import anomaly.experiment.controller.objects.InjectorAgentController;
import anomaly.experiment.controller.requests.RequestSender;
import anomaly.experiment.controller.requests.RequestSenderJSON;
import anomaly.experiment.controller.utils.UnirestUtils;
import anomaly.experiment.controller.utils.Config;
import org.apache.commons.cli.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by alex on 20.02.19.
 */
public class Main {

    static { Config.initializeLogger(); }
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(Level.INFO));
        CommandLineParser parser = new DefaultParser();
        Options o = new Options();
        o.addOption(Option.builder("i").longOpt("inventory-file").hasArg().required().desc(
                "YAML file containing anomaly experiment information.").build());
        o.addOption(Option.builder("t").hasArg().required().desc(
                "Duration of the whole experiment. Last character determines dimension. Possible dimensions: " +
                        "s, m, h, d (seconds, minutes, hours, days).").build());
        o.addOption(Option.builder("t-anomaly").hasArg().desc(
                "Runtime of each anomaly. Last character determines dimension. Possible dimensions:" +
                        " s, m, h, d (seconds, minutes, hours, days). Default is 5 minutes").build());
        o.addOption(Option.builder("t-load").hasArg().desc(
                "Runtime of load between anomalies. Last character determines dimension. Possible dimensions: " +
                        "s, m, h, d (seconds, minutes, hours, days). Default is 5 minutes").build());
        o.addOption(Option.builder("t-initial-load").hasArg().desc(
                "Runtime of initial load. Time at beginning of experiment, where no anomalies will be triggered. " +
                        "Last character determines dimension. Possible dimensions: " +
                        "s, m, h, d (seconds, minutes, hours, days).").build());
        o.addOption(Option.builder("c").longOpt("collector-endpoint").hasArgs().desc("Collector endpoint" +
                " used for documentation of injected anomalies. It is possible to define multiple endpoints.")
                .build());
        final CommandLine flags;
        try {
            flags = parser.parse(o, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        //Path to inventory file.
        String inventoryFilePath = flags.getOptionValue("i");

        //Get duration of the whole experiment in milliseconds
        String tmp = flags.getOptionValue("t");
        long experimentDuration = getDurationInMS(
                Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        //Calculate end of experiment
        long stopTime = new Date().getTime() + experimentDuration;
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
        //Collector endpoints used for documentation of anomaly injections
        String collectorEndpoints[] = null;
        if(flags.hasOption("c")) {
            collectorEndpoints = flags.getOptionValues("c");
        }

        //Initialize unirest API. Only one time.
        UnirestUtils.initUnirest();

        //Parse inventory file
        AnomalyScenario scenario;
        try {
            final InputStream input = new FileInputStream(new File(inventoryFilePath));
            Yaml yaml = new Yaml(new Constructor(AnomalyScenario.class));
            scenario = yaml.load(input);
            logger.log(Level.INFO, "Inventory file " + inventoryFilePath + " successfully read.");
        } catch (FileNotFoundException ex) {
            logger.severe("File " + inventoryFilePath + " does not exist.");
            return;
        } catch (Exception e){
            logger.severe("Failed to read inventory file " + inventoryFilePath + ".");
            e.printStackTrace();
            return;
        }

        //Injector agent controller initialization
        List<InjectorAgentController> injectorAgentController = getInjectorAgentController(
                scenario, new RequestSenderJSON(3, 1000));

        //Collector agent controller initialization
        List<CollectorAgentController> collectorAgentController = new ArrayList<>();
        if(collectorEndpoints != null && collectorEndpoints.length > 0) {
            collectorAgentController = getCollectorAgentController(
                    collectorEndpoints, new RequestSender(3, 1000));
        }

        final DistributedExperimentController controller = new DistributedExperimentController(
                injectorAgentController, collectorAgentController);

        //Create time selection for anomaly and load
        DistributedExperimentController.TimeSelector timeSelector_anomaly =
                controller.new ConstantTimeSelector(t_anomaly) ;
        DistributedExperimentController.TimeSelector timeSelector_load =
                controller.new ConstantTimeSelector(t_load);

        controller.setAnomalyTimeSelector(timeSelector_anomaly);
        controller.setLoadTimeSelector(timeSelector_load);

        //Set shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.log(Level.INFO, "Shouting down gracefully ...");
                shutdownHook(controller, flags.hasOption("f"));
                logger.log(Level.INFO, "Shut down successful.");
            } catch (InterruptedException | IOException e) {
                logger.log(Level.SEVERE, "Graceful shutdown failed.");
            }
        }));

        controller.startExperiment(stopTime, t_initialLoad);
    }

    /**
     * Helper class to deserialize anomaly scenario definition YAML file.
     */
    static class AnomalyScenario {

        public Map<String, List<AnomalyGroup>> anomalyGroups;
        public List<Host> hosts;
    }
    /**
     * Convert a duration value to milliseconds.
     * @param duration
     *          Numerical value representing the duration.
     * @param dimension
     *          Character represents the dimension of the duration. Can be [s]econds, [m]inutes,
     *          [h]ours or [d]ays.
     * @return
     *          Duration value in milliseconds.
     */
    private static long getDurationInMS(long duration, char dimension) {
        TimeUnit t;
        switch (dimension) {
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

    private static List<InjectorAgentController> getInjectorAgentController(
            AnomalyScenario scenario, RequestSenderJSON requestSenderJSON) {
        List<InjectorAgentController> injectorAgentController = new ArrayList<>();
        for(Host h : scenario.hosts){
            InjectorAgentController iac = new InjectorAgentController(h, requestSenderJSON);
            injectorAgentController.add(iac);
        }
        return injectorAgentController;
    }

    private static List<CollectorAgentController> getCollectorAgentController(
            String[] collectorEndpoints, RequestSender requestSender) {
        List<CollectorAgentController> collectorAgentController = new ArrayList<>();

        int i =  0;
        for(String endpoint : collectorEndpoints){
            CollectorAgentController cac = new CollectorAgentController(
                    new Host("collector_" + i++, endpoint, null), requestSender);
            collectorAgentController.add(cac);
        }
        return collectorAgentController;
    }

    private static synchronized void shutdownHook(DistributedExperimentController controller, boolean fetchRemoteData)
            throws IOException, InterruptedException {
        if(controller != null)
            controller.shutdown();
    }
}
