package anomaly.experiment.controller.objects;

import anomaly.experiment.controller.requests.HTTPRequestSender;
import anomaly.experiment.controller.requests.RequestSender;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 20.02.19.
 */
public class InjectorAgentController {

    private static final Logger logger = Logger.getLogger(InjectorAgentController.class.getName());

    public static final String ANOMALY_PATH = "/api/anomalies/";
    public static final String STATUS_PATH = "/api/status/";

    private static final String HTTP_PREFIX = "http://";

    private final Host host;
    private final RequestSender requestController;

    private final int anomalyMaxInjectionCount;


//    Runtime control
    private int index;
    private AnomalyGroup currentAnomaly;
    private Map<AnomalyGroup, Integer> injectionCounter;

    public InjectorAgentController(Host host, RequestSender requestController, int anomalyMaxInjectionCount) {
        this.host = host;
        this.requestController = requestController;
        this.anomalyMaxInjectionCount = anomalyMaxInjectionCount;
        index = 0;
        if(anomalyMaxInjectionCount > 0) {
            this.injectionCounter = new HashMap<>();
            for(AnomalyGroup anomalyGroup : this.host.getAnomalyGroups())
                injectionCounter.put(anomalyGroup, 0);
        }
    }

    public AnomalyGroup getCurrentAnomaly() {
        return currentAnomaly;
    }

    public int getAnomalyIndex() {
        return index;
    }

    public void setAnomalyIndex(int index) {
        this.index = index;
    }

    public Host getHost() {
        return host;
    }

    public int getAnomalyMaxInjectionCount() {
        return anomalyMaxInjectionCount;
    }

    public AnomalyGroup getNextAnomaly(){
        AnomalyGroup result = null;
        if(index < host.getAnomalyGroups().size()) {
            result = host.getAnomalyGroups().get(index++);
            // Limit the number of injectable anomalies
            if(anomalyMaxInjectionCount > 0){
                if(injectionCounter.containsKey(result) && injectionCounter.get(result) > anomalyMaxInjectionCount){
                    result = null;
                } else {
                    // Is initialized at constructor
                    injectionCounter.put(result, injectionCounter.get(result) + 1);
                }
            }
        }
        return result;
    }

    public void resetNextAnomalyIndex(){
        index = 0;
    }

    public boolean isMaxInjectionCountReached(){
        boolean isReached = true;
        if(anomalyMaxInjectionCount < 0){
            isReached = false;
        } else {
            // Every anomaly group count must be reached to make result true
            for(Integer count : this.injectionCounter.values()){
                isReached = isReached && count >= anomalyMaxInjectionCount;
            }
        }
        return isReached;
    }

    public InjectorStatus getInjectorStatus() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Getting status from " + host.getName() + " at " + getStatusAPIEndpoint());
        return requestController.get(this.getStatusAPIEndpoint(), InjectorStatus.class);
    }

    private String getStatusAPIEndpoint(){
        return host.getEndpoint() + ":" + STATUS_PATH;
    }

    public AnomalyGroup startNextAnomaly(AnomalyGroup anomalyGroup, int backupRevertTime)
            throws InvalidParameterException {
        checkRequestController();
        if(anomalyGroup != null) {
            logger.log(Level.INFO, "Starting anomaly " + anomalyGroup.toString() + " on:" + host.getName() + " at: ");
            JSONObject params;
            for(Anomaly anomaly : anomalyGroup.getAnomalies()) {
                String anomalyID = anomaly.getId_name();
                logger.log(Level.INFO, getAnomalyAPIEndpoint(anomalyID));
                params = anomaly.getParamsAsJSON();
                params.put("time", backupRevertTime+"");
                requestController.post(this.getAnomalyAPIEndpoint(anomalyID), params);
            }
        }
        this.currentAnomaly = anomalyGroup;
        return anomalyGroup;
    }

    public AnomalyGroup stopAnomaly(AnomalyGroup anomalyGroup) throws InvalidParameterException {
        checkRequestController();
        if(anomalyGroup != null) {
            logger.log(Level.INFO, "Stopping anomaly " + anomalyGroup.toString() + " on " + host.getName() + " at: ");
            for(Anomaly anomaly : anomalyGroup.getAnomalies()) {
                String anomalyID = anomaly.getId_name();
                logger.log(Level.INFO, getAnomalyAPIEndpoint(anomalyID));
                requestController.delete(this.getAnomalyAPIEndpoint(anomalyID), true);
            }
        }
        this.currentAnomaly = null;
        return anomalyGroup;
    }

    private String getAnomalyAPIEndpoint(String anomaly){
        return this.host.getEndpoint() + ANOMALY_PATH + anomaly + "/";
    }

    private void checkRequestController() throws InvalidParameterException{
        if(requestController == null)
            throw new InvalidParameterException("Request controller is undefined.");
    }

    @Override
    public String toString() {
        return host.toString();
    }
}
