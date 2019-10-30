package anomaly.experiment.controller.objects;

import anomaly.experiment.controller.requests.RequestSender;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 20.02.19.
 */
public class HostGroupInjectionController {

    public static final String ANOMALY_PATH = "/api/anomalies/";
    public static final String STATUS_PATH = "/api/status/";
    private static final Logger logger = Logger.getLogger(HostGroupInjectionController.class.getName());
    private static final String HTTP_PREFIX = "http://";

    private final HostGroup hostGroup;
    private final RequestSender requestController;

    private final int anomalyMaxInjectionCount;

    private final AnomalyGroupSelector selector;


    //    Runtime control
    private Endpoint currentEndpoint;
    private AnomalyGroup currentAnomaly;
    private Map<AnomalyGroup, Integer> injectionCounter;

    public HostGroupInjectionController(HostGroup hostGroup, RequestSender requestController, int anomalyMaxInjectionCount,
                                        AnomalyGroupSelector selector) {
        this.hostGroup = hostGroup;
        this.requestController = requestController;
        this.anomalyMaxInjectionCount = anomalyMaxInjectionCount;
        if (anomalyMaxInjectionCount > 0) {
            this.injectionCounter = new HashMap<>();
            for (AnomalyGroup anomalyGroup : this.hostGroup.getAnomalyGroups())
                injectionCounter.put(anomalyGroup, 0);
        }
        this.selector = selector;
    }

    public HostGroupInjectionController(HostGroup hostGroup, RequestSender requestController, int anomalyMaxInjectionCount,
                                        String selector) {
        this(hostGroup, requestController, anomalyMaxInjectionCount, getAnomalyGroupSelector(selector));
    }

    private static AnomalyGroupSelector getAnomalyGroupSelector(String selector) {

        if (selector.equals("iter")) {
            return new IterativeAnomalyGroupSelector();
        } else if (selector.equals("random")) {
            return new RandomAnomalyGroupSelector();
        } else {
            throw new IllegalArgumentException(String.format("Unknown selector %s. Known selector are: %s, %s.",
                    selector, "iter", "random"));
        }
    }

    public AnomalyGroup getCurrentAnomaly() {
        return currentAnomaly;
    }

    public Endpoint getCurrentEndpoint() {
        return currentEndpoint;
    }

    public HostGroup getHostGroup() {
        return hostGroup;
    }

    public int getAnomalyMaxInjectionCount() {
        return anomalyMaxInjectionCount;
    }

    public AnomalyGroup getNextAnomaly() {
        AnomalyGroup result = null;
        result = this.selector.selectNextAnomalyGroup(this.hostGroup.getAnomalyGroups());
        if (result != null){
            this.currentEndpoint = this.hostGroup.getRandomEndpoint();
            if (anomalyMaxInjectionCount > 0) {
                if (injectionCounter.containsKey(result) && injectionCounter.get(result) > anomalyMaxInjectionCount) {
                    result = null;
                } else {
                    // Is initialized at constructor
                    injectionCounter.put(result, injectionCounter.get(result) + 1);
                }
            }
        }
        return result;
    }

    public void resetAnomalySelector() {
        this.selector.reset();
    }

    public boolean isMaxInjectionCountReached() {
        boolean isReached = true;
        if (anomalyMaxInjectionCount < 0) {
            isReached = false;
        } else {
            // Every anomaly group count must be reached to make result true
            for (Integer count : this.injectionCounter.values()) {
                isReached = isReached && count >= anomalyMaxInjectionCount;
            }
        }
        return isReached;
    }

    public InjectorStatus getInjectorStatus() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Getting status from " + hostGroup.getName() + " at " + getStatusAPIEndpoint());
        return requestController.get(this.getStatusAPIEndpoint(), InjectorStatus.class);
    }

    private String getStatusAPIEndpoint() {
        return hostGroup.getRandomEndpoint() + ":" + STATUS_PATH;
    }

    public AnomalyGroup startNextAnomaly(AnomalyGroup anomalyGroup, int backupRevertTime)
            throws InvalidParameterException {
        checkRequestController();
        if (anomalyGroup != null) {
            logger.log(Level.INFO, "Starting anomaly " + anomalyGroup.toString() + " on host group " +
                    hostGroup.getName() + " on endpoint " + this.currentEndpoint.toString() + ": ");
            JSONObject params;
            for (Anomaly anomaly : anomalyGroup.getAnomalies()) {
                String anomalyID = anomaly.getId_name();
                logger.log(Level.INFO, getAnomalyAPIEndpoint(this.currentEndpoint, anomalyID));
                params = anomaly.getParamsAsJSON();
                params.put("time", backupRevertTime + "");
                requestController.post(this.getAnomalyAPIEndpoint(this.currentEndpoint, anomalyID), params);
            }
        }
        this.currentAnomaly = anomalyGroup;
        return anomalyGroup;
    }

    public AnomalyGroup stopAnomaly(AnomalyGroup anomalyGroup) throws InvalidParameterException {
        checkRequestController();
        if (anomalyGroup != null) {
            logger.log(Level.INFO, "Stopping anomaly " + anomalyGroup.toString() + " on " + hostGroup.getName() + " at: ");
            for (Anomaly anomaly : anomalyGroup.getAnomalies()) {
                String anomalyID = anomaly.getId_name();
                logger.log(Level.INFO, getAnomalyAPIEndpoint(this.currentEndpoint, anomalyID));
                requestController.delete(this.getAnomalyAPIEndpoint(this.currentEndpoint, anomalyID), true);
            }
        }
        this.currentAnomaly = null;
        return anomalyGroup;
    }

    private String getAnomalyAPIEndpoint(Endpoint endpoint, String anomaly) {
        return endpoint.getEndpoint() + ANOMALY_PATH + anomaly + "/";
    }

    private void checkRequestController() throws InvalidParameterException {
        if (requestController == null)
            throw new InvalidParameterException("Request controller is undefined.");
    }

    @Override
    public String toString() {
        return hostGroup.toString();
    }

    private interface AnomalyGroupSelector {
        AnomalyGroup selectNextAnomalyGroup(List<AnomalyGroup> groups);
        void reset();
    }

    private static class IterativeAnomalyGroupSelector implements AnomalyGroupSelector {

        private int index;

        public IterativeAnomalyGroupSelector() {
            this.index = 0;
        }

        @Override
        public AnomalyGroup selectNextAnomalyGroup(List<AnomalyGroup> groups) {
            if (this.index < groups.size()) {
                return groups.get(this.index++);
            } else {
                return null;
            }
        }

        @Override
        public void reset() {
            this.index = 0;
        }
    }

    private static class RandomAnomalyGroupSelector implements AnomalyGroupSelector {

        private final Random random = new Random();

        @Override
        public AnomalyGroup selectNextAnomalyGroup(List<AnomalyGroup> groups) {
            int randomIndex = random.nextInt(groups.size());
            return groups.get(randomIndex);
        }

        @Override
        public void reset() {
        }
    }

}
