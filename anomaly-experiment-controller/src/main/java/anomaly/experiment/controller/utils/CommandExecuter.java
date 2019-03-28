package anomaly.experiment.controller.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 08.09.17.
 */
public class CommandExecuter {

    private static final Logger logger = Logger.getLogger(CommandExecuter.class.getName());

    public static boolean executeCommand(String path, String... arguments) throws IOException, InterruptedException {
        boolean success = false;
        if(checkFileExists(path)) {
            logger.log(Level.INFO, "Executing command script " + path + ".");
            String[] cmdArgs = new String[arguments.length + 1];
            cmdArgs[0] = path;
            for(int i = 0; i < arguments.length; i++)
                cmdArgs[i + 1] = arguments[i];
            Process process = Runtime.getRuntime().exec(cmdArgs);
            try(
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))
            ) {
                String line;
                while ((line = in.readLine()) != null)
                    logger.log(Level.INFO, line);
            }
            process.waitFor();
            success = true;
            logger.log(Level.INFO, "Execution complete.");
        }
        return success;
    }

    private static boolean checkFileExists(String path){
        if(!new File(path).exists()){
            logger.log(Level.WARNING, "Script file " + path + " does not exist. Command cannot be executed.");
            return false;
        }
        return true;
    }
}
