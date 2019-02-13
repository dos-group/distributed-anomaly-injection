package bitflow.distributedexperiments.objects.targets;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by alex on 27.08.17.
 */
public class CollectorTarget extends Target{
    private static final String FILE_OUTPUT_PATH = "/api/file_output";
    private static final String TAGS_PATH = "/api/tags";
    private static final String TAG_PATH = "/api/tag";

    @Override
    protected String[] getRecoveryCmdArgs() {
        return new String[] {
                "-l",
                name,
                "-e",
                "container=bitflow-collector"
        };
    }

    public CollectorTarget(String name, String address, String port, String group) {
        super(name, address, port, group);
    }

    public CollectorTarget(String address, String port) {
        super(address, port);
    }

    public void startFileOutput() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Starting file output of collector " + name + " at " + getFileOutputAPIEndpoint());
        if(!requestController.post(this.getFileOutputAPIEndpoint()))
            this.recover();
    }

    public void stopFileOutput() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Stopping file output of collector " + name + " at " + getFileOutputAPIEndpoint());
        requestController.delete(this.getFileOutputAPIEndpoint());
    }

    private String getFileOutputAPIEndpoint(){
        return this.getAddress() + ":" + port + FILE_OUTPUT_PATH ;
    }

    public void setTags(Map<String, String> tags){
        checkRequestController();
        logger.log(Level.INFO, "Setting tags " + mapToString(tags) +
                " on collector " + name + " at " + getTagsAPIEndpoint());
        if(!requestController.query(this.getTagsAPIEndpoint(), tags))
            this.recover();
    }

    private String getTagsAPIEndpoint(){
        return this.getAddress() + ":" + port + TAGS_PATH ;
    }

    public void removeTags(Set<String> tagKeys){
        checkRequestController();
        logger.log(Level.INFO, "Removing tags " + setToString(tagKeys) +
                " on collector " + name + " at " + getTagsAPIEndpoint());
        boolean success = true;
        for(String tagKey : tagKeys)
            success = success && requestController.delete(this.getTagAPIEndpoint(tagKey));
        if(!success)
            this.recover();
    }

    private String getTagAPIEndpoint(String tagKey){
        return this.getAddress() + ":" + port + TAG_PATH + "/" + tagKey ;
    }

    private String mapToString(Map<String, String> map){
        String result = "";
        for(Map.Entry<String, String> entry : map.entrySet())
            result += "(" + entry.getKey() + "=" + entry.getValue() + ")";
        return result;
    }

    private String setToString(Set<String> set){
        String result = "";
        for(String entry : set)
            result += entry + ", ";
        return "(" + result.substring(0, result.length() - 2) + ")";
    }
}
