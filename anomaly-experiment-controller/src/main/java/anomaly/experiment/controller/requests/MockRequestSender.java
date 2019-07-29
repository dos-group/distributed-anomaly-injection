package anomaly.experiment.controller.requests;

import org.json.JSONObject;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MockRequestSender implements RequestSender {

    protected static final Logger logger = Logger.getLogger(MockRequestSender.class.getName());


    @Override
    public boolean post(String targetAPIEndpoint) {
        logger.log(Level.INFO, "POST request successful.");
        return true;
    }

    @Override
    public boolean post(String targetAPIEndpoint, JSONObject data) {
        logger.log(Level.INFO, "POST request successful.");
        return true;
    }

    @Override
    public boolean delete(String targetAPIEndpoint, boolean isResponseJson) {
        logger.log(Level.INFO, "DELETE request successful.");
        return true;
    }

    @Override
    public <T> T get(String targetAPIEndpoint, Class<? extends T> type) {
        logger.log(Level.INFO, "POST request successful.");
        T instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            instance = null;
        }
        return instance;
    }

    @Override
    public boolean query(String targetAPIEndpoint, Map<String, String> queryElements) {
        logger.log(Level.INFO, "DELETE request successful.");
        return true;
    }
}
