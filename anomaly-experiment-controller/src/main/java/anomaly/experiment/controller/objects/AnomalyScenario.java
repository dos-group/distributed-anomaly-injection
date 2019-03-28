package anomaly.experiment.controller.objects;

import java.util.Map;

/**
 * Created by alex on 21.02.19.
 * Helper class to deserialize anomaly scenario definition YAML file.
 */
public class AnomalyScenario {

    private Map<String, AnomalyGroup> anomalyGroups;
    //List<Host> hosts;

    public AnomalyScenario(Map<String, AnomalyGroup> anomalyGroups) {
        this.anomalyGroups = anomalyGroups;
        //  this.hosts = hosts;
    }

    public AnomalyScenario() {
        this(null);
    }

    public Map<String, AnomalyGroup> getAnomalyGroups() {
        return anomalyGroups;
    }

    public void setAnomalyGroups(Map<String, AnomalyGroup> anomalyGroups) {
        this.anomalyGroups = anomalyGroups;
    }

//        public List<Host> getHosts() {
//            return hosts;
//        }
//
//        public void setHosts(List<Host> hosts) {
//            this.hosts = hosts;
//        }
}
