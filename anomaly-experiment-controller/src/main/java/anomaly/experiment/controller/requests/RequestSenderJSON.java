package anomaly.experiment.controller.requests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.util.logging.Level;

/**
 * Created by fschmidt and alex on 28.08.17.
 */
public class RequestSenderJSON extends RequestSender {

    public RequestSenderJSON(int maxRetries, long retryDelay) {
        super(maxRetries, retryDelay);
    }

    public boolean post(String targetAPIEndpoint, JSONObject data) {
        boolean success = false;
        int retryCounter = 0;
        HttpResponse<String> resp = null;
        while(!success && retryCounter < maxRetries){
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
        if(!success)
            this.logFailure("POST");
        if(resp != null) {
            if (resp.getStatus() != 201) {
                this.logUnexpectedResponse("POST", resp, data);
            } else {
                this.logSuccess("POST", resp);
            }
        }
        return success;
    }

    public boolean delete(String targetAPIEndpoint) {
        boolean success = false;
        int retryCounter = 0;
        HttpResponse<String> resp = null;
        while(!success && retryCounter < maxRetries){
            try {
                resp = Unirest.delete(targetAPIEndpoint).asString();
                success = true;
                break;
            } catch (UnirestException e1) {
                retryCounter++;
            }
            this.delay(this.retryDelay);
        }
        if(!success)
            this.logFailure("DELETE");
        if(resp != null) {
            if (resp.getStatus() != 201) {
                this.logUnexpectedResponse("POST", resp);
            } else {
                this.logSuccess("POST", resp);
            }
        }
        return success;
    }

    private void logUnexpectedResponse(String requestType, HttpResponse<String> resp){
        logger.log(Level.WARNING, requestType + " request returned unexpected response.");
        this.logServerResponse(resp);
    }

    private void logUnexpectedResponse(String requestType, HttpResponse<String> resp, JSONObject data){
        logger.log(Level.WARNING, requestType + " request returned unexpected response.");
        this.logServerResponse(resp, data);
    }

    private void logServerResponse(HttpResponse<String> resp, JSONObject data){
        logger.log(Level.INFO, "Status: " + resp.getStatus() + ", Server response: " + resp.getBody() +
                ", Data: " + data.toString());
    }

    protected void logJSONServerResponse(HttpResponse<JsonNode> resp){
        logger.log(Level.INFO, "Status: " + resp.getStatus() + ", Server response: " + resp.getBody().toString());
    }
}
