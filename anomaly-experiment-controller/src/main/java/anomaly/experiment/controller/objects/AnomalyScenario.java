package anomaly.experiment.controller.objects;

import java.util.List;
import java.util.Map;

/**
 * Created by alex on 12.04.19.
 */
public class AnomalyScenario {

    private Map<String, List<AnomalyGroup>> anomalyGroups;
    private List<HostGroup> hostGroups;

    public AnomalyScenario() {
    }

    public AnomalyScenario(List<HostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public List<HostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<HostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public Map<String, List<AnomalyGroup>> getAnomalyGroups() {
        return anomalyGroups;
    }

    public void setAnomalyGroups(Map<String, List<AnomalyGroup>> anomalyGroups) {
        this.anomalyGroups = anomalyGroups;
    }
}
