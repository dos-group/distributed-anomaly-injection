package bitflow.distributedexperiments;

import bitflow.distributedexperiments.load_control.RunLoadChanging;
import bitflow.distributedexperiments.load_control.RunLoadInterface;
import bitflow.distributedexperiments.objects.Anomaly;
import bitflow.distributedexperiments.objects.InjectorStatus;
import bitflow.distributedexperiments.objects.targets.CollectorTarget;
import bitflow.distributedexperiments.objects.targets.InjectorTarget;
import bitflow.distributedexperiments.objects.targets.Target;

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

    private Map<String, List<Target>> injectorTargets;
    private Map<String, List<Target>> collectorTargets;

    private RunLoadInterface loadController;
    private TimeSelector anomalyTimeSelector;
    private TimeSelector loadTimeSelector;

    private InjectorTarget currentInjectorTarget = null;
    private boolean shutdown = false;

    private Injector anomalyInjector;

    public interface Injector{
        void injectNextAnomaly(Anomaly a, long backupRevertTime);
        Anomaly getNextAnomaly();
    }


    public DistributedExperimentController(Map<String, List<Target>> injectorTargets,
                                           Map<String, List<Target>> collectorTargets,
                                           Injector anomalyInjector){
        this.collectorTargets = collectorTargets;
        this.injectorTargets = injectorTargets;
        this.anomalyInjector = anomalyInjector;
        this.anomalyTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.loadTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
    }

    public DistributedExperimentController(Map<String, List<Target>> injectorTargets,
                                           Map<String, List<Target>> collectorTargets,
                                           RunLoadChanging loadController, Injector anomalyInjector) {
        this.injectorTargets = injectorTargets;
        this.collectorTargets = collectorTargets;
        this.loadController = loadController;
        this.anomalyInjector = anomalyInjector;
        this.anomalyTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
        this.loadTimeSelector = new DistributedExperimentController.ConstantTimeSelector(DEFAULT_TIME_VALUE);
    }

    public void setAnomalyTimeSelector(TimeSelector anomalyTimeSelector) {
        this.anomalyTimeSelector = anomalyTimeSelector;
    }

    public void setLoadTimeSelector(TimeSelector loadTimeSelector) {
        this.loadTimeSelector = loadTimeSelector;
    }

    public RunLoadInterface getLoadController() {
        return loadController;
    }

    public void setLoadController(RunLoadInterface loadController) {
        this.loadController = loadController;
    }

    public Injector getAnomalyInjector() {
        return anomalyInjector;
    }

    public void setAnomalyInjector(Injector anomalyInjector) {
        this.anomalyInjector = anomalyInjector;
    }

    public void startExperiment(long endTime, boolean initKnownAnomalies){
        this.startExperiment(endTime, 0, initKnownAnomalies);
    }

    public void startExperiment(long endTime, long initialDelay, boolean initKnownAnomalies){
        logger.log(Level.INFO, "Starting experiment. Expected end of experiment: " +
                DATE_TIME_FORMAT.format(new Date(endTime)));

        //Log targets on which the experiments should be executed
        logTargets("Injector", injectorTargets);
        logTargets("Collector", collectorTargets);
        //Initialize each injector target with known anomalies
        if(initKnownAnomalies)
            this.initializeKnownAnomalies();
        //Log injector targets with anomalies
        this.logTargets("Injector", injectorTargets);

        //Reset collector data sort_data
        this.unsetTags(new HashSet<>(Arrays.asList(CLS_KEY, ANOMALY_TAG_KEY)));
        this.setClsTags();

        //Start file output at collectors
        this.startFileOutputAtCollectors();
        //Start generating random load on the system
        if(loadController != null)
            loadController.runLoad();

        if(injectorTargets.size() > 0) {
            try {
                //Initially delay execution of anomaly simulations
                if (initialDelay > 0) {
                    logger.log(Level.INFO, "Executing initial load...");
                    Thread.sleep(initialDelay);
                }
                //Run anomaly injection experiment
                this.runExperiment(endTime);
            } catch (InterruptedException e) {
            }
        }else{
            //Run idle loop if no anomaly simulations should be run
            try {
                this.runIdle(endTime);
            } catch (InterruptedException e) {}
        }

        //Shutdown experiment
        this.shutdown();
    }

    public synchronized void shutdown(){
        //Set shutdown flag
        this.shutdown = true;
        //If there is some anomaly running --> stop it
        if(currentInjectorTarget != null){
            if(currentInjectorTarget.getCurrentRunningAnomaly() != null) {
                Anomaly a = currentInjectorTarget.getCurrentRunningAnomaly();
                currentInjectorTarget.stopAnomaly(a);
                if(a.getRecoveryTags() != null)
                    this.unsetTags(new HashSet<>(a.getRecoveryTags().keySet()));
            }
        }
        //Delete all tags at collectors
        this.unsetTags(new HashSet<>(Arrays.asList(CLS_KEY, ANOMALY_TAG_KEY)));
        //Stop data collection at collector targets
        stopFileOutputAtCollectors();
        //Shutdown load changer
        if(loadController != null)
            loadController.shutdown();
    }

    /**
     * Get status of injector targets and derive known anomalies of each injector.
     * Remove target if something goes wrong during the status get request.
     */
    private void initializeKnownAnomalies(){
        InjectorStatus injectorStatus;
        for(List<Target> l : injectorTargets.values()) {
            for (Iterator<Target> iterator = l.iterator(); iterator.hasNext(); ) {
                InjectorTarget injectorTarget = (InjectorTarget) iterator.next();
                injectorStatus = injectorTarget.getInjectorStatus();
                if (injectorStatus != null) {
                    injectorTarget.setAnomalies(injectorStatus.getKnownAnomalies());
                } else {
                    iterator.remove();
                    logger.log(Level.WARNING, "Failed to get known anomalies from injector {0}. Target was removed from experiment injection list.", injectorTarget.getName());
                }
            }
        }
    }

    private void logTargets(String msg, Map<String, List<Target>> targets) {
        for(List<Target> l : targets.values())
            for(Target t : l)
                logger.log(Level.INFO, msg + ": " + t.toString());
    }

    private void startFileOutputAtCollectors() {
        for(List<Target> l : collectorTargets.values()){
            for(Target t : l) {
                CollectorTarget collectorTarget = (CollectorTarget) t;
                collectorTarget.startFileOutput();
            }
        }
    }

    private void stopFileOutputAtCollectors() {
        for(List<Target> l : collectorTargets.values()){
            for(Target t : l) {
                CollectorTarget collectorTarget = (CollectorTarget) t;
                collectorTarget.stopFileOutput();
            }
        }
    }

    private void runExperiment(long stopTime) throws InterruptedException {
        //Auxiliary variables
        Set<String> clsTagKeys = new HashSet<>(Arrays.asList(CLS_KEY));
        Set<String> anomalyTagKeys = new HashSet<>(Arrays.asList(ANOMALY_TAG_KEY));
        Set<String> tagCombination;
        int anomalyExecutionCounter = 0, injectorIndex = 0;

        //Loop as long as the end time is not reached
        while(new Date().getTime() < stopTime && !shutdown) {
            long anomalyRuntime = anomalyTimeSelector.getTime();
            Anomaly a = anomalyInjector.getNextAnomaly();
            if(a != null) {
                anomalyExecutionCounter++;
                this.unsetTags(clsTagKeys);
                this.setAnomalyTags(a, currentInjectorTarget.getName());
                anomalyInjector.injectNextAnomaly(a, anomalyRuntime + 10000);
                Thread.sleep(anomalyRuntime);
                currentInjectorTarget.stopAnomaly(a);
                this.currentInjectorTarget = null;
                tagCombination = new HashSet<>(anomalyTagKeys);
                if (a.getRecoveryTags() != null)
                    tagCombination.addAll(a.getRecoveryTags().keySet());
                this.unsetTags(tagCombination);
                this.setClsTags();
                Thread.sleep(loadTimeSelector.getTime());
            }
        }
    }

    private void runIdle(long stopTime) throws InterruptedException {
        while(new Date().getTime() < stopTime && !shutdown) {
            Thread.sleep(IDLE_TIME_VALUE);
        }
    }

    private void unsetTags(Set<String> tagKeys) {
        for(List<Target> l : collectorTargets.values()) {
            for (Target t : l) {
                CollectorTarget collectorTarget = (CollectorTarget) t;
                collectorTarget.removeTags(tagKeys);
            }
        }
    }

    private void setAnomalyTags(Anomaly a, String injectorTargetName) {
        Map<String, String> tags = new HashMap<>();
        tags.put(ANOMALY_TAG_KEY, injectorTargetName + "|" + a.getName());
        if(a.getRecoveryTags() != null)
            tags.putAll(a.getRecoveryTags());
        for(List<Target> l : collectorTargets.values()) {
            for (Target t : l) {
                CollectorTarget collectorTarget = (CollectorTarget) t;
                collectorTarget.setTags(tags);
            }
        }
    }

    private void setClsTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put(CLS_KEY, DEFAULT_CLS_VALUE);
        for(List<Target> l : collectorTargets.values()) {
            for (Target t : l) {
                CollectorTarget collectorTarget = (CollectorTarget) t;
                collectorTarget.setTags(tags);
            }
        }
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
        private List<Target> injectors;
        private int injectorIndex;
        private int anomalyInjectionCounter;


        public RoundRobinInjector(){
            injectorIndex = 0;
            injectors = new ArrayList<>();
            for(List<Target> l : injectorTargets.values())
                injectors.addAll(l);
        }

        @Override
        public Anomaly getNextAnomaly(){
            //Run anomalies on targets one after another
            if(injectorIndex < injectors.size()) {
                InjectorTarget injectorTarget = (InjectorTarget) injectors.get(injectorIndex++);
                Anomaly a = injectorTarget.getNextAnomaly();
                if (a != null) {
                    anomalyInjectionCounter++;
                    currentInjectorTarget = injectorTarget;
                    return a;
                }else{
                    getNextAnomaly();
                }
            }else{
                injectorIndex = 0;
                //After EACH injector target executed ALL of its anomalies, all injector target anomalies
                //are reset and can be started in the next round.
                if(anomalyInjectionCounter == 0){
                    for (Target t : injectors) {
                        ((InjectorTarget) t).resetNextAnomalyIndex();
                    }
                }
                anomalyInjectionCounter = 0;
            }
            return null;
        }

        @Override
        public void injectNextAnomaly(Anomaly a, long backupRevertTime) {
            //Run anomalies on target
            currentInjectorTarget.startNextAnomaly(a, backupRevertTime);
        }
    }

    public class ServiceGroupRoundRobinInjector implements Injector{
        private int currentGroupIndex;
        private List<String> groups;

        public ServiceGroupRoundRobinInjector(){
            groups = new ArrayList<>();
            groups.addAll(injectorTargets.keySet());
            currentGroupIndex = 0;
        }

        @Override
        public Anomaly getNextAnomaly() {
            String group = groups.get(currentGroupIndex % groups.size());
            currentGroupIndex++;
            InjectorTarget injectorTarget = (InjectorTarget) injectorTargets.get(group).get(
                    ThreadLocalRandom.current().nextInt(0, injectorTargets.get(group).size()));

            Anomaly a = injectorTarget.getNextAnomaly();
            if(a == null){
                injectorTarget.resetNextAnomalyIndex();
                a = injectorTarget.getNextAnomaly();
            }
            for(Target t : injectorTargets.get(group))
                ((InjectorTarget) t).setAnomalyIndex(injectorTarget.getAnomalyIndex());

            currentInjectorTarget = injectorTarget;
            return a;
        }

        @Override
        public void injectNextAnomaly(Anomaly a, long backupRevertTime) {
            currentInjectorTarget.startNextAnomaly(a, backupRevertTime);
        }
    }
}
