package bitflow.distributedexperiments.objects.targets;

import bitflow.distributedexperiments.requests.RequestSender;
import bitflow.distributedexperiments.utils.CommandExecuter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 27.08.17.
 */
public abstract class Target {

    public final static String GROUP_DEFAULT = "nogroup";
    public static final String SERVICE_UNKNOWN = "unknown";

    protected static final Logger logger = Logger.getLogger(InjectorTarget.class.getName());

    protected static final String HTTP_PREFIX = "http://";

    protected RequestSender requestController;

    protected String name;
    protected String group;
    protected String address;
    protected String port;
    protected String service_name;

    protected String targetRecoveryScript = null;

    protected abstract String[] getRecoveryCmdArgs();

    public Target(String name, String address, String port, String service_name) {
        this.name = name;
        this.address = address;
        this.port = port;
        if(service_name == null || service_name.isEmpty())
            this.service_name = SERVICE_UNKNOWN;
        else
            this.service_name = service_name;
    }

    public Target(String address, String port) {
        this("", address, port, GROUP_DEFAULT);
    }

    public String getAddress() {
        return (address.startsWith(HTTP_PREFIX) ? address : HTTP_PREFIX + address);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestSender getRequestController() {
        return requestController;
    }

    public void setRequestController(RequestSender requestController) {
        this.requestController = requestController;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public void setTargetRecoveryScript(String targetRecoveryScript) {
        this.targetRecoveryScript = targetRecoveryScript;
    }

    protected void checkRequestController() throws InvalidParameterException{
        if(requestController == null)
            throw new InvalidParameterException("Request controller is undefined.");
    }

    protected void recover(){
        if(this.targetRecoveryScript != null && !this.targetRecoveryScript.isEmpty()){
            String[] recoveryCmdArgs = this.getRecoveryCmdArgs();
            try {
                if(CommandExecuter.executeCommand(this.targetRecoveryScript, recoveryCmdArgs))
                    logger.log(Level.INFO, "Recovering with cmd " + this.targetRecoveryScript +
                            " " + this.argsToStr(recoveryCmdArgs));
                else
                    logger.log(Level.INFO, "Recovering with cmd " + this.targetRecoveryScript + " " +
                            this.argsToStr(recoveryCmdArgs) + " returned non-zero.");
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error during recovery cmd execution. " +
                        this.targetRecoveryScript + " " + this.argsToStr(recoveryCmdArgs) + " returned non-zero.");
            }catch (InterruptedException ignore){
            }
        }
    }

    @Override
    public String toString(){
        return name + " of service group \"" + group +
                "\": " + this.getAddress() + ":" + port;
    }

    private String argsToStr(String[] args){
        String ret = "";
        for(String s : args){
            ret += s + " ";
        }
        return ret;
    }
}
