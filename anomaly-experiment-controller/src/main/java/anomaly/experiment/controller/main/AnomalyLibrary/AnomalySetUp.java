package anomaly.experiment.controller.main.AnomalyLibrary;

import anomaly.experiment.controller.objects.Anomaly;

import java.util.List;

/**
 * Created by alex on 15.02.18.
 */
public interface AnomalySetUp {
    List<Anomaly> getAnomalies(Object key);
}
