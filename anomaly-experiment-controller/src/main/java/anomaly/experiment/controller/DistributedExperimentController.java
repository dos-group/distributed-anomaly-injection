package anomaly.experiment.controller;

import anomaly.experiment.controller.objects.AnomalyGroup;
import anomaly.experiment.controller.objects.CollectorAgentController;
import anomaly.experiment.controller.objects.InjectorAgentController;

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
    private static final String CLS_KEY = "cls";
    private static final String DEFAULT_CLS_VALUE = "load";

    private static final long DEFAULT_TIME_VALUE = 5 * 60 * 1000;
    private static final long IDLE_TIME_VALUE = 10 * 1000;
    private static final int AUTO_RECOVERY_DELAY = 5000;

    private List<InjectorAgentController> injectorAgentController;
    private List<CollectorAgentController> collectorAgentController;

    private TimeSelector anomalyTimeSelector;
    private TimeSelector loadTimeSelector;

    private InjectorAgentController currentInjectorAgentController = null;
    private boolean shutdown = false;

    private Injector anomalyInjector;

    public interface Injector{
        void injectNextAnomaly(AnomalyGroup anomalyGroup, int backupRevertTime);
        AnomalyGroup getNextAnomaly();
    }

    public DistributedExperimentController(List<InjectorAgentController> injectorAgentController,
                                           List<CollectorAgentController> collectorAgentController,
                                           Injector anomalyInjector){
        this.injectorAgentController = injectorAgentController;
        this.collectorAgentController = collectorAgentController;
        this.anomalyInjector = anomalyInjector;
        this.anomalyTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.loadTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
    }

    public DistributedExperimentController(List<InjectorAgentController> injectorAgentController,
                                           List<CollectorAgentController> collectorAgentController){
        this(injectorAgentController, collectorAgentController, null);
        this.anomalyInjector = new DistributedExperimentController.RoundRobinInjector();
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

    public void startExperiment(long endTime){
        this.startExperiment(endTime, 0);
    }

    public void startExperiment(long endTime, long initialDelay){
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

        if(injectorAgentController.size() > 0) {
            try {
                //Initially delay execution of anomaly simulations
                if (initialDelay > 0) {
                    logger.log(Level.INFO, "Executing initial load...");
                    Thread.sleep(initialDelay);
                }
                //Run anomaly injection experiment
                this.runExperiment(endTime);
            } catch (InterruptedException ignored) {
            }
        } else{
            //Run idle loop if no anomaly simulations should be run
            try {
                this.runIdle(endTime);
            } catch (InterruptedException ignored) {}
        }

        //Shutdown experiment
        this.shutdown();
    }

    public synchronized void shutdown(){
        //Set shutdown flag
        this.shutdown = true;
        //If there is some anomaly running --> stop it
        if(currentInjectorAgentController != null){
            if(currentInjectorAgentController.getCurrentAnomaly() != null) {
                AnomalyGroup a = currentInjectorAgentController.getCurrentAnomaly();
                currentInjectorAgentController.stopAnomaly(a);
            }
        }
        //Delete all tags at collectors
        this.unsetTags(new HashSet<>(Arrays.asList(CLS_KEY, ANOMALY_TAG_KEY)));
        //Stop data collection at collector targets
        stopFileOutputAtCollectors();
    }

    private void logTargets(String msg, List<? extends Object> injectorAgentController) {
        for(Object o : injectorAgentController)
            logger.log(Level.INFO, msg + ": " + o.toString());
    }

    private void startFileOutputAtCollectors() {
        for(CollectorAgentController cac : collectorAgentController)
            cac.startFileOutput();
    }

    private void stopFileOutputAtCollectors() {
        for(CollectorAgentController cac : collectorAgentController)
            cac.stopFileOutput();
    }

    private void runExperiment(long stopTime) throws InterruptedException {
        //Auxiliary variables
        Set<String> clsTagKeys = new HashSet<>(Arrays.asList(CLS_KEY));
        Set<String> anomalyTagKeys = new HashSet<>(Arrays.asList(ANOMALY_TAG_KEY));
        Set<String> tagCombination;
        int anomalyExecutionCounter = 0;

        //Loop as long as the end time is not reached
        while(new Date().getTime() < stopTime && !shutdown) {
            long anomalyRuntime = anomalyTimeSelector.getTime();
            AnomalyGroup anomalyGroup = anomalyInjector.getNextAnomaly();
            if(anomalyGroup != null) {
                anomalyExecutionCounter++;
                this.unsetTags(clsTagKeys);
                this.setAnomalyTags(anomalyGroup, currentInjectorAgentController.getHost().getName());
                anomalyInjector.injectNextAnomaly(anomalyGroup, (int)(anomalyRuntime + AUTO_RECOVERY_DELAY));
                Thread.sleep(anomalyRuntime);
                currentInjectorAgentController.stopAnomaly(anomalyGroup);
                this.currentInjectorAgentController = null;
                tagCombination = new HashSet<>(anomalyTagKeys);
                this.unsetTags(tagCombination);
                this.setClsTags();
                Thread.sleep(loadTimeSelector.getTime());
            }
        }
    }

    private void runIdle(long stopTime) throws InterruptedException {
        while(new Date().getTime() < stopTime && !shutdown)
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

    private void setClsTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put(CLS_KEY, DEFAULT_CLS_VALUE);
        for (CollectorAgentController cac : collectorAgentController)
            cac.setTags(tags);
    }

    public interface TimeSelector{
        long getTime();
    }

    public class ConstantTimeSelector implements TimeSelector{
        private long timeValue;

        public ConstantTimeSelector(long timeValue){
            this.timeValue = timeValue;
        }

        @Override
        public long getTime(){
            return timeValue;
        }
    }

    public class EqualDestributionTimeSelector implements TimeSelector{
        private long minTimeValue;
        private long maxTimeValue;

        public EqualDestributionTimeSelector(long minTimeValue, long maxTimeValue){
            this.minTimeValue = minTimeValue;
            this.maxTimeValue = maxTimeValue;
        }

        @Override
        public long getTime(){
            return ThreadLocalRandom.current().nextLong(minTimeValue, maxTimeValue);
        }
    }

    public class NormalDestributionTimeSelector implements TimeSelector{
        private final Random random = new Random();
        private long mean;
        private long stdDeviation;

        public NormalDestributionTimeSelector(long mean, long stdDeviation){
            this.mean = mean;
            this.stdDeviation = stdDeviation;
        }

        @Override
        public long getTime(){
            return Math.round(random.nextGaussian() * mean + stdDeviation);
        }
    }

    public class RoundRobinInjector implements Injector{
        private int injectorIndex;
        private int anomalyInjectionCounter;


        public RoundRobinInjector(){
            injectorIndex = 0;
        }

        @Override
        public AnomalyGroup getNextAnomaly(){
            //Run anomalies on targets one after another
            if(injectorIndex < injectorAgentController.size()) {
                InjectorAgentController injectorTarget = injectorAgentController.get(injectorIndex++);
                AnomalyGroup a = injectorTarget.getNextAnomaly();
                if (a != null) {
                    anomalyInjectionCounter++;
                    currentInjectorAgentController = injectorTarget;
                    return a;
                }else{
                    getNextAnomaly();
                }
            }else{
                injectorIndex = 0;
                //After EACH injector target executed ALL of its anomalies, all injector target anomalies
                //are reset and can be started in the next round.
                if(anomalyInjectionCounter == 0){
                    for (InjectorAgentController iac : injectorAgentController) {
                        iac.resetNextAnomalyIndex();
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

//    public class ServiceGroupRoundRobinInjector implements Injector{
//        private int currentGroupIndex;
//        private List<String> groups;
//
//        public ServiceGroupRoundRobinInjector(){
//            groups = new ArrayList<>();
//            groups.addAll(injectorTargets.keySet());
//            currentGroupIndex = 0;
//        }
//
//        @Override
//        public Anomaly getNextAnomaly() {
//            String group = groups.get(currentGroupIndex % groups.size());
//            currentGroupIndex++;
//            InjectorTarget injectorTarget = (InjectorTarget) injectorTargets.get(group).get(
//                    ThreadLocalRandom.current().nextInt(0, injectorTargets.get(group).size()));
//
//            Anomaly a = injectorTarget.getNextAnomaly();
//            if(a == null){
//                injectorTarget.resetNextAnomalyIndex();
//                a = injectorTarget.getNextAnomaly();
//            }
//            for(Target t : injectorTargets.get(group))
//                ((InjectorTarget) t).setAnomalyIndex(injectorTarget.getAnomalyIndex());
//
//            currentInjectorTarget = injectorTarget;
//            return a;
//        }
//
//        @Override
//        public void injectNextAnomaly(Anomaly a, long backupRevertTime) {
//            currentInjectorTarget.startNextAnomaly(a, backupRevertTime);
//        }
//    }
}
