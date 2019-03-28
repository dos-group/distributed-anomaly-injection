package execution_agent.storage;

import execution_agent.objects.RecoveryAction;

import java.util.List;

/**
 * Created by alex on 13.09.17.
 */
public interface RecoveryActionStorageInterface {

    void addRecoveryAction(RecoveryAction action);
    RecoveryAction getRecoveryAction(Object identifier);
    List<RecoveryAction> getAllRecoveryActions();
}
