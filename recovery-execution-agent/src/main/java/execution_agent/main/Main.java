package execution_agent.main;

import bitflow4j.http.Server;
import execution_agent.http.ActionHandler;
import execution_agent.http.ActionsHandler;
import execution_agent.utils.Config;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by alex on 13.09.17.
 */
public class Main {

    static { Config.initializeLogger(); }
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final String ROUTE_API = "/api";
    private static final String ROUTE_ACTION = "/action";
    private static final String ROUTE_ACTIONS = "/actions";

    public static void main(String args[]) throws IOException, ParseException {
        CommandLineParser parser = new DefaultParser();
        Options o = new Options();
        o.addOption(Option.builder("p").longOpt("port").hasArg().type(Number.class).required().desc("Port number for web server").build());
        CommandLine flags = parser.parse(o, args);

        int port = ((Number) flags.getParsedOptionValue("p")).intValue();
        Server server = new Server(port);

        server.addRoute(ROUTE_API + ROUTE_ACTIONS, ActionsHandler.class);
        server.addRoute(ROUTE_API + ROUTE_ACTION + "/[^/]+$", ActionHandler.class);

        server.start();

        while(true)
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {}
    }
}
