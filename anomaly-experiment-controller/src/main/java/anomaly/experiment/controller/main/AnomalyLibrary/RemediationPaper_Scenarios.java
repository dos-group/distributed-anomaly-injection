//package bitflow.distributedexperiments.main.AnomalyLibrary;
//
//import Anomaly;
//import bitflow.distributedexperiments.objects.Parameters;
//
//import java.util.*;
//
///**
// * Created by alex on 06.11.17.
// */
//public class RemediationPaper_Scenarios implements AnomalySetUp{
//
//    private static final Set<String> idSetCPU = new HashSet<>(Arrays.asList("stress_cpu"));
//    private static final Set<String> idSetMem = new HashSet<>(Arrays.asList("stress_mem"));
//    private static final Set<String> idLeakMem = new HashSet<>(Arrays.asList("mem_leak"));
//    private static final Set<String> idSetHDD = new HashSet<>(Arrays.asList("stress_hdd"));
//    private static final Set<String> idSetDownload = new HashSet<>(Arrays.asList("download"));
//    private static final Set<String> idSetPacketLoss = new HashSet<>(Arrays.asList("packet_loss"));
//    private static final Set<String> idSetBandwidth = new HashSet<>(Arrays.asList("bandwidth"));
//    private static final Set<String> idSetCPUMem = new HashSet<>(Arrays.asList("stress_cpu", "stress_mem"));
//    private static final Set<String> idSetCPUMemHDD = new HashSet<>(Arrays.asList("stress_cpu", "stress_mem", "stress_hdd"));
//
//    //####CPU_40_PHYSICAL_HOST###
//    private static final Map<String, Parameters> cpu_40_phy_params;
//    static
//    {
//        cpu_40_phy_params = new HashMap<>();
//        cpu_40_phy_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=40"));
//    }
//    private static final Anomaly cpu_40_phy = new Anomaly(
//            idSetCPU,"stress_cpu_40_phy", cpu_40_phy_params, false);
//
//    //####CPUSTRESS_40_VM###
//    private static final Map<String, Parameters> cpu_40_vm_params;
//    static
//    {
//        cpu_40_vm_params = new HashMap<>();
//        cpu_40_vm_params.put("stress_cpu", new Parameters("--cpu=2 --cpu-load=40"));
//    }
//    private static final Anomaly cpu_40_vm = new Anomaly(
//            idSetCPU,"stress_cpu_40_vm", cpu_40_vm_params, false);
//
//    //####CPU_40_PHYSICAL_HOST###
//    private static final Map<String, Parameters> cpu_95_phy_params;
//    static
//    {
//        cpu_95_phy_params = new HashMap<>();
//        cpu_95_phy_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=95"));
//    }
//    private static final Anomaly cpu_95_phy = new Anomaly(
//            idSetCPU,"stress_cpu_95_phy", cpu_95_phy_params, false);
//
//    //####CPUSTRESS_95_VM###
//    private static final Map<String, Parameters> cpu_95_vm_params;
//    static
//    {
//        cpu_95_vm_params = new HashMap<>();
//        cpu_95_vm_params.put("stress_cpu", new Parameters("--cpu=2 --cpu-load=95"));
//    }
//    private static final Anomaly cpu_95_vm = new Anomaly(
//            idSetCPU,"stress_cpu_95_vm", cpu_95_vm_params, false);
//
//    //####MEM_6.5_7GB###
//    private static final Map<String, Parameters> mem_6_5_to_7GB_params_phy;
//    static
//    {
//        mem_6_5_to_7GB_params_phy = new HashMap<>();
//        mem_6_5_to_7GB_params_phy.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{6500 7000}}m"));
//    }
//    private static final Anomaly mem_6_5_to_7GB_phy = new Anomaly(
//            idSetMem,"stress_mem_6_5_to_7GB", mem_6_5_to_7GB_params_phy, false);
//
//    //####MEMSTRESS_700MB_VM###
//    private static final Map<String, Parameters> memstress_700mb_vm_p;
//    static
//    {
//        memstress_700mb_vm_p = new HashMap<>();
//        memstress_700mb_vm_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes=700m"));
//    }
//    private static final Anomaly memstress_700mb_vm = new Anomaly(
//            idSetMem,"memstress_700mb_vm", memstress_700mb_vm_p, false);
//
//    //####MEMLEAK_HOST_SLOW###
//    private static final Map<String, Parameters> memleak_host_slow_p;
//    static
//    {
//        memleak_host_slow_p = new HashMap<>();
//        memleak_host_slow_p.put("mem_leak", new Parameters("--sleep=1 --memory=15 --max_memory=7000"));
//    }
//    private static final Anomaly memleak_host_slow = new Anomaly(
//            idLeakMem,"memleak_host_slow", memleak_host_slow_p, false);
//
//    //####MEMLEAK_HOST_FAST###
//    private static final Map<String, Parameters> memleak_host_fast_p;
//    static
//    {
//        memleak_host_fast_p = new HashMap<>();
//        memleak_host_fast_p.put("mem_leak", new Parameters("--sleep=1 --memory=30 --max_memory=7000"));
//    }
//    private static final Anomaly memleak_host_fast = new Anomaly(
//            idLeakMem,"memleak_host_fast", memleak_host_fast_p, false);
//
//    //####MEMLEAK_VM_###
//    private static final Map<String, Parameters> memleak_vm_p;
//    static
//    {
//        memleak_vm_p = new HashMap<>();
//        memleak_vm_p.put("mem_leak", new Parameters("--sleep=1 --memory=3 --max_memory=700"));
//    }
//    private static final Anomaly memleak_vm = new Anomaly(
//            idLeakMem,"memleak_vm", memleak_vm_p, false);
//
//    //####HDD_1###
//    private static final Map<String, Parameters> hdd_1_params;
//    static
//    {
//        hdd_1_params = new HashMap<>();
//        hdd_1_params.put("stress_hdd", new Parameters("--hdd=1"));
//    }
//    private static final Anomaly stress_hdd_1 = new Anomaly(
//            idSetHDD,"stress_hdd_1", hdd_1_params, false);
//
//    //####HDD_3###
//    private static final Map<String, Parameters> hdd_3_params;
//    static
//    {
//        hdd_3_params = new HashMap<>();
//        hdd_3_params.put("stress_hdd", new Parameters("--hdd=3"));
//    }
//    private static final Anomaly stress_hdd_3 = new Anomaly(
//            idSetHDD,"stress_hdd_3", hdd_3_params, false);
//
//    //####Download###
//    private static final Map<String, Parameters> download_params;
//    static
//    {
//        download_params = new HashMap<>();
//        download_params.put("download", new Parameters(""));
//    }
//    private static final Anomaly download = new Anomaly(
//            idSetDownload,"download", download_params, false);
//
//
//    //####cpu_95_mem_5GB###
//    private static final Map<String, Parameters> cpu_95_mem_5GB_params;
//    static
//    {
//        cpu_95_mem_5GB_params = new HashMap<>();
//        cpu_95_mem_5GB_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=95"));
//        cpu_95_mem_5GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes=5000m"));
//    }
//    private static final Anomaly cpu_95_mem_5GB = new Anomaly(
//            idSetCPUMem,"cpu_95_mem_5GB", cpu_95_mem_5GB_params, false);
//
//    //####cpu_95_mem_500MB###
//    private static final Map<String, Parameters> cpu_95_mem_500MB_params;
//    static
//    {
//        cpu_95_mem_500MB_params = new HashMap<>();
//        cpu_95_mem_500MB_params.put("stress_cpu", new Parameters("--cpu=2 --cpu-load=95"));
//        cpu_95_mem_500MB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes=500m"));
//    }
//    private static final Anomaly cpu_95_mem_500MB = new Anomaly(
//            idSetCPUMem,"cpu_95_mem_500MB", cpu_95_mem_500MB_params, false);
//
//    //####cpu_95_mem_5GB_hdd_2###
//    private static final Map<String, Parameters> cpu_95_mem_5GB_hdd_2_params;
//    static
//    {
//        cpu_95_mem_5GB_hdd_2_params = new HashMap<>();
//        cpu_95_mem_5GB_hdd_2_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=95"));
//        cpu_95_mem_5GB_hdd_2_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes=5000m"));
//        cpu_95_mem_5GB_hdd_2_params.put("stress_hdd", new Parameters("--hdd=2"));
//    }
//    private static final Anomaly cpu_95_mem_5GB_hdd_2 = new Anomaly(
//            idSetCPUMemHDD,"cpu_95_mem_5GB_hdd_2", cpu_95_mem_5GB_hdd_2_params,false);
//
//    //####cpu_95_mem_500MB_hdd_2###
//    private static final Map<String, Parameters> cpu_95_mem_500MB_hdd_2_params;
//    static
//    {
//        cpu_95_mem_500MB_hdd_2_params = new HashMap<>();
//        cpu_95_mem_500MB_hdd_2_params.put("stress_cpu", new Parameters("--cpu=2 --cpu-load=95"));
//        cpu_95_mem_500MB_hdd_2_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes=500m"));
//        cpu_95_mem_500MB_hdd_2_params.put("stress_hdd", new Parameters("--hdd=2"));
//    }
//    private static final Anomaly cpu_95_mem_500MB_hdd_2 = new Anomaly(
//            idSetCPUMemHDD,"cpu_95_mem_500MB_hdd_2", cpu_95_mem_500MB_hdd_2_params,false);
//
//    //####Packet_loss_HOST###
//    private static final Map<String, Parameters> packetloss_phy_p;
//    static
//    {
//        packetloss_phy_p = new HashMap<>();
//        packetloss_phy_p.put("packet_loss", new Parameters("eno1 enp2s0 15"));
//    }
//    private static final Anomaly packetloss_phy = new Anomaly(
//            idSetPacketLoss,"packetloss_phy", packetloss_phy_p, false);
//
//    //####Packet_loss_VM###
//    private static final Map<String, Parameters> packetloss_vm_p;
//    static
//    {
//        packetloss_vm_p = new HashMap<>();
//        packetloss_vm_p.put("packet_loss", new Parameters("ens3 15"));
//    }
//    private static final Anomaly packetloss_vm = new Anomaly(
//            idSetPacketLoss,"packetloss_vm", packetloss_vm_p, false);
//
//    //####Bandwidth_physical_host###
//    private static final Map<String, Parameters> bandwidth_phy_p;
//    static
//    {
//        bandwidth_phy_p = new HashMap<>();
//        bandwidth_phy_p.put("bandwidth", new Parameters("eno1 enp2s0 15Kbps"));
//    }
//    private static final Anomaly bandwidth_phy = new Anomaly(
//            idSetBandwidth,"bandwidth_phy", bandwidth_phy_p, false);
//
//    //####Bandwidth_HOST_BACKEND###
//    private static final Map<String, Parameters> bandwidth_vm_p;
//    static
//    {
//        bandwidth_vm_p = new HashMap<>();
//        bandwidth_vm_p.put("bandwidth", new Parameters("ens3 15Kbps"));
//    }
//    private static final Anomaly bandwidth_vm = new Anomaly(
//            idSetBandwidth,"bandwidth_vm", bandwidth_vm_p, false);
//
//
//
//    private static final List PHYSICAL_HOST_ANOMALIES = new ArrayList() { {
//        add(cpu_40_phy); add(cpu_95_phy); add(mem_6_5_to_7GB_phy);
//        add(memleak_host_slow); add(memleak_host_fast); add(stress_hdd_1);
//        add(stress_hdd_3); add(cpu_95_mem_5GB); add(cpu_95_mem_5GB_hdd_2);
//        add(download); add(bandwidth_phy); add(packetloss_phy);
//    }};
//
//    private static final List VM_ANOMALIES = new ArrayList() { {
//        add(cpu_40_vm); add(cpu_95_vm); add(memstress_700mb_vm); add(memleak_vm);
//        add(stress_hdd_1); add(stress_hdd_3); add(cpu_95_mem_500MB); add(cpu_95_mem_500MB_hdd_2);
//        add(download); add(bandwidth_vm); add(packetloss_vm);
//    }};
//
//
//
//    @Override
//    public List<Anomaly> getAnomalyGroups(Object key) {
//        if(key.toString().startsWith("wally")){
//            return PHYSICAL_HOST_ANOMALIES;
//        }else if(key.toString().endsWith(".ims4")){
//            return VM_ANOMALIES;
//        }else{
//            throw new IllegalArgumentException("Invalid group key " + key.toString());
//        }
//    }
//}
