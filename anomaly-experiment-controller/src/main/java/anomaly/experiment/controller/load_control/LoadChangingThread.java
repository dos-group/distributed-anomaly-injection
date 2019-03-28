package anomaly.experiment.controller.load_control;

import anomaly.experiment.controller.utils.CommandExecuter;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fschmidt
 */
public class LoadChangingThread implements Runnable {

    private static final Logger logger = Logger.getLogger(LoadChangingThread.class.getName());

    private String loadGenerationsScript;
    private int upperBound;
    private int lowerBound;
    private int noLoad;


    public LoadChangingThread(String loadGenerationScript){
        this(loadGenerationScript, 100, 0, 0);
    }

    public LoadChangingThread(String loadGenerationScript, int upperBound, int lowerBound, int noLoad){
        this.loadGenerationsScript = loadGenerationScript;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.noLoad = noLoad;
    }

    @Override
    public void run() {
        try {
            int randomNum = ThreadLocalRandom.current().nextInt(lowerBound, upperBound + 1);
            if(CommandExecuter.executeCommand(loadGenerationsScript, randomNum+""))
                logger.log(Level.INFO, "Changed load to " + randomNum);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error during load changing script execution. Load unchanged.");
        }catch (InterruptedException e){
            try {
                if(CommandExecuter.executeCommand(loadGenerationsScript, noLoad+""))
                    logger.log(Level.INFO, "Changed load to " + noLoad);
            } catch (Exception ignore) {}
        }
    }

    public void shutDown(){
        try {
            if(CommandExecuter.executeCommand(loadGenerationsScript, noLoad+""))
                logger.log(Level.INFO, "Changed load to " + noLoad);
        } catch (Exception ignore) {}
    }

}
