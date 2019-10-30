package anomaly.experiment.controller.objects;

import anomaly.experiment.controller.requests.RequestSender;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 22.02.19.
 */
public class CollectorAgentController {

    private static final Logger logger = Logger.getLogger(CollectorAgentController.class.getName());

    private static final String FILE_OUTPUT_PATH = "/api/file_output";
    private static final String TAGS_PATH = "/api/tags";
    private static final String TAG_PATH = "/api/tag";

    private Endpoint endpoint;
    private RequestSender requestController;

    public CollectorAgentController(Endpoint endpoint, RequestSender requestController) {
        this.endpoint = endpoint;
        this.requestController = requestController;
    }

    public void startFileOutput() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Starting file output of collector " + endpoint.getName() + " at " +
                getFileOutputAPIEndpoint());
        if (!requestController.post(this.getFileOutputAPIEndpoint()))
            logger.warning("Unable to start file output.");
    }

    public void stopFileOutput() throws InvalidParameterException {
        checkRequestController();
        logger.log(Level.INFO, "Stopping file output of collector " + endpoint.getName() + " at " +
                getFileOutputAPIEndpoint());
        if (!requestController.delete(this.getFileOutputAPIEndpoint(), false))
            logger.warning("Unable to stop file output.");
    }

    private String getFileOutputAPIEndpoint() {
        return this.endpoint.getEndpoint() + FILE_OUTPUT_PATH;
    }

    public void setTags(Map<String, String> tags) {
        checkRequestController();
        logger.log(Level.INFO, "Setting tags " + mapToString(tags) +
                " on collector " + endpoint.getName() + " at " + getTagsAPIEndpoint());
        if (!requestController.query(this.getTagsAPIEndpoint(), tags))
            logger.warning("Unable to set tags.");
    }

    private String getTagsAPIEndpoint() {
        return this.endpoint.getEndpoint() + TAGS_PATH;
    }

    public void removeTags(Set<String> tagKeys) {
        checkRequestController();
        logger.log(Level.INFO, "Removing tags " + setToString(tagKeys) +
                " on collector " + endpoint.getName() + " at " + getTagsAPIEndpoint());
        boolean success = true;
        for (String tagKey : tagKeys)
            success = success && requestController.delete(this.getTagAPIEndpoint(tagKey), false);
        if (!success)
            logger.warning("Unable to reset tags.");
    }

    private String getTagAPIEndpoint(String tagKey) {
        return this.endpoint.getEndpoint() + TAG_PATH + "/" + tagKey;
    }

    private String mapToString(Map<String, String> map) {
        String result = "";
        for (Map.Entry<String, String> entry : map.entrySet())
            result += "(" + entry.getKey() + "=" + entry.getValue() + ")";
        return result;
    }

    private String setToString(Set<String> set) {
        String result = "";
        for (String entry : set)
            result += entry + ", ";
        return "(" + result.substring(0, result.length() - 2) + ")";
    }

    private void checkRequestController() throws InvalidParameterException {
        if (requestController == null)
            throw new InvalidParameterException("Request controller is undefined.");
    }

    @Override
    public String toString() {
        return endpoint.toString();
    }
}
