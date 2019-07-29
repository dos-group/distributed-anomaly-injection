import os

from injector_agent import *
from anomalies.anomaly import *
from anomalies.anomaly_network import *
from anomalies.anomaly_process_python import *

def get_parametrized_anomalies():
    home = os.getcwd()
    parameterized = { \
        "noop": [ ParameterizedAnomaly(NoopAnomaly("noop"), None, "") ], \
        "mem_leak": [ \
            ParameterizedAnomaly(ProcessAnomaly("mem_leak", home + "/anomalies/binaries/mem_alloc"), \
                                 PHYSICAL, "--sleep={{20 40}} --memory={{800 1200}} --max_memory=6000"), \
            ParameterizedAnomaly(ProcessAnomaly("mem_leak", home + "/anomalies/binaries/mem_alloc"), \
                                 VIRTUAL, "--sleep={{1 2}} --memory={{20 30}} --max_memory=1200"), \
            ParameterizedAnomaly(ProcessAnomaly("mem_leak", home + "/anomalies/binaries/vnf_mem_alloc"), \
                                 SERVICE, "--sleep={{1 2}} --memory={{20 30}} --max_memory=1200"), \
            ],
        "fluct_mem": [ \
            ParameterizedAnomaly(ProcessAnomaly("fluct_mem", home + "/anomalies/binaries/mem_alloc", "-f"), \
                                 PHYSICAL, "--sleep={{1 3}} --memory={{500 1000}} --max_memory={{3500 6000}}"), \
            ParameterizedAnomaly(ProcessAnomaly("fluct_mem", home + "/anomalies/binaries/mem_alloc", "-f"), \
                                 VIRTUAL, "--sleep={{1 4}} --memory={{100 200}} --max_memory={{800 1500}}"), \
            ParameterizedAnomaly(ProcessAnomaly("fluct_mem", home + "/anomalies/binaries/vnf_mem_alloc", "-f"), \
                                 SERVICE, "--sleep={{1 4}} --memory={{100 200}} --max_memory={{800 1500}}"), \
            ],
        "stress_mem": [ \
            ParameterizedAnomaly(ProcessAnomaly("stress_mem", home + "/anomalies/binaries/stress-ng", "--vm-hang=0"), \
                                 PHYSICAL, "--vm={{1 2}} --vm-bytes={{3 5}}g"), \
            ParameterizedAnomaly(ProcessAnomaly("stress_mem", home + "/anomalies/binaries/stress-ng", "--vm-hang=0"), \
                                 VIRTUAL, "--vm=1 --vm-bytes={{800 1500}}m"), \
            ParameterizedAnomaly(ProcessAnomaly("stress_mem", home + "/anomalies/binaries/vnf_stress-ng", "--vm-hang=0"), \
                                 SERVICE, "--vm=1 --vm-bytes={{300 1000}}m"), \
            ],
        "stress_cpu": [ \
            ParameterizedAnomaly(ProcessAnomaly("stress_cpu", home + "/anomalies/binaries/stress-ng"), \
                                 PHYSICAL, "--cpu={{4 8}} --cpu-load={{80 100}}"), \
            ParameterizedAnomaly(ProcessAnomaly("stress_cpu", home + "/anomalies/binaries/stress-ng"), \
                                 VIRTUAL, "--cpu=1 --cpu-load={{80 100}}"), \
            ParameterizedAnomaly(ProcessAnomaly("stress_cpu", home + "/anomalies/binaries/vnf_stress-ng"), \
                                 SERVICE, "--cpu=1 --cpu-load={{80 100}}"), \
            ],
        "leak_cpu": [ \
            ParameterizedAnomaly(ProcessAnomaly("leak_cpu", home + "/anomalies/scripts/cpu/cpu_utilization.sh"), \
                                 PHYSICAL, "--cpu={{3 8}} --init-load={{0 30}} --load-growth={{5 10}} --sleep={{3 15}}"), \
            ParameterizedAnomaly(ProcessAnomaly("leak_cpu", home + "/anomalies/scripts/cpu/cpu_utilization.sh"), \
                                 VIRTUAL, "--cpu=1 --init-load={{0 20}} --load-growth={{5 8}} --sleep={{3 15}}"), \
            ParameterizedAnomaly(ProcessAnomaly("leak_cpu", home + "/anomalies/scripts/cpu/vnf_cpu_utilization.sh"), \
                                 SERVICE, "--cpu=1 --init-load={{0 10}} --load-growth={{5 8}} --sleep={{3 15}}"), \
            ],
        "fluct_cpu": [ \
            ParameterizedAnomaly(ProcessAnomaly("fluct_cpu", home + "/anomalies/scripts/cpu/cpu_utilization.sh", "-f"), \
                                 PHYSICAL, "--cpu={{3 8}} --init-load={{0 20}} --load-growth={{10 30}} --sleep={{3 8}}"), \
            ParameterizedAnomaly(ProcessAnomaly("fluct_cpu", home + "/anomalies/scripts/cpu/cpu_utilization.sh", "-f"), \
                                 VIRTUAL, "--cpu=1 --init-load={{0 10}} --load-growth={{10 20}} --sleep={{3 5}}"), \
            ParameterizedAnomaly(ProcessAnomaly("fluct_cpu", home + "/anomalies/scripts/cpu/vnf_cpu_utilization.sh", "-f"), \
                                 SERVICE, "--cpu=1 --init-load={{0 10}} --load-growth={{10 20}} --sleep={{3 5}}"), \
            ],
        "stress_hdd": [ \
            ParameterizedAnomaly(ProcessAnomaly("stress_hdd", home + "/anomalies/binaries/stress-ng",
                                                termination_routine=ClearTempDirectories("./tmp-stress-ng-hdd*")), \
                                 PHYSICAL, "--hdd={{2 4}}"), \
            ParameterizedAnomaly(ProcessAnomaly("stress_hdd", home + "/anomalies/binaries/stress-ng",
                                                termination_routine=ClearTempDirectories("./tmp-stress-ng-hdd*")), \
                                 VIRTUAL, "--hdd={{2 4}}"), \
            ParameterizedAnomaly(ProcessAnomaly("stress_hdd", home + "/anomalies/binaries/vnf_stress-ng",
                                                termination_routine=ClearTempDirectories("./tmp-stress-ng-hdd*")), \
                                 SERVICE, "--hdd={{2 4}}"), \
            ],
        "download": [ \
            ParameterizedAnomaly(ProcessAnomaly("download", home + "/anomalies/scripts/download/download.sh"), \
                                 SERVICE, ""), \
            ParameterizedAnomaly(ProcessAnomaly("download", home + "/anomalies/scripts/download/vnf_download.sh"), \
                                 VIRTUAL, ""), \
            ],
        "latency": [
            ParameterizedAnomaly(TrafficControlAnomaly.Latency("latency"), \
                                 SERVICE, "lo {{200 280}}"), \
            ParameterizedAnomaly(TrafficControlAnomaly.Latency("latency"), \
                                 VIRTUAL, "lo {{200 280}}")
        ], \
        "packet_loss": [
            ParameterizedAnomaly(TrafficControlAnomaly.PacketLoss("packet_loss"), \
                                 PHYSICAL, "lo {{10 25}}"), \
            ParameterizedAnomaly(TrafficControlAnomaly.PacketLoss("packet_loss"), \
                                 VIRTUAL, "lo {{10 25}}")
        ], \
        "bandwidth": [
            ParameterizedAnomaly(TrafficControlAnomaly.Bandwidth("bandwidth"), \
                                 PHYSICAL, "lo {{100 250}}Mbps"), \
            ParameterizedAnomaly(TrafficControlAnomaly.Bandwidth("bandwidth"), \
                                 VIRTUAL, "lo {{20 70}}Mbps")
        ], \
        "packet_duplicate": [
            ParameterizedAnomaly(TrafficControlAnomaly.PacketDuplicate("packet_duplicate"), \
                                 PHYSICAL, "lo {{10 25}}"), \
            ParameterizedAnomaly(TrafficControlAnomaly.PacketDuplicate("packet_duplicate"), \
                                 VIRTUAL, "lo {{10 25}}")
        ], \
        "packet_corruption": [
            ParameterizedAnomaly(TrafficControlAnomaly.PacketCorruption("packet_corruption"), \
                                 PHYSICAL, "lo {{10 25}}"), \
            ParameterizedAnomaly(TrafficControlAnomaly.PacketCorruption("packet_corruption"), \
                                 VIRTUAL, "lo {{10 25}}")
        ], \
        "packet_reordering": [
            ParameterizedAnomaly(TrafficControlAnomaly.PacketReordering("packet_reordering"), \
                                 PHYSICAL, "lo {{10 25}}"), \
            ParameterizedAnomaly(TrafficControlAnomaly.PacketReordering("packet_reordering"), \
                                 VIRTUAL, "lo {{10 25}}")
        ], \
        "root_cause": [ \
            ParameterizedAnomaly(RootCauseAnomaly("root_cause", home + "/anomalies/root_cause/root_cause.py", "python3"),
                                 PHYSICAL, '{ "targets": [] }'), \
            ParameterizedAnomaly(RootCauseAnomaly("root_cause", home + "/anomalies/root_cause/root_cause.py", "python3"),
                                 VIRTUAL, '{ "targets": [] }'), \
            ParameterizedAnomaly(RootCauseAnomaly("root_cause", home + "/anomalies/root_cause/vnf_root_cause.py", "python3"),
                                 SERVICE, '{ "targets": [] }'), \
            ],
        "leak_fork_flooding": [ \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("leak_fork_flooding", home + "/anomalies/binaries/fork_flooding", "-s"), \
                PHYSICAL, "--num_forks={{1000 2000}} --memory={{200 250}} --sleep={{2000 6000}}"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("leak_fork_flooding", home + "/anomalies/binaries/fork_flooding", "-s"), \
                VIRTUAL, "--num_forks={{500 1000}} --memory={{50 100}} --sleep={{2000 6000}}"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("leak_fork_flooding", home + "/anomalies/binaries/vnf_fork_flooding", "-s"), \
                SERVICE, "--num_forks={{500 1000}} --memory={{50 100}} --sleep={{2000 6000}}"), \
            ],
        "fluct_fork_flooding": [ \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("fluct_fork_flooding", home + "/anomalies/binaries/fork_flooding", "-f -s"), \
                PHYSICAL, "--num_forks={{1500 2000}} --memory={{200 300}} --sleep={{2000 5000}}"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("fluct_fork_flooding", home + "/anomalies/binaries/fork_flooding", "-f -s"), \
                VIRTUAL, "--num_forks={{300 800}} --memory={{50 100}} --sleep={{3000 6000}}"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("fluct_fork_flooding", home + "/anomalies/binaries/vnf_fork_flooding", "-f -s"), \
                SERVICE, "--num_forks={{300 800}} --memory={{50 100}} --sleep={{3000 6000}}"), \
            ],
        "disk_pollution": [ \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("disk_pollution", home + "/anomalies/binaries/disk_pollution", "-c -n -f"), \
                PHYSICAL, "--write-size={{300 600}} --sleep={{7 12}} --max-wsize=3000"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("disk_pollution", home + "/anomalies/binaries/disk_pollution", "-c -n -f"), \
                VIRTUAL, "--write-size={{500 800}} --sleep={{7 12}} --max-wsize=10000"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("disk_pollution", home + "/anomalies/binaries/vnf_disk_pollution", "-c -n -f"), \
                SERVICE, "--write-size={{500 800}} --sleep={{7 12}} --max-wsize=10000"), \
            ],
        "disk_pollution_tmp": [ \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("disk_pollution_tmp", home + "/anomalies/binaries/disk_pollution", "-t"), \
                PHYSICAL, "--write-size={{300 600}} --sleep={{7 12}} --max-wsize=3000"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("disk_pollution_tmp", home + "/anomalies/binaries/disk_pollution", "-t"), \
                VIRTUAL, "--write-size={{500 800}} --sleep={{7 12}} --max-wsize=10000"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("disk_pollution_tmp", home + "/anomalies/binaries/vnf_disk_pollution", "-t"), \
                SERVICE, "--write-size={{500 800}} --sleep={{7 12}} --max-wsize=10000"), \
            ],
        "file_pointer_wasting": [ \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("file_pointer_wasting", home + "/anomalies/binaries/disk_pollution", "-n -c"), \
                PHYSICAL, "--sleep={{1 4}} --max-wsize=3000"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("file_pointer_wasting", home + "/anomalies/binaries/disk_pollution", "-n -c"), \
                VIRTUAL, "--sleep={{1 4}} --max-wsize=10000"), \
            ParameterizedAnomaly(
                ProcessAnomalyTerminating("file_pointer_wasting", home + "/anomalies/binaries/vnf_disk_pollution", "-n -c"), \
                SERVICE, "--sleep={{1 4}} --max-wsize=10000"), \
            ]
    }
    return parameterized
