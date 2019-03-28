package execution_agent.objects;

import java.util.*;

/**
 * Created by alex on 13.09.17.
 */
public class RecoveryAction implements Action {

    public enum Status{
        ONGOING, SUCCESS, FAILURE
    }

    public interface ProgressListener{
        void progressNotification(Status status, Object src);
    }

    private String name;
    private List<ExecutionStep> steps = new ArrayList<>();
    private Set<ProgressListener> progressListener = new HashSet<>();

    public RecoveryAction(String name, ExecutionStep... steps){
        this.name = name;
        this.steps.addAll(Arrays.asList(steps));
    }

    @Override
    public void execute() {
        for(ExecutionStep step : steps){
            try {
                step.execute();
                notifyProgressListener(Status.ONGOING, step);
            } catch (ExecutionStep.ExecutionStepException e) {
                notifyProgressListener(Status.FAILURE, step);
                return;
            }
        }
        notifyProgressListener(Status.SUCCESS, this);
    }

    private void notifyProgressListener(Status status, Object src) {
        for(ProgressListener listener : progressListener)
            listener.progressNotification(status, src);
    }
}
