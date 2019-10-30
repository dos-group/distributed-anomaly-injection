package anomaly.experiment.controller;

import anomaly.experiment.controller.objects.AnomalyGroup;
import anomaly.experiment.controller.objects.CollectorAgentController;
import anomaly.experiment.controller.objects.HostGroupInjectionController;
import anomaly.experiment.controller.utils.CommandExecuter;
import anomaly.experiment.controller.utils.Pair;
import anomaly.experiment.controller.utils.Utils;

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

    private List<HostGroupInjectionController> hostGroupInjectionController;
    private List<CollectorAgentController> collectorAgentController;

    private TimeSelector anomalyTimeSelector;
    private TimeSelector loadTimeSelector;

    private HostGroupInjectionController currentHostGroupInjectionController = null;
    private boolean shutdown = false;
    private boolean suppressAnomalyReverting;
    private long autoRecoveryDelay;

    private HostGroupSelector selector;

    private String pathPostInjectionScript;

    public DistributedExperimentController(List<HostGroupInjectionController> hostGroupInjectionController,
                                           List<CollectorAgentController> collectorAgentController,
                                           String pathPostInjectionScript,
                                           boolean suppressAnomalyReverting,
                                           long autoRecoveryDelay,
                                           HostGroupSelector selector,
                                           TimeSelector anomalyTimeSelector,
                                           TimeSelector loadTimeSelector) {
        this.hostGroupInjectionController = hostGroupInjectionController;
        this.collectorAgentController = collectorAgentController;
        this.anomalyTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.loadTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.pathPostInjectionScript = pathPostInjectionScript;
        this.suppressAnomalyReverting = suppressAnomalyReverting;
        this.autoRecoveryDelay = autoRecoveryDelay;
        this.selector = selector;
        this.anomalyTimeSelector = anomalyTimeSelector;
        this.loadTimeSelector = loadTimeSelector;
    }

    public static HostGroupSelector getHostGroupSelector(
            String selector, List<HostGroupInjectionController> hostGroupInjectionController) {

        if (selector.equals("rr")) {
            return new RoundRobinSelector(hostGroupInjectionController);
        } else if (selector.equals("random")) {
            return new RandomSelector(hostGroupInjectionController);
        } else {
            throw new IllegalArgumentException(String.format("Unknown selector %s. Known selector are: %s, %s.",
                    selector, "rr", "random"));
        }
    }

    public static TimeSelector getTimeSelector(String timeSelector){
        String distribution = timeSelector.substring(0, 1);
        String tmp = timeSelector.substring(1);
        if (distribution.equals("C")) {
            long t1 = Utils.getDurationInMS(Integer.parseInt(
                    tmp.substring(0, tmp.length() - 1)), tmp.charAt(tmp.length() - 1));
            return new ConstantTimeSelector(t1);
        } else{
            String[] parts = tmp.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid time distribution definition %s. " +
                        "Expected two time definitions separated by ':' but got %s", timeSelector, tmp));
            }
            long t1 = Utils.getDurationInMS(Integer.parseInt(
                    parts[0].substring(0, parts[0].length() - 1)), parts[0].charAt(parts[0].length() - 1));
            long t2 = Utils.getDurationInMS(Integer.parseInt(
                    parts[1].substring(0, parts[1].length() - 1)), parts[1].charAt(parts[1].length() - 1));
            if(distribution.equals("E")) {
                return new EqualDistributionTimeSelector(t1, t2);
            } else if(distribution.equals("N")) {
                return new NormalDistributionTimeSelector(t1, t2);
            } else {
                throw new IllegalArgumentException(String.format("Invalid time distribution definition %s. " +
                        "First letter must describe distribution type but distribution %s not supported. " +
                        "Known distributions: %s, %s, %s", timeSelector, distribution, "C", "E", "N"));
            }
        }
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

    public HostGroupSelector getHostGroupSelector() {
        return selector;
    }

    public void setHostGroupSelector(HostGroupSelector selector) {
        this.selector = selector;
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
        logTargets("Injector", hostGroupInjectionController);
        logTargets("Collector", collectorAgentController);

        //Reset collector data sort_data
        this.unsetTags(new HashSet<>(Arrays.asList(CLS_KEY, ANOMALY_TAG_KEY)));
        this.setClsTags();

        //Start file output at collectors
        this.startFileOutputAtCollectors();

        if (hostGroupInjectionController.size() > 0) {
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
        if (currentHostGroupInjectionController != null) {
            if (currentHostGroupInjectionController.getCurrentAnomaly() != null) {
                AnomalyGroup a = currentHostGroupInjectionController.getCurrentAnomaly();
                currentHostGroupInjectionController.stopAnomaly(a);
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
            Pair<HostGroupInjectionController, AnomalyGroup> result = selector.getNextInjectionTarget();
            if (result != null) {
                this.currentHostGroupInjectionController = result.getLeft();
                AnomalyGroup anomalyGroup = result.getRight();
                if (anomalyGroup != null) {
                    this.runAnomalyInjection(anomalyGroup);
                }
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
        for(HostGroupInjectionController ic : this.hostGroupInjectionController){
            result = result && ic.isMaxInjectionCountReached();
        }
        return result;
    }

    private void runExperiment(long stopTime) throws InterruptedException, ScriptHookUpException {
        //Loop as long as the end time is not reached
        while (new Date().getTime() < stopTime && !shutdown) {
            Pair<HostGroupInjectionController, AnomalyGroup> result = selector.getNextInjectionTarget();
            this.currentHostGroupInjectionController = result.getLeft();
            AnomalyGroup anomalyGroup = result.getRight();
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
        this.setAnomalyAndRcaTags(anomalyGroup, this.currentHostGroupInjectionController.getCurrentEndpoint().getComponent());
        this.currentHostGroupInjectionController.startNextAnomaly(anomalyGroup, (int) (anomalyRuntime + autoRecoveryDelay));
        Thread.sleep(anomalyRuntime);
        if (!this.suppressAnomalyReverting)
            currentHostGroupInjectionController.stopAnomaly(anomalyGroup);
        this.currentHostGroupInjectionController = null;
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

    private void setAnomalyTags(AnomalyGroup anomalyGroup, String component) {
        Map<String, String> tags = new HashMap<>();
        tags.put(ANOMALY_TAG_KEY, component + "|" + anomalyGroup.getName());
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    private void setRcaTags(String component) {
        Map<String, String> tags = new HashMap<>();
        tags.put(RCA_TAG_KEY, component);
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    /* fewer requests compared to use setrcatags and setanomaly tags */
    private void setAnomalyAndRcaTags(AnomalyGroup anomalyGroup, String component) {
        Map<String, String> tags = new HashMap<>();
        tags.put(RCA_TAG_KEY, component);
        tags.put(ANOMALY_TAG_KEY, component + "|" + anomalyGroup.getName());
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    private void setClsTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put(CLS_KEY, DEFAULT_CLS_VALUE);
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    public interface TimeSelector {
        long getTime();
    }

    public static class ConstantTimeSelector implements TimeSelector {
        private long timeValue;

        public ConstantTimeSelector(long timeValue) {
            this.timeValue = timeValue;
        }

        @Override
        public long getTime() {
            return timeValue;
        }
    }

    public static class EqualDistributionTimeSelector implements TimeSelector {
        private long minTimeValue;
        private long maxTimeValue;

        public EqualDistributionTimeSelector(long minTimeValue, long maxTimeValue) {
            this.minTimeValue = minTimeValue;
            this.maxTimeValue = maxTimeValue;
        }

        @Override
        public long getTime() {
            return ThreadLocalRandom.current().nextLong(minTimeValue, maxTimeValue);
        }
    }

    public static class NormalDistributionTimeSelector implements TimeSelector {
        private final Random random = new Random();
        private long mean;
        private long stdDeviation;

        public NormalDistributionTimeSelector(long mean, long stdDeviation) {
            this.mean = mean;
            this.stdDeviation = stdDeviation;
        }

        @Override
        public long getTime() {
            return Math.round(random.nextGaussian() * mean + stdDeviation);
        }
    }

    public interface HostGroupSelector {
        Pair<HostGroupInjectionController, AnomalyGroup> getNextInjectionTarget();
    }

    public static class RoundRobinSelector implements HostGroupSelector {
        private int injectorTargetIndex;
        private int anomalyInjectionCounter;

        private List<HostGroupInjectionController> hostGroupInjectionController;

        RoundRobinSelector(List<HostGroupInjectionController> hostGroupInjectionController) {
            this.injectorTargetIndex = 0;
            this.hostGroupInjectionController = hostGroupInjectionController;
        }

        @Override
        public Pair<HostGroupInjectionController, AnomalyGroup> getNextInjectionTarget() {
            // Run anomalies on targets one after another
            if (this.injectorTargetIndex < this.hostGroupInjectionController.size()) {
                HostGroupInjectionController injectorTarget = this.hostGroupInjectionController.get(this.injectorTargetIndex++);
                AnomalyGroup a = injectorTarget.getNextAnomaly();
                if (a != null) {
                    this.anomalyInjectionCounter++;
                    return new Pair<>(injectorTarget, a);
                } else {
                    return getNextInjectionTarget();
                }
            } else {
                this.injectorTargetIndex = 0;
                //After EACH injector target executed ALL of its anomalies, all injector target anomalies
                //are reset and can be started in the next round.
                if (this.anomalyInjectionCounter == 0) {
                    for (HostGroupInjectionController iac : this.hostGroupInjectionController) {
                        iac.resetAnomalySelector();
                    }
                }
                this.anomalyInjectionCounter = 0;
            }
            return null;
        }
    }

    public static class RandomSelector implements HostGroupSelector {
        private Random random = new Random();

        private List<HostGroupInjectionController> hostGroupInjectionController;

        public RandomSelector(List<HostGroupInjectionController> hostGroupInjectionController) {
            this.hostGroupInjectionController = hostGroupInjectionController;
        }

        @Override
        public Pair<HostGroupInjectionController, AnomalyGroup> getNextInjectionTarget() {
            if (this.hostGroupInjectionController.isEmpty()) {
                return null; // Exit for recursive method call
            }
            int randomIndex = random.nextInt(hostGroupInjectionController.size());
            HostGroupInjectionController injectorTarget = this.hostGroupInjectionController.get(randomIndex);
            AnomalyGroup a = injectorTarget.getNextAnomaly();
            if (a != null) {
                return new Pair<>(injectorTarget, a);
            } else {
                injectorTarget.resetAnomalySelector();
                a = injectorTarget.getNextAnomaly();
                if (a == null) {
                    this.hostGroupInjectionController.remove(randomIndex); // Remove if anomaly limit is reached
                    return getNextInjectionTarget();
                } else {
                    return new Pair<>(injectorTarget, a);
                }
            }
        }
    }

    public static class ScriptHookUpException extends Exception{
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