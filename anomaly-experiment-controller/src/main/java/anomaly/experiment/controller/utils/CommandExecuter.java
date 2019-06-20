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
        boolean success;
        if(checkFileExists(path)) {
            logger.log(Level.INFO, "Executing command script " + path + ".");
            String[] cmdArgs = new String[arguments.length + 1];
            cmdArgs[0] = path;
            System.arraycopy(arguments, 0, cmdArgs, 1, arguments.length);
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
            success = process.exitValue() == 0;
            if (success)
                logger.log(Level.INFO, "Execution completed successfully.");
            else
                logger.log(Level.WARNING, String.format("Execution was not successful. Return value: %d",
                        process.exitValue()));
        } else {
            logger.log(Level.WARNING, "Script file " + path + " does not exist. Command cannot be executed.");
            success = false;
        }
        return success;
    }

    public static boolean checkFileExists(String path){
        return new File(path).exists();
    }
}
