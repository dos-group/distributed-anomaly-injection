package anomaly.experiment.controller.objects;

import java.util.List;

/**
 * Created by alex
 */
public class AnomalyGroup {

    private String name;
    private List<Anomaly> anomalies;

    public AnomalyGroup(String name, List<Anomaly> anomalies) {
        this.name = name;
        this.anomalies = anomalies;
    }

    public AnomalyGroup(String name) {
        this(name, null);
    }

    public AnomalyGroup() {
        this(null, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Anomaly> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<Anomaly> anomalies) {
        this.anomalies = anomalies;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("");
        if(anomalies != null && !anomalies.isEmpty()) {
            if (anomalies.size() > 1) {
                str.append(name).append("{");
                for(Anomaly a : anomalies)
                    str.append(a.toString()).append(",");
                str.setLength(Math.max(str.length() - 1, 0)); //remove last comma
                str.append("}");
            } else {
                str.append(anomalies.get(0).toString());
            }
        }
        return str.toString();
    }
}
