package execution_agent.http;

import bitflow4j.http.Handler;
import bitflow4j.http.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 13.09.17.
 */
public class ActionHandler extends Handler {

    private final Gson gson = new GsonBuilder().create();

    public ActionHandler() {
        super(NanoHTTPD.Method.GET, NanoHTTPD.Method.POST, NanoHTTPD.Method.DELETE);
    }

    @Override
    public NanoHTTPD.Response handle_uri(RouterNanoHTTPD.UriResource uriResource,
                                         Map<String, String> urlParams,
                                         NanoHTTPD.IHTTPSession session) throws HandlerException {
        System.out.println(session.getUri());
        return handle(urlParams, session);
    }

    @Override
    public NanoHTTPD.Response handle(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Map<String, String> map = new HashMap<>();
        map.put("Message", "This one action.");
        return Server.jsonResponse(NanoHTTPD.Response.Status.OK, this.gson.toJson(map));
    }
}
