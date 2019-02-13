package bitflow.distributedexperiments.objects.targets;

import bitflow.distributedexperiments.objects.Anomaly;
import bitflow.distributedexperiments.objects.InjectorStatus;
import bitflow.distributedexperiments.requests.RequestSenderJSON;
import bitflow.distributedexperiments.utils.filter.Filter;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by alex on 27.08.17.
 */
public class InjectorTarget extends Target{

    public static final String ANOMALY_PATH = "/api/anomalies/";
    public static final String STATUS_PATH = "/api/status/";

    private Filter.NameFilter anomalyFilter;
    private List<Anomaly> anomalies = new ArrayList<>();
    private int index = 0;

    //TODO: Make list of currently running anomalies.
    private Anomaly currentRunningAnomaly = null;


    public InjectorTarget(String name, String address, String port, String group, List<Anomaly> anomalies) {
        super(name, address, port, group);
        this.anomalies = anomalies;
    }

    public InjectorTarget(String address, String port, List<Anomaly> anomalies) {
        super(address, port);
        this.anomalies = anomalies;
    }

    @Override
    protected String[] getRecoveryCmdArgs() {
        return new String[] {
                "-l",
                name,
                "-e",
                "container=injector"
        };
    }

    public InjectorTarget(String name, String address, String port, String group) {
        super(name, address, port, group);
    }

    public InjectorTarget(String address, String port) {
        super(address, port);
    }

    public List<Anomaly> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<Anomaly> anomalies) {
        this.anomalies = anomalies;
    }

    public Anomaly getCurrentRunningAnomaly(){ return this.currentRunningAnomaly; }

    public int getAnomalyIndex() {
        return index;
    }

    public void setAnomalyIndex(int index) {
        this.index = index;
    }

    public Filter.NameFilter getAnomalyFilter() {
        return anomalyFilter;
    }

    public void setAnomalyNameFilter(Filter.NameFilter anomalyFilter) {
        this.anomalyFilter = anomalyFilter;
    }

    public Anomaly getNextAnomaly(){
        Anomaly result = null;
        if(index < anomalies.size()) {
            result = anomalies.get(index);
            index++;
            if(anomalyFilter != null) {
                if (!anomalyFilter.shouldInclude(result.getName())) {
                    result = this.getNextAnomaly();
                }
            }
        }
        return result;
    }

    public void resetNextAnomalyIndex(){
        index = 0;
    }

    public InjectorStatus getInjectorStatus() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Getting status from " + name + " at " + getStatusAPIEndpoint());
        return requestController.get(this.getStatusAPIEndpoint(), InjectorStatus.class);
    }

    private String getStatusAPIEndpoint(){
        return this.getAddress() + ":" + port + STATUS_PATH;
    }

    public Anomaly startNextAnomaly(Anomaly a, long backupRevertTime) throws InvalidParameterException {
        checkRequestController();
        if(a != null) {
            logger.log(Level.INFO, "Starting anomaly " + a.getName() + " on:" + name + "at:");
            JSONObject params;
            for(String anomalyID : a.getIds()) {
                logger.log(Level.INFO, getAnomalyAPIEndpoint(anomalyID));
                if(a.getUseDefaultParams())
                    params = new JSONObject();
                else
                    params = a.getParamsAsJSON(anomalyID);
                params.put("time", (backupRevertTime / 1000)+"");
                ((RequestSenderJSON) requestController).post(this.getAnomalyAPIEndpoint(anomalyID),
                        params);
            }
        }
        this.currentRunningAnomaly = a;
        return a;
    }

    public Anomaly stopAnomaly(Anomaly a) throws InvalidParameterException {
        checkRequestController();
        if(a != null) {
            logger.log(Level.INFO, "Stopping anomaly " + a.getName() + " on " + name + " at:");
            for(String anomalyID : a.getIds()) {
                logger.log(Level.INFO, getAnomalyAPIEndpoint(anomalyID));
                requestController.delete(this.getAnomalyAPIEndpoint(anomalyID));
            }
        }
        this.currentRunningAnomaly = null;
        return a;
    }

    private String getAnomalyAPIEndpoint(String anomaly){
        return this.getAddress() + ":" + port + ANOMALY_PATH + anomaly + "/";
    }

    @Override
    public String toString(){
        String anomaliesString = "[";
        if(!anomalies.isEmpty()) {
            for (Anomaly a : anomalies) {
                anomaliesString += a.getName() + ", ";
            }
            anomaliesString += anomaliesString.substring(0,
                    anomaliesString.length() - 2) + "]";
        }else{
            anomaliesString += "]";
        }
        return super.toString() + "\n Known Anomalies: " + anomaliesString;
    }
}
