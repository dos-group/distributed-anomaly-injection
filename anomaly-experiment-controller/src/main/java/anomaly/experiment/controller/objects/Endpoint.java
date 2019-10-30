package anomaly.experiment.controller.objects;

public class Endpoint {
    private String name;
    private String component;
    private String endpoint;

    public Endpoint() {
    }

    public Endpoint(String name, String component, String endpoint) {
        this.name = name;
        this.component = component;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) -> %s ", this.name, this.component, this.endpoint);
    }
}
