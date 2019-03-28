package anomaly.experiment.controller.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *
  "known anomalies": [
    {
      "name": "download",
      "currently_running": false,
      "default_param": ""
    },
    {
      "name": "latency",
      "currently_running": false,
      "default_param": "em1 p4p1 eth0 lo {{200 280}}"
    },
    {
      "name": "packet_loss",
      "currently_running": false,
      "default_param": "em1 p4p1 eth0 lo {{10 25}}"
    },
    {
      "name": "bandwidth",
      "currently_running": false,
      "default_param": "em1 p4p1 eth0 lo {{200 1000}}"
    },
    {
      "name": "noop",
      "currently_running": false,
      "default_param": ""
    },
    {
      "name": "stress_hdd",
      "currently_running": false,
      "default_param": "--hdd {{2 4}}"
    },
    {
      "name": "stress_cpu",
      "currently_running": false,
      "default_param": "-c {{7 8}}"
    },
    {
      "name": "mem_leak",
      "currently_running": false,
      "default_param": "{{20 40}} {{800 1200}}"
    }
  ],
  "Hostnames": [
    "wally136"
  ],
  "Injection Mode": "manual"
 * @author fschmidt
 */
public class InjectorStatus {
    @JsonProperty("Injection Mode")
    private String injectionMode;
    @JsonProperty("Hostnames")
    private List<String> hostnames;
    @JsonProperty("known anomalies")
    private List<Anomaly> knownAnomalies;

    public String getInjectionMode() {
        return injectionMode;
    }

    public void setInjectionMode(String injectionMode) {
        this.injectionMode = injectionMode;
    }

    public List<String> getHostnames() {
        return hostnames;
    }

    public void setHostnames(List<String> hostnames) {
        this.hostnames = hostnames;
    }

    public List<Anomaly> getKnownAnomalies() {
        return knownAnomalies;
    }

    public void setKnownAnomalies(List<Anomaly> knownAnomalies) {
        this.knownAnomalies = knownAnomalies;
    }    
    
}
