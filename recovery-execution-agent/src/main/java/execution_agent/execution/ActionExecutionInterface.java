package execution_agent.execution;

import execution_agent.objects.RecoveryAction;

/**
 * Created by alex on 13.09.17.
 */
public interface ActionExecutionInterface {

    void executeAction(RecoveryAction action);
}
