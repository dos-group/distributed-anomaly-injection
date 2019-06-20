package anomaly.experiment.controller.requests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONObject;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fschmidt and alex on 27.08.17.
 */
public class HTTPRequestSender implements RequestSender {

    protected static final Logger logger = Logger.getLogger(HTTPRequestSender.class.getName());

    protected int maxRetries;
    protected long retryDelay;

    public HTTPRequestSender(int maxRetries, long retryDelay) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    @Override
    public boolean post(String targetAPIEndpoint) {
        boolean success = false;
        int retryCounter = 0;
        while (!success && retryCounter < maxRetries) {
            try {
                HttpResponse<String> resp = Unirest.post(targetAPIEndpoint).asString();
                success = true;
                this.logSuccess("POST", resp);
                break;
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if (!success)
            this.logFailure("POST");
        return success;
    }

    @Override
    public boolean post(String targetAPIEndpoint, JSONObject data) {
        boolean success = false;
        int retryCounter = 0;
        HttpResponse<String> resp = null;
        while (!success && retryCounter < maxRetries) {
            try {
                resp = Unirest.post(targetAPIEndpoint)
                        .header("accept", "application/json")
                        .header("Content-Type", "application/json")
                        .body(data)
                        .asString();
                success = true;
                break;
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if (!success)
            this.logFailure("POST");
        if (resp != null) {
            if (resp.getStatus() != 201) {
                this.logUnexpectedResponse("POST", resp, data);
            } else {
                this.logSuccess("POST", resp);
            }
        }
        return success;
    }

    @Override
    public boolean delete(String targetAPIEndpoint, boolean isResponseJson) {
        boolean success = false;
        if (isResponseJson) {
            success = this.deleteJsonResp(targetAPIEndpoint);
        } else {
            success = this.delete(targetAPIEndpoint);
        }
        return success;
    }

    private boolean delete(String targetAPIEndpoint) {
        boolean success = false;
        int retryCounter = 0;
        while (!success && retryCounter < maxRetries) {
            try {
                HttpResponse<String> resp = Unirest.delete(targetAPIEndpoint).asString();
                success = true;
                this.logSuccess("DELETE", resp);
                break;
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if (!success)
            this.logFailure("DELETE");
        return success;
    }

    private boolean deleteJsonResp(String targetAPIEndpoint) {
        boolean success = false;
        int retryCounter = 0;
        HttpResponse<String> resp = null;
        while (!success && retryCounter < maxRetries) {
            try {
                resp = Unirest.delete(targetAPIEndpoint).asString();
                success = true;
                break;
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if (!success)
            this.logFailure("DELETE");
        if (resp != null) {
            if (resp.getStatus() != 201) {
                this.logUnexpectedResponse("POST", resp);
            } else {
                this.logSuccess("POST", resp);
            }
        }
        return success;
    }

    @Override
    public <T> T get(String targetAPIEndpoint, Class<? extends T> type) {
        T result = null;
        boolean success = false;
        int retryCounter = 0;
        HttpResponse<T> resp = null;
        while (!success && retryCounter < maxRetries) {
            try {
                resp = Unirest.get(targetAPIEndpoint).asObject(type);
                if (resp != null) {
                    success = true;
                    result = resp.getBody();
                    logger.warning(result.toString());
                    this.logSuccess("GET", null);
                    break;
                }
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if (!success) {
            if (resp == null)
                this.logUnexpectedResponse("GET");
            this.logFailure("GET");
        }
        return result;
    }

    @Override
    public boolean query(String targetAPIEndpoint, Map<String, String> queryElements) {
        HttpRequestWithBody request = Unirest.put(targetAPIEndpoint);
        for (Map.Entry<String, String> tagEntries : queryElements.entrySet()) {
            request.queryString(tagEntries.getKey(), tagEntries.getValue());
        }

        boolean success = false;
        int retryCounter = 0;
        while (!success && retryCounter < maxRetries) {
            try {
                HttpResponse<String> resp = request.asString();
                success = true;
                this.logSuccess("QUERY", resp);
                break;
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if (!success)
            this.logFailure("QUERY");
        return success;
    }

    private void logSuccess(String requestType, HttpResponse<String> resp) {
        logger.log(Level.INFO, requestType + " request successful.");
        if (resp != null)
            this.logServerResponse(resp);
    }

    private void logServerResponse(HttpResponse<String> resp) {
        logger.log(Level.INFO, "Status: " + resp.getStatus() + ", Server response: " + resp.getBody());
    }

    private void logFailure(String requestType) {
        logger.log(Level.WARNING, requestType + " request failed after {0} retries.", maxRetries);
    }

    private void logUnexpectedResponse(String requestType, HttpResponse<String> resp) {
        logger.log(Level.WARNING, requestType + " request returned unexpected response.");
        this.logServerResponse(resp);
    }

    private void logUnexpectedResponse(String requestType) {
        logger.log(Level.WARNING, "Received unexpected response from " + requestType + " request.");
    }

    private void logUnexpectedResponse(String requestType, HttpResponse<String> resp, JSONObject data) {
        logger.log(Level.WARNING, requestType + " request returned unexpected response.");
        this.logServerResponse(resp, data);
    }

    private void logServerResponse(HttpResponse<String> resp, JSONObject data) {
        logger.log(Level.INFO, "Status: " + resp.getStatus() + ", Server response: " + resp.getBody() +
                ", Data: " + data.toString());
    }

    private void delay(long delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
        }
    }
}
