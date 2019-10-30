package anomaly.experiment.controller;

import anomaly.experiment.controller.objects.AnomalyGroup;
import anomaly.experiment.controller.objects.CollectorAgentController;
import anomaly.experiment.controller.objects.InjectorAgentController;
import anomaly.experiment.controller.utils.CommandExecuter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 28.08.17.
 */
public class DistributedExperimentController {

    private static final Logger logger = Logger.getLogger(DistributedExperimentController.class.getName());

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final String ANOMALY_TAG_KEY = "target";
    private static final String RCA_TAG_KEY = "RCA";

    private static final String CLS_KEY = "cls";
    private static final String DEFAULT_CLS_VALUE = "load";

    private static final long DEFAULT_TIME_VALUE = 5 * 60 * 1000;
    private static final long IDLE_TIME_VALUE = 10 * 1000;

    private static final int DEFAULT_AUTO_RECOVERY_DELAY = 5000 * 30;

    private List<InjectorAgentController> injectorAgentController;
    private List<CollectorAgentController> collectorAgentController;

    private TimeSelector anomalyTimeSelector;
    private TimeSelector loadTimeSelector;

    private InjectorAgentController currentInjectorAgentController = null;
    private boolean shutdown = false;
    private boolean suppressAnomalyReverting;
    private long autoRecoveryDelay;

    private Injector anomalyInjector;

    private String pathPostInjectionScript;

    public DistributedExperimentController(List<InjectorAgentController> injectorAgentController,
                                           List<CollectorAgentController> collectorAgentController,
                                           Injector anomalyInjector, String pathPostInjectionScriptpathPostInjectionScript,
                                           boolean suppressAnomalyReverting, long autoRecoveryDelay) {
        this.injectorAgentController = injectorAgentController;
        this.collectorAgentController = collectorAgentController;
        this.anomalyInjector = anomalyInjector;
        this.anomalyTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.loadTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.pathPostInjectionScript = pathPostInjectionScriptpathPostInjectionScript;
        this.suppressAnomalyReverting = suppressAnomalyReverting;
        this.autoRecoveryDelay = autoRecoveryDelay;
    }

    public DistributedExperimentController(List<InjectorAgentController> injectorAgentController,
                                           List<CollectorAgentController> collectorAgentController) {
        this(injectorAgentController, collectorAgentController,
                null, null, true, DEFAULT_AUTO_RECOVERY_DELAY);
        this.anomalyInjector = new DistributedExperimentController.RoundRobinInjector();
    }

    public long getAutoRecoveryDelay() {
        return autoRecoveryDelay;
    }

    public void setAutoRecoveryDelay(long autoRecoveryDelay) {
        this.autoRecoveryDelay = autoRecoveryDelay;
    }

    public void setAnomalyTimeSelector(TimeSelector anomalyTimeSelector) {
        this.anomalyTimeSelector = anomalyTimeSelector;
    }

    public void setLoadTimeSelector(TimeSelector loadTimeSelector) {
        this.loadTimeSelector = loadTimeSelector;
    }

    public Injector getAnomalyInjector() {
        return anomalyInjector;
    }

    public void setAnomalyInjector(Injector anomalyInjector) {
        this.anomalyInjector = anomalyInjector;
    }

    public void startExperiment(long endTime, int numberOfAnomalyInjections) throws ScriptHookUpException{
        this.startExperiment(endTime, 0, numberOfAnomalyInjections);
    }

    public String getPathPostInjectionScript() {
        return pathPostInjectionScript;
    }

    public void setPathPostInjectionScript(String pathPostInjectionScript) {
        this.pathPostInjectionScript = pathPostInjectionScript;
    }

    public boolean isSuppressAnomalyReverting() {
        return suppressAnomalyReverting;
    }

    public void setSuppressAnomalyReverting(boolean suppressAnomalyReverting) {
        this.suppressAnomalyReverting = suppressAnomalyReverting;
    }

    public void startExperiment(long endTime, long initialDelay, int numberOfAnomalyInjections)
            throws ScriptHookUpException{
        logger.log(Level.INFO, "Starting experiment. Expected end of experiment: " +
                DATE_TIME_FORMAT.format(new Date(endTime)));

        //Log targets on which the experiments should be executed
        logTargets("Injector", injectorAgentController);
        logTargets("Collector", collectorAgentController);

        //Reset collector data sort_data
        this.unsetTags(new HashSet<>(Arrays.asList(CLS_KEY, ANOMALY_TAG_KEY)));
        this.setClsTags();

        //Start file output at collectors
        this.startFileOutputAtCollectors();

        if (injectorAgentController.size() > 0) {
            try {
                //Initially delay execution of anomaly simulations
                if (initialDelay > 0) {
                    logger.log(Level.INFO, "Executing initial load...");
                    Thread.sleep(initialDelay);
                }
                //Run anomaly injection experiment
                if(numberOfAnomalyInjections < 0)
                    this.runExperiment(endTime);
                else
                    this.runExperiment(numberOfAnomalyInjections);
            } catch (InterruptedException ignored) {
            }
        } else {
            //Run idle loop if no anomaly simulations should be run
            try {
                this.runIdle(endTime);
            } catch (InterruptedException ignored) {
            }
        }

        //Shutdown experiment
        this.shutdown();
    }

    public synchronized void shutdown() {
        //Set shutdown flag
        this.shutdown = true;
        //If there is some anomaly running --> stop it
        if (currentInjectorAgentController != null) {
            if (currentInjectorAgentController.getCurrentAnomaly() != null) {
                AnomalyGroup a = currentInjectorAgentController.getCurrentAnomaly();
                currentInjectorAgentController.stopAnomaly(a);
            }
        }
        //Delete all tags at collectors
        this.unsetTags(new HashSet<>(Arrays.asList(CLS_KEY, ANOMALY_TAG_KEY, RCA_TAG_KEY)));
        //Stop data collection at collector targets
        stopFileOutputAtCollectors();
    }

    private void logTargets(String msg, List<?> injectorAgentController) {
        for (Object o : injectorAgentController)
            logger.log(Level.INFO, msg + ": " + o.toString());
    }

    private void startFileOutputAtCollectors() {
        for (CollectorAgentController cac : collectorAgentController)
            cac.startFileOutput();
    }

    private void stopFileOutputAtCollectors() {
        for (CollectorAgentController cac : collectorAgentController)
            cac.stopFileOutput();
    }

    private void runExperiment(int numberOfInjections) throws InterruptedException, ScriptHookUpException {
        // Loop until shutdown flag is set
        while (!shutdown) {
            AnomalyGroup anomalyGroup = anomalyInjector.getNextAnomaly();
            if (anomalyGroup != null) {
                this.runAnomalyInjection(anomalyGroup);
            }
            if(this.isNumberOfInjectionsReached()){
                shutdown = true;
            }
        }
        logger.log(Level.INFO, String.format("Experiment reached end after each anomaly was " +
                "executed %d times on every host.", numberOfInjections));
    }

    private boolean isNumberOfInjectionsReached() {
        boolean result = true;
        // All injector controller must reach the max injection counter to make return value true
        for(InjectorAgentController ic : this.injectorAgentController){
            result = result && ic.isMaxInjectionCountReached();
        }
        return result;
    }

    private void runExperiment(long stopTime) throws InterruptedException, ScriptHookUpException {
        //Loop as long as the end time is not reached
        while (new Date().getTime() < stopTime && !shutdown) {
            AnomalyGroup anomalyGroup = anomalyInjector.getNextAnomaly();
            if (anomalyGroup != null) {
                this.runAnomalyInjection(anomalyGroup);
            }
        }
        logger.log(Level.INFO, "Experiment reached end after defined runtime expired.");
    }

    private void runAnomalyInjection(AnomalyGroup anomalyGroup) throws InterruptedException, ScriptHookUpException {
        Set<String> clsTagKeys = new HashSet<>(Arrays.asList(CLS_KEY));
        long anomalyRuntime = anomalyTimeSelector.getTime();

        this.unsetTags(clsTagKeys);
        this.setAnomalyAndRcaTags(anomalyGroup, currentInjectorAgentController.getHost().getName());
        anomalyInjector.injectNextAnomaly(anomalyGroup, (int) (anomalyRuntime + autoRecoveryDelay));
        Thread.sleep(anomalyRuntime);
        if (!this.suppressAnomalyReverting)
            currentInjectorAgentController.stopAnomaly(anomalyGroup);
        this.currentInjectorAgentController = null;
        this.unsetTags(new HashSet<>(Arrays.asList(ANOMALY_TAG_KEY, RCA_TAG_KEY)));
        this.setClsTags();
        if(!this.executePostInjectionScript()){
            throw new ScriptHookUpException(String.format("Failed to execute post injection script %s.",
                    this.pathPostInjectionScript));
        }
        Thread.sleep(loadTimeSelector.getTime());
    }

    private boolean executePostInjectionScript() throws InterruptedException {
        boolean success = true;
        if (this.pathPostInjectionScript != null && !this.pathPostInjectionScript.isEmpty()) {
            try {
                success = CommandExecuter.executeCommand(this.pathPostInjectionScript);
            } catch (IOException e) {
                logger.log(Level.WARNING, String.format("Not able to execute post injection script: " +
                        "%s", this.pathPostInjectionScript), e);
                success = false;
            }
        }
        return success;
    }

    private void runIdle(long stopTime) throws InterruptedException {
        while (new Date().getTime() < stopTime && !shutdown)
            Thread.sleep(IDLE_TIME_VALUE);
    }

    private void unsetTags(Set<String> tagKeys) {
        for (CollectorAgentController cac : collectorAgentController)
            cac.removeTags(tagKeys);
    }

    private void setAnomalyTags(AnomalyGroup anomalyGroup, String injectorTargetName) {
        Map<String, String> tags = new HashMap<>();
        tags.put(ANOMALY_TAG_KEY, injectorTargetName + "|" + anomalyGroup.getName());
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    private void setRcaTags(AnomalyGroup anomalyGroup, String InjectorTargetName) {
        Map<String, String> tags = new HashMap<>();
        tags.put(RCA_TAG_KEY, InjectorTargetName);
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    /* fewer requests compared to use setrcatags and setanomaly tags */
    private void setAnomalyAndRcaTags(AnomalyGroup anomalyGroup, String injectorTargetName) {
        Map<String, String> tags = new HashMap<>();
        tags.put(RCA_TAG_KEY, injectorTargetName);
        tags.put(ANOMALY_TAG_KEY, injectorTargetName + "|" + anomalyGroup.getName());
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    private void setClsTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put(CLS_KEY, DEFAULT_CLS_VALUE);
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    public interface Injector {
        void injectNextAnomaly(AnomalyGroup anomalyGroup, int backupRevertTime);
        AnomalyGroup getNextAnomaly();
    }

    public interface TimeSelector {
        long getTime();
    }

    public class ConstantTimeSelector implements TimeSelector {
        private long timeValue;

        public ConstantTimeSelector(long timeValue) {
            this.timeValue = timeValue;
        }

        @Override
        public long getTime() {
            return timeValue;
        }
    }

    public class EqualDestributionTimeSelector implements TimeSelector {
        private long minTimeValue;
        private long maxTimeValue;

        public EqualDestributionTimeSelector(long minTimeValue, long maxTimeValue) {
            this.minTimeValue = minTimeValue;
            this.maxTimeValue = maxTimeValue;
        }

        @Override
        public long getTime() {
            return ThreadLocalRandom.current().nextLong(minTimeValue, maxTimeValue);
        }
    }

    public class NormalDestributionTimeSelector implements TimeSelector {
        private final Random random = new Random();
        private long mean;
        private long stdDeviation;

        public NormalDestributionTimeSelector(long mean, long stdDeviation) {
            this.mean = mean;
            this.stdDeviation = stdDeviation;
        }

        @Override
        public long getTime() {
            return Math.round(random.nextGaussian() * mean + stdDeviation);
        }
    }

    public class RoundRobinInjector implements Injector {
        private int injectorIndex;
        private int anomalyInjectionCounter;
        private boolean isNextAnomalyIndexReset = true;


        RoundRobinInjector() {
            injectorIndex = 0;
        }

        @Override
        public AnomalyGroup getNextAnomaly() {
            //Run anomalies on targets one after another
            if (injectorIndex < injectorAgentController.size()) {
                InjectorAgentController injectorTarget = injectorAgentController.get(injectorIndex++);
                AnomalyGroup a = injectorTarget.getNextAnomaly();
                if (a != null) {
                    anomalyInjectionCounter++;
                    currentInjectorAgentController = injectorTarget;
                    return a;
                } else {
                    getNextAnomaly();
                }
            } else {
                injectorIndex = 0;
                //After EACH injector target executed ALL of its anomalies, all injector target anomalies
                //are reset and can be started in the next round.
                if (anomalyInjectionCounter == 0) {
                    for (InjectorAgentController iac : injectorAgentController) {
                        iac.resetAnomalySelector();
                    }
                }
                anomalyInjectionCounter = 0;
            }
            return null;
        }

        @Override
        public void injectNextAnomaly(AnomalyGroup anomalyGroup, int backupRevertTime) {
            //Run anomalies on target
            currentInjectorAgentController.startNextAnomaly(anomalyGroup, backupRevertTime);
        }
    }
    public class ScriptHookUpException extends Exception{
        public ScriptHookUpException() {
        }

        public ScriptHookUpException(String message) {
            super(message);
        }

        public ScriptHookUpException(String message, Throwable cause) {
            super(message, cause);
        }

        public ScriptHookUpException(Throwable cause) {
            super(cause);
        }

        public ScriptHookUpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}