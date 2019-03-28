package execution_agent.objects;

/**
 * Created by alex on 13.09.17.
 */
public class ExecutionStep {

    private String name;
    private String status;

    public ExecutionStep(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getStatus() {
        return status;
    }

    void execute() throws ExecutionStepException{
    }

    public class ExecutionStepException extends Exception{
    }
}
