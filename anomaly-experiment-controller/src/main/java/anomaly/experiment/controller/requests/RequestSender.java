package anomaly.experiment.controller.requests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fschmidt and alex on 27.08.17.
 */
public class RequestSender {

    protected static final Logger logger = Logger.getLogger(RequestSender.class.getName());

    protected int maxRetries;
    protected long retryDelay;

    public RequestSender(int maxRetries, long retryDelay){
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    public boolean post(String targetAPIEndpoint){
        boolean success = false;
        int retryCounter = 0;
        while(!success && retryCounter < maxRetries){
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
        if(!success)
            this.logFailure("POST");
        return success;
    }

    public boolean delete(String targetAPIEndpoint) {
        boolean success = false;
        int retryCounter = 0;
        while(!success && retryCounter < maxRetries){
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
        if(!success)
            this.logFailure("DELETE");
        return success;
    }

    public <T> T get(String targetAPIEndpoint, Class<? extends T> type){
        T result = null;
        boolean success = false;
        int retryCounter = 0;
        HttpResponse<T> resp = null;
        while(!success && retryCounter < maxRetries){
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
        if(!success){
            if (resp == null)
                this.logUnexpectedResponse("GET");
            this.logFailure("GET");
        }
        return result;
    }

    public boolean query(String targetAPIEndpoint, Map<String, String> queryElements) {
        HttpRequestWithBody request = Unirest.put(targetAPIEndpoint);
        for (Map.Entry<String, String> tagEntries : queryElements.entrySet()) {
            request.queryString(tagEntries.getKey(), tagEntries.getValue());
        }

        boolean success = false;
        int retryCounter = 0;
        while(!success && retryCounter < maxRetries){
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
        if(!success)
            this.logFailure("QUERY");
        return success;
    }

    protected void logSuccess(String requestType, HttpResponse<String> resp){
        logger.log(Level.INFO, requestType + " request successful.");
        if(resp != null)
            this.logServerResponse(resp);
    }

    protected void logServerResponse(HttpResponse<String> resp){
        logger.log(Level.INFO, "Status: " + resp.getStatus() + ", Server response: " + resp.getBody());
    }

    protected void logFailure(String requestType){
        logger.log(Level.WARNING, requestType + " request failed after {0} retries.", maxRetries);
    }

    protected void logUnexpectedResponse(String requestType){
        logger.log(Level.WARNING, "Received unexpected response from " + requestType + " request.");
    }

    protected void delay(long delayTime){
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {}
    }
}
