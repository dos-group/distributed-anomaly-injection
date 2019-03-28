package anomaly.experiment.controller.objects;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author alex
 */
public class Anomaly {

    private static final String JSON_PARAM_KEY = "parameter";

    private String id_name;
    private String parameters;

    public Anomaly(String id_name, String parameters) {
        this.id_name = id_name;
        this.parameters = parameters;
    }

    public Anomaly() {
        this(null, null);
    }

    public String getId_name() {
        return id_name;
    }

    public void setId_name(String id_name) {
        this.id_name = id_name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public JSONObject getParamsAsJSON(){
        Map<String, String> params = new HashMap<>();
        params.put(JSON_PARAM_KEY, parameters);
        return new JSONObject(params);
    }

    @Override
    public String toString() {
        return id_name + "(" + parameters + ")";
    }
}
