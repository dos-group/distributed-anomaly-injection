package bitflow.distributedexperiments.main.AnomalyLibrary;

import bitflow.distributedexperiments.objects.Anomaly;
import bitflow.distributedexperiments.objects.Parameters;

import java.util.*;

/**
 * Created by alex on 06.11.17.
 */
public class ClearwaterAnomalySetup implements AnomalySetUp{

    private static final Set<String> idSetCPU = new HashSet<>(Arrays.asList("stress_cpu"));
    private static final Set<String> idSetHDD = new HashSet<>(Arrays.asList("stress_hdd"));
    private static final Set<String> idSetMem = new HashSet<>(Arrays.asList("stress_mem"));
    private static final Set<String> idLeakMem = new HashSet<>(Arrays.asList("mem_leak"));
    private static final Set<String> idSetDownload = new HashSet<>(Arrays.asList("download"));
    private static final Set<String> idSetPacketLoss = new HashSet<>(Arrays.asList("packet_loss"));
    private static final Set<String> idSetPacketDuplicate = new HashSet<>(Arrays.asList("packet_duplicate"));
    private static final Set<String> idSetCPUMem = new HashSet<>(Arrays.asList("stress_cpu", "stress_mem"));
    private static final Set<String> idSetCPUMemHDD = new HashSet<>(Arrays.asList("stress_cpu", "stress_mem", "stress_hdd"));

    //####CPUSTRESS_HOST###
    private static final Map<String, Parameters> stresscpu_host_p;
    static
    {
        stresscpu_host_p = new HashMap<>();
        stresscpu_host_p.put("stress_cpu", new Parameters("--cpu=8 --cpu-load={{90 100}}"));
    }
    public static final Anomaly stresscpu_host = new Anomaly(
            idSetCPU,"stresscpu_host", stresscpu_host_p, false);

    //####CPUSTRESS_VM###
    private static final Map<String, Parameters> stresscpu_vm_p;
    static
    {
        stresscpu_vm_p = new HashMap<>();
        stresscpu_vm_p.put("stress_cpu", new Parameters("--cpu=2 --cpu-load={{90 100}}"));
    }
    public static final Anomaly stresscpu_vm = new Anomaly(
            idSetCPU,"stresscpu_vm", stresscpu_vm_p, false);

    //####HDDSTRESS###
    private static final Map<String, Parameters> hddstress_p;
    static
    {
        hddstress_p = new HashMap<>();
        hddstress_p.put("stress_hdd", new Parameters("--hdd=1"));
    }
    public static final Anomaly hddstress = new Anomaly(
            idSetHDD,"hddstress", hddstress_p, false);

    //####HDDSTRESS_HIGH###
    private static final Map<String, Parameters> hddstress_high_p;
    static
    {
        hddstress_high_p = new HashMap<>();
        hddstress_high_p.put("stress_hdd", new Parameters("--hdd=3"));
    }
    public static final Anomaly hddstress_high = new Anomaly(
            idSetHDD,"hddstress", hddstress_high_p, false);

    //####Download###
    private static final Map<String, Parameters> download_params;
    static
    {
        download_params = new HashMap<>();
        download_params.put("download", new Parameters(""));
    }
    public static final Anomaly download = new Anomaly(
            idSetDownload,"download", download_params, false);

    //####MEMSTRESS_HOST###
    private static final Map<String, Parameters> memstress_host_p;
    static
    {
        memstress_host_p = new HashMap<>();
        memstress_host_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{3000 3500}}m"));
    }
    public static final Anomaly memstress_host = new Anomaly(
            idSetMem,"memstress_host", memstress_host_p, false);

    //####MEMSTRESS_VM###
    private static final Map<String, Parameters> memstress_vm_p;
    static
    {
        memstress_vm_p = new HashMap<>();
        memstress_vm_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{500 700}}m"));
    }
    public static final Anomaly memstress_vm = new Anomaly(
            idSetMem,"memstress_vm", memstress_vm_p, false);

    //####MEMLEAK_HOST_SLOW###
    private static final Map<String, Parameters> memleak_host_slow_p;
    static
    {
        memleak_host_slow_p = new HashMap<>();
        memleak_host_slow_p.put("mem_leak", new Parameters("--sleep=1 --memory={{12 14}} --max_memory=4000"));
    }
    public static final Anomaly memleak_host_slow = new Anomaly(
            idLeakMem,"memleak_host", memleak_host_slow_p, false);

    //####MEMLEAK_HOST_FAST###
    private static final Map<String, Parameters> memleak_host_fast_p;
    static
    {
        memleak_host_fast_p = new HashMap<>();
        memleak_host_fast_p.put("mem_leak", new Parameters("--sleep=1 --memory={{30 35}} --max_memory=4000"));
    }
    public static final Anomaly memleak_host_fast = new Anomaly(
            idLeakMem,"memleak_host", memleak_host_fast_p, false);

    //####MEMLEAK_VM_SLOW###
    private static final Map<String, Parameters> memleak_vm_slow_p;
    static
    {
        memleak_vm_slow_p = new HashMap<>();
        memleak_vm_slow_p.put("mem_leak", new Parameters("--sleep=1 --memory={{3 5}} --max_memory=1000"));
    }
    public static final Anomaly memleak_vm_slow = new Anomaly(
            idLeakMem,"memleak_vm", memleak_vm_slow_p, false);

    //####MEMLEAK_VM_FAST###
    private static final Map<String, Parameters> memleak_vm_fast_p;
    static
    {
        memleak_vm_fast_p = new HashMap<>();
        memleak_vm_fast_p.put("mem_leak", new Parameters("--sleep=1 --memory={{9 11}} --max_memory=1000"));
    }
    public static final Anomaly memleak_vm_fast = new Anomaly(
            idLeakMem,"memleak_vm", memleak_vm_fast_p, false);

    //####Packet_loss_HOST###
    private static final Map<String, Parameters> packetloss_host_p;
    static
    {
        packetloss_host_p = new HashMap<>();
        packetloss_host_p.put("packet_loss", new Parameters("eno1 enp2s0 {{3 6}}"));
    }
    public static final Anomaly packetloss_host = new Anomaly(
            idSetPacketLoss,"packetloss_host", packetloss_host_p, false);

    //####Packet_loss_VM###
    private static final Map<String, Parameters> packetloss_vm_p;
    static
    {
        packetloss_vm_p = new HashMap<>();
        packetloss_vm_p.put("packet_loss", new Parameters("ens3 {{3 6}}"));
    }
    public static final Anomaly packetloss_vm = new Anomaly(
            idSetPacketLoss,"packetloss_vm", packetloss_vm_p, false);

    //####Packet_Duplicate_HOST###
    private static final Map<String, Parameters> packet_duplicate_host_p;
    static
    {
        packet_duplicate_host_p = new HashMap<>();
        packet_duplicate_host_p.put("packet_duplicate", new Parameters("eno1 enp2s0 {{3 6}}"));
    }
    public static final Anomaly packet_duplicate_host = new Anomaly(
            idSetPacketDuplicate,"packet_duplicate_host", packet_duplicate_host_p, false);

    //####Packet_Duplicate_VM###
    private static final Map<String, Parameters> packet_duplicate_vm_p;
    static
    {
        packet_duplicate_vm_p = new HashMap<>();
        packet_duplicate_vm_p.put("packet_duplicate", new Parameters("ens3 {{3 6}}"));
    }
    public static final Anomaly packet_duplicate_vm = new Anomaly(
            idSetPacketDuplicate,"packet_duplicate_vm", packet_duplicate_vm_p, false);

    //####CPU_STRESS_MEM_STRESS_HOST###
    private static final Map<String, Parameters> cpu_stress_mem_stress_host_p;
    static
    {
        cpu_stress_mem_stress_host_p = new HashMap<>();
        cpu_stress_mem_stress_host_p.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=60"));
        cpu_stress_mem_stress_host_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{2000 2500}}m"));
    }
    public static final Anomaly cpu_stress_mem_stress_host = new Anomaly(
            idSetCPUMem,"cpu_stress_mem_stress_host", cpu_stress_mem_stress_host_p, false);

    //####CPU_STRESS_MEM_STRESS_VM###
    private static final Map<String, Parameters> cpu_stress_mem_stress_vm_p;
    static
    {
        cpu_stress_mem_stress_vm_p = new HashMap<>();
        cpu_stress_mem_stress_vm_p.put("stress_cpu", new Parameters("--cpu=2 --cpu-load=60"));
        cpu_stress_mem_stress_vm_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{300 400}}m"));
    }
    public static final Anomaly cpu_stress_mem_stress_vm = new Anomaly(
            idSetCPUMem,"cpu_stress_mem_stress_vm", cpu_stress_mem_stress_vm_p, false);

    //####CPU_MEM_HDD_STRESS_HOST###
    private static final Map<String, Parameters> cpu_mem_hdd_stress_host_p;
    static
    {
        cpu_mem_hdd_stress_host_p = new HashMap<>();
        cpu_mem_hdd_stress_host_p.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=60"));
        cpu_mem_hdd_stress_host_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{2000 2500}}m"));
        cpu_mem_hdd_stress_host_p.put("stress_hdd", new Parameters("--hdd=1"));
    }
    public static final Anomaly cpu_mem_hdd_stress_host = new Anomaly(
            idSetCPUMem,"cpu_mem_hdd_stress_host", cpu_mem_hdd_stress_host_p, false);

    //####CPU_MEM_HDD_STRESS_HOST###
    private static final Map<String, Parameters> cpu_mem_hdd_stress_vm_p;
    static
    {
        cpu_mem_hdd_stress_vm_p = new HashMap<>();
        cpu_mem_hdd_stress_vm_p.put("stress_cpu", new Parameters("--cpu=2 --cpu-load=60"));
        cpu_mem_hdd_stress_vm_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{300 350}}m"));
        cpu_mem_hdd_stress_vm_p.put("stress_hdd", new Parameters("--hdd=1"));
    }
    public static final Anomaly cpu_mem_hdd_stress_vm = new Anomaly(
            idSetCPUMem,"cpu_mem_hdd_stress_vm", cpu_mem_hdd_stress_vm_p, false);


    public static final List HOST_ANOMALIES = new ArrayList() { {
        add(stresscpu_host); add(hddstress); add(memleak_host_fast);
        add(memleak_host_slow); add(memstress_host); add(hddstress_high);
        add(packetloss_host); add(download); add(packet_duplicate_host);
        add(cpu_stress_mem_stress_host); add(cpu_mem_hdd_stress_host);
    }};

    public static final List VM_ANOMALIES = new ArrayList() { {
        add(stresscpu_vm); add(memstress_vm); add(memleak_vm_slow); add(memleak_vm_fast);
        add(hddstress); add(hddstress_high); add(packetloss_vm); add(download);
        add(packet_duplicate_vm); add(cpu_stress_mem_stress_vm); add(cpu_mem_hdd_stress_vm);
    }};

    @Override
    public List<Anomaly> getAnomalies(Object key) {
        String group = (String) key;
        if (group.contains("wally")){
            return HOST_ANOMALIES;
        }else if(group.startsWith("cw-")){
            return VM_ANOMALIES;
        }else {
            return null;
        }
    }
}
