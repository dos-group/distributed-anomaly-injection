package anomaly.experiment.controller.objects;

import java.util.List;

/**
 * Created by alex
 */
public class Host {

    private String name;
    private String endpoint;

    private List<AnomalyGroup> anomalyGroups;

//    Runtime control
    private AnomalyGroup currentAnomaly;
    private int index;


    public Host(String name, String endpoint, List<AnomalyGroup> anomalyGroups) {
        this.name = name;
        this.endpoint = endpoint;
        this.anomalyGroups = anomalyGroups;
        this.index = 0;
    }

    public Host() {
        this(null, null, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<AnomalyGroup> getAnomalyGroups() {
        return anomalyGroups;
    }

    public void setAnomalyGroups(List<AnomalyGroup> anomalyGroups) {
        this.anomalyGroups = anomalyGroups;
    }

    @Override
    public String toString(){
        String anomaliesString = "[";
        if(anomalyGroups != null && !anomalyGroups.isEmpty()) {
            for (AnomalyGroup a : anomalyGroups) {
                anomaliesString += a.toString() + ", ";
            }
            anomaliesString = anomaliesString.substring(0,
                    anomaliesString.length() - 2) + "]";
        }else{
            anomaliesString += "]";
        }
        return name + " (" + this.getEndpoint() + ") " + "\n Known Anomalies: " + anomaliesString;
    }
}
