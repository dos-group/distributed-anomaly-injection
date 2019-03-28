package anomaly.experiment.controller.load_control;

import anomaly.experiment.controller.utils.CommandExecuter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alex
 */
public class RunLoad implements RunLoadInterface{

    private static final Logger logger = Logger.getLogger(LoadChangingThread.class.getName());

    private final String startLoadScript;
    private final String stopLoadScript;

    private final int experimentDuration;

    private final int loadUpperBoundValue;
    private final int loadLowerBoundValue;

    public RunLoad(String startLoadScript, String stopLoadScript, long experimentDuration,
                   int loadLowerBoundValue, int loadUpperBoundValue) {
        this.loadUpperBoundValue = loadUpperBoundValue;
        this.loadLowerBoundValue = loadLowerBoundValue;
        this.startLoadScript = startLoadScript;
        this.stopLoadScript = stopLoadScript;
        this.experimentDuration = (int) (experimentDuration / 1000);
    }

    @Override
    public void runLoad() {
        try {
            if(CommandExecuter.executeCommand(startLoadScript, "--duration", experimentDuration+"",
                    "--min_users", loadLowerBoundValue+"", "--max_users", loadUpperBoundValue+""))
                logger.log(Level.INFO, "Started load generation");
            else
                logger.log(Level.INFO, "Load generation script returned non-zero.");
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error during load starting script execution. No load.");
        }catch (InterruptedException e){
            try {
                if(CommandExecuter.executeCommand(stopLoadScript))
                    logger.log(Level.INFO, "Terminated load generation.");
            } catch (Exception ignore) {}
        }
    }

    @Override
    public void shutdown() {
        try {
            if(CommandExecuter.executeCommand(stopLoadScript))
                logger.log(Level.INFO, "Terminated load generation.");
        } catch (Exception e) {
            logger.log(Level.INFO, "Load generation could not be stopped.");
        }
    }
}
