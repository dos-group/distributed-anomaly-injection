package anomaly.experiment.controller.requests;

import org.json.JSONObject;

import java.util.Map;

public interface RequestSender {

    boolean post(String targetAPIEndpoint);

    boolean post(String targetAPIEndpoint, JSONObject data);

    boolean delete(String targetAPIEndpoint, boolean isResponseJson);

    <T> T get(String targetAPIEndpoint, Class<? extends T> type);

    boolean query(String targetAPIEndpoint, Map<String, String> queryElements);
}
