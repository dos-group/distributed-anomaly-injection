package bitflow.distributedexperiments.main.AnomalyLibrary;

import bitflow.distributedexperiments.objects.Anomaly;

import java.util.List;

/**
 * Created by alex on 15.02.18.
 */
public interface AnomalySetUp {
    List<Anomaly> getAnomalies(Object key);
}
