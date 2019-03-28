package execution_agent.http;

import bitflow4j.http.Handler;
import bitflow4j.http.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 13.09.17.
 */
public class ActionsHandler extends Handler {

    private final Gson gson = new GsonBuilder().create();

    public ActionsHandler() {
        super(NanoHTTPD.Method.GET);
    }

    @Override
    public NanoHTTPD.Response handle(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Map<String, String> map = new HashMap<>();
        map.put("Message", "All the beautiful actions.");
        return Server.jsonResponse(NanoHTTPD.Response.Status.OK, this.gson.toJson(map));
    }
}
