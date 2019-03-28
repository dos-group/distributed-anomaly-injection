package anomaly.experiment.controller.load_control;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author fschmidt
 */
public class RunLoadChanging implements RunLoadInterface{

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> loadChangerHandler;
    private final Runnable loadChanger;

    private long frequency;
    private TimeUnit timeUnit;

    public RunLoadChanging(String loadChangingScript, long frequency, TimeUnit timeUnit) {
        this.frequency = frequency;
        this.timeUnit = timeUnit;
        this.loadChanger = new LoadChangingThread(loadChangingScript);

    }

    public RunLoadChanging(String loadChangingScript, long frequency, TimeUnit timeUnit,
                           int loadUpperBoundValue, int loadLowerBoundValue, int noLoadValue) {
        this.frequency = frequency;
        this.timeUnit = timeUnit;
        this.loadChanger = new LoadChangingThread(loadChangingScript, loadUpperBoundValue,
                loadLowerBoundValue, noLoadValue);

    }

    @Override
    public void runLoad() {
        loadChangerHandler = scheduler.scheduleAtFixedRate(loadChanger, 0, frequency, timeUnit);
    }

    @Override
    public void shutdown() {
        if(loadChangerHandler != null)
            loadChangerHandler.cancel(true);
        if(scheduler != null)
            scheduler.shutdown();
        if(loadChanger != null)
            ((LoadChangingThread) loadChanger).shutDown();
    }
}
