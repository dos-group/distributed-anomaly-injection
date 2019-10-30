package anomaly.experiment.controller.objects;

import org.apache.commons.codec.binary.StringUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by alex
 */
public class HostGroup {

    private final static Random random = new Random();

    private String name;
    private List<Endpoint> endpoints;

    private List<AnomalyGroup> anomalyGroups;

//    Runtime control
    private AnomalyGroup currentAnomaly;
    private Endpoint currentEndpoint;


    public HostGroup(String name, List<Endpoint> endpoints, List<AnomalyGroup> anomalyGroups) {
        this.name = name;
        this.endpoints = endpoints;
        this.anomalyGroups = anomalyGroups;
    }

    public HostGroup() {
        this(null, null, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Endpoint> getEndpoints() {
        return this.endpoints;
    }

    public Endpoint getRandomEndpoint() {
        int randomIndex = random.nextInt(this.endpoints.size());
        return this.endpoints.get(randomIndex);
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public List<AnomalyGroup> getAnomalyGroups() {
        return anomalyGroups;
    }

    public void setAnomalyGroups(List<AnomalyGroup> anomalyGroups) {
        this.anomalyGroups = anomalyGroups;
    }

    @Override
    public String toString(){
        String anomaliesString = "[ ]";
        if(this.anomalyGroups != null && !this.anomalyGroups.isEmpty()) {
            anomaliesString = this.anomalyGroups.stream().map(String::valueOf).collect(
                    Collectors.joining(", ", "[ ", " ]"));
        }
        String endpointsString = "";
        if(this.endpoints != null && !this.endpoints.isEmpty()) {
            endpointsString = this.endpoints.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        return String.format("Host group name: %s\nAnomaly injector agent endpoints: %s\nKnown Anomalies: %s",
                this.name, endpointsString, anomaliesString);
    }
}
