package anomaly.experiment.controller.main;

import anomaly.experiment.controller.DistributedExperimentController;
import anomaly.experiment.controller.objects.*;
import anomaly.experiment.controller.requests.HTTPRequestSender;
import anomaly.experiment.controller.requests.MockRequestSender;
import anomaly.experiment.controller.requests.RequestSender;
import anomaly.experiment.controller.utils.CommandExecuter;
import anomaly.experiment.controller.utils.Config;
import anomaly.experiment.controller.utils.UnirestUtils;
import anomaly.experiment.controller.utils.Utils;
import org.apache.commons.cli.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by alex on 20.02.19.
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static {
        Config.initializeLogger();
    }

    public static void main(String[] args) throws MissingArgumentException {

        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(Level.INFO));
        CommandLineParser parser = new DefaultParser();
        Options o = new Options();
        o.addOption(Option.builder("i").longOpt("inventory_file").hasArg().required().desc(
                "YAML file containing anomaly experiment information.").build());
        o.addOption(Option.builder("t").hasArg().desc(
                "Duration of the whole experiment. Last character determines dimension. Possible dimensions: " +
                        "s, m, h, d (seconds, minutes, hours, days). Either this or the -number_injection argument" +
                        "is required in order to determine the experiment duration.").build());
        o.addOption(Option.builder("n").longOpt("number_injections").hasArg().desc(
                "Define amount of anomaly executions on each injection target. Every defined anomaly will be executed" +
                        "exactly this amount of times on the host. Either this or the -t argument is " +
                        "required in order to determine the experiment duration.").build());
        o.addOption(Option.builder("t_anomaly").hasArg().desc(
                "Runtime of each anomaly. Is defined as random distribution. First character determines distribution " +
                        "type (C - constant, N - Normal distribution, E - Equal distribution). " +
                        "After that, the time value(s) are defined. Normal and equal  distributions require two time values " +
                        "separated by ':'. Last character determines dimension. Possible dimensions:" +
                        " s, m, h, d (seconds, minutes, hours, days). Default is C5m (constant 5 minutes)." +
                        "Examples for normal distribution (N[mean value]:[standard deviation]: N5m30s, N30s10s, N45s10m." +
                        "Example for equal distribution (E[lower bound]:[upper bound]: E5s:10s, E10m:20m").build());
        o.addOption(Option.builder("t_load").hasArg().desc(
                "Runtime of each anomaly. Is defined as random distribution. First character determines distribution " +
                        "type (C - constant, N - Normal distribution, E - Equal distribution). " +
                        "After that, the time value(s) are defined. Normal and equal  distributions require two time values " +
                        "separated by ':'. Last character determines dimension. Possible dimensions:" +
                        " s, m, h, d (seconds, minutes, hours, days). Default is C5m (constant 5 minutes)." +
                        "Examples for normal distribution (N[mean value]:[standard deviation]: N5m30s, N30s10s, N45s10m." +
                        "Example for equal distribution (E[lower bound]:[upper bound]: E5s:10s, E10m:20m").build());
        o.addOption(Option.builder("t_initial_load").hasArg().desc(
                "Runtime of initial load. Time at beginning of experiment, where no anomalies will be triggered. " +
                        "Last character determines dimension. Possible dimensions: " +
                        "s, m, h, d (seconds, minutes, hours, days).").build());
        o.addOption(Option.builder("c").longOpt("collector_endpoint").hasArgs().desc("Collector endpoint" +
                " used for documentation of injected anomalies. It is possible to define multiple endpoints.").build());
        o.addOption(Option.builder("post_inject_script").hasArgs().desc("Path to executable script file" +
                " which will be executed after each anomaly injection.").build());
        o.addOption(Option.builder("suppress_anomaly_reverting").desc("If set, the injected anomalies won't" +
                " be reverted after t_anomaly expired.").build());
        o.addOption(Option.builder("auto_recovery_delay").hasArgs().desc("(t_anomaly + auto_recovery_delay) is " +
                "the time after which the anomaly should be auto-reverted by the injector agent itself. " +
                "Last character determines dimension. Possible dimensions: s, m, h, d (seconds, " +
                "minutes, hours, days).").build());
        o.addOption(Option.builder("anomaly_group_selection").hasArgs().desc("Controls how anomaly groups are " +
                "selected for each host. 'iter' will select anomalies one after another while 'random' will pick random" +
                " anomaly groups for injection.").build());
        o.addOption(Option.builder("host_group_selection").hasArgs().desc("Controls how host groups are " +
                "selected for anomaly injection. 'rr' will select host group in round robin fashion while 'random' will pick random" +
                " host groups for injection.").build());
        o.addOption(Option.builder("run_mock_mode").desc("If set, no real requests will be sent. " +
                " Can be used to test experiment in dry run to see if it behaves as intended.").build());
        final CommandLine flags;
        try {
            flags = parser.parse(o, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        //Yaml yaml = new Yaml(new Constructor(AnomalyScenario.class));
        //HostGroup hg1 = new HostGroup();
        //HostGroup hg2 = new HostGroup();
        //AnomalyScenario as = new AnomalyScenario();

        //Path to inventory file.
        String inventoryFilePath = flags.getOptionValue("i");

        long stopTime = 0;
        int numberInjection = -1;
        if (flags.hasOption("t") && flags.hasOption("n")) {
            throw new IllegalArgumentException("Must set either -t or -n argument, but not both.");
        } else if (flags.hasOption("t")) { // Ending experiment based on time
            //Get duration of the whole experiment in milliseconds
            String tmp = flags.getOptionValue("t");
            long experimentDuration = Utils.getDurationInMS(
                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
            //Calculate end of experiment
            stopTime = new Date().getTime() + experimentDuration;
        } else if (flags.hasOption("n")) { // Ending experiment based on number of injections
            numberInjection = Integer.parseInt(flags.getOptionValue("n"));
            if (numberInjection <= 0) {
                throw new IllegalArgumentException(String.format("Duration must be positive > 0, " +
                        "but was set to %d", numberInjection));
            }
        } else {
            throw new MissingArgumentException("Either -t or -n argument is required in order to determine the end" +
                    " of the experiment.");
        }

        //Get duration of initial load
        long t_initialLoad = 0;
        if (flags.hasOption("t_initial_load")) {
            String tmp = flags.getOptionValue("t_initial_load");
            t_initialLoad = Utils.getDurationInMS(
                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        }

        // duration of each injected anomaly
        String t_anomaly = "C5m";
        if (flags.hasOption("t_anomaly")) {
            t_anomaly = flags.getOptionValue("t_anomaly");
        }

        // duration of each injected anomaly
        String t_load = "C5m";
        if (flags.hasOption("t_load")) {
            t_load = flags.getOptionValue("t_load");
        }

        //Collector endpoints used for documentation of anomaly injections
        String[] collectorEndpoints = null;
        if (flags.hasOption("c")) {
            collectorEndpoints = flags.getOptionValues("c");
        }

        //Script, that should be executed after each injection
        String pathPostInjectionScript = null;
        if (flags.hasOption("post_inject_script")) {
            pathPostInjectionScript = flags.getOptionValue("post_inject_script");
            if (!CommandExecuter.checkFileExists(pathPostInjectionScript)) {
                logger.log(Level.SEVERE, String.format("Post injection script file %s does not exist. Exiting...",
                        pathPostInjectionScript));
                return;
            }
        }

        //Get duration of initial load
        long auto_recovery_delay = 0;
        if (flags.hasOption("auto_recovery_delay")) {
            String tmp = flags.getOptionValue("auto_recovery_delay");
            auto_recovery_delay = Utils.getDurationInMS(
                    Integer.parseInt(tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
        }

        boolean suppressAnomalyReverting = flags.hasOption("suppress_anomaly_reverting");
        boolean run_mock_mode = flags.hasOption("run_mock_mode");

        String anomalyGroupSelector = "iter";
        if (flags.hasOption("anomaly_group_selection")) {
            anomalyGroupSelector = flags.getOptionValue("anomaly_group_selection");
        }

        String hostGroupSelector = "rr";
        if (flags.hasOption("host_group_selection")) {
            hostGroupSelector = flags.getOptionValue("host_group_selection");
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
        } catch (Exception e) {
            logger.severe("Failed to read inventory file " + inventoryFilePath + ".");
            e.printStackTrace();
            return;
        }

        // Request sender
        RequestSender requestSender;
        if (run_mock_mode) {
            requestSender = new MockRequestSender();
        } else {
            requestSender = new HTTPRequestSender(3, 1000);
        }

        //Injector agent controller initialization
        List<HostGroupInjectionController> hostGroupInjectionController = getInjectorAgentController(
                scenario, requestSender, numberInjection, anomalyGroupSelector);
        //Collector agent controller initialization
        List<CollectorAgentController> collectorAgentController = new ArrayList<>();
        if (collectorEndpoints != null && collectorEndpoints.length > 0) {
            collectorAgentController = getCollectorAgentController(
                    collectorEndpoints, requestSender);
        }

        final DistributedExperimentController controller = new DistributedExperimentController(
                hostGroupInjectionController,
                collectorAgentController,
                pathPostInjectionScript,
                suppressAnomalyReverting,
                auto_recovery_delay,
                DistributedExperimentController.getHostGroupSelector(hostGroupSelector, hostGroupInjectionController),
                DistributedExperimentController.getTimeSelector(t_anomaly),
                DistributedExperimentController.getTimeSelector(t_load));

        //Set shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.INFO, "Shutting down gracefully ...");
            shutdownHook(controller);
            logger.log(Level.INFO, "Shut down successful.");
        }));

        try {
            controller.startExperiment(stopTime, t_initialLoad, numberInjection);
        } catch (DistributedExperimentController.ScriptHookUpException e) {
            logger.log(Level.SEVERE, "Experiment failed due to failed execution of a hookup script.", e);
        }
    }

    private static List<HostGroupInjectionController> getInjectorAgentController(
            AnomalyScenario scenario, RequestSender requestSender, int numberOfAnomalyInjections,
            String anomalyGroupSelector) {
        List<HostGroupInjectionController> hostGroupInjectionController = new ArrayList<>();
        for (HostGroup hg : scenario.getHostGroups()) {
            HostGroupInjectionController iac = new HostGroupInjectionController(hg, requestSender, numberOfAnomalyInjections,
                    anomalyGroupSelector);
            hostGroupInjectionController.add(iac);
        }
        return hostGroupInjectionController;
    }

    private static List<CollectorAgentController> getCollectorAgentController(
            String[] collectorEndpoints, RequestSender requestSender) {
        List<CollectorAgentController> collectorAgentController = new ArrayList<>();

        int i = 0;
        for (String ce : collectorEndpoints) {
            Endpoint endpoint = new Endpoint("collector", "collector", ce);
            CollectorAgentController cac = new CollectorAgentController(endpoint, requestSender);
            collectorAgentController.add(cac);
        }
        return collectorAgentController;
    }

    private static synchronized void shutdownHook(DistributedExperimentController controller) {
        if (controller != null)
            controller.shutdown();
    }
}
