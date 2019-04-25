package anomaly.experiment.controller.objects;

import java.util.List;
import java.util.Map;

/**
 * Created by alex on 12.04.19.
 */
public class AnomalyScenario {

    private Map<String, List<AnomalyGroup>> anomalyGroups;
    private List<Host> hosts;

    public AnomalyScenario() {
    }

    public AnomalyScenario(List<Host> hosts) {
        this.hosts = hosts;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    public Map<String, List<AnomalyGroup>> getAnomalyGroups() {
        return anomalyGroups;
    }

    public void setAnomalyGroups(Map<String, List<AnomalyGroup>> anomalyGroups) {
        this.anomalyGroups = anomalyGroups;
    }
}
