package bitflow.distributedexperiments.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.util.*;

/**
 *
 * @author fschmidt
 */
public class Anomaly {

    @JsonIgnore
    private Set<String> ids = new HashSet<>();
    private String name;
    @JsonProperty("currently_running")
    private boolean currentlyRunning;
    @JsonProperty("default_param")
    private String defaultParam;
    @JsonIgnore
    private Map<String, Parameters> parameters = new HashMap<>();
    @JsonIgnore
    private boolean useDefaultParams = true;
    @JsonIgnore
    private Map<String, String> recoveryTags;

    public Anomaly(){
        
    }

    public Anomaly(Set<String> ids, String name, Map<String, Parameters> parameters,
                   boolean useDefaultParams, Map<String, String> recoveryTags) {
        this.ids = ids;
        this.name = name;
        this.parameters = parameters;
        this.useDefaultParams = useDefaultParams;
        this.recoveryTags = recoveryTags;
    }

    public Anomaly(Set<String> ids, String name, Map<String, Parameters> parameters,
                   boolean useDefaultParams) {
        this(ids, name, parameters, useDefaultParams, null);
    }

    public Set<String> getIds() {
        if(ids.isEmpty())
            return new HashSet<>(Arrays.asList(name));
        return ids;
    }

    public void setIds(HashSet<String> ids) {
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getCurrentlyRunning() {
        return currentlyRunning;
    }

    public void setCurrentlyRunning(boolean currentlyRunning) {
        this.currentlyRunning = currentlyRunning;
    }

    public String getDefaultParam() {
        return defaultParam;
    }

    public Parameters getParameter(String anomalyID) {
        return parameters.get(anomalyID);
    }

    public boolean getUseDefaultParams() {
        return useDefaultParams;
    }

    public void setUseDefaultParams(boolean useDefaultParams) {
        this.useDefaultParams = useDefaultParams;
    }

    public Map<String, String> getRecoveryTags() {
        return recoveryTags;
    }

    public void setRecoveryTags(Map<String, String> recoveryTags) {
        this.recoveryTags = recoveryTags;
    }

    public void addRecoveryTag(String key, String value){
        this.recoveryTags.put(key, value);
    }

    public JSONObject getParamsAsJSON(String id){
        return parameters.get(id).getParamsAsJSON();
    }
}
