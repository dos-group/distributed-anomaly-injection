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
//public class VideoStreamAnomalySetup implements AnomalySetUp{
//
//    private static final Set<String> idSetCPU = new HashSet<>(Arrays.asList("stress_cpu"));
//    private static final Set<String> idSetHDD = new HashSet<>(Arrays.asList("stress_hdd"));
//    private static final Set<String> idSetMem = new HashSet<>(Arrays.asList("stress_mem"));
//    private static final Set<String> idLeakMem = new HashSet<>(Arrays.asList("mem_leak"));
//    private static final Set<String> idSetDownload = new HashSet<>(Arrays.asList("download"));
//    private static final Set<String> idSetBandwidth = new HashSet<>(Arrays.asList("bandwidth"));
//    private static final Set<String> idSetLatency = new HashSet<>(Arrays.asList("latency"));
//    private static final Set<String> idSetPacketLoss = new HashSet<>(Arrays.asList("packet_loss"));
//    private static final Set<String> idSetPacketDuplicate = new HashSet<>(Arrays.asList("packet_duplicate"));
//    private static final Set<String> idSetPacketCorruption = new HashSet<>(Arrays.asList("packet_corruption"));
//
//    //####CPUSTRESS_HOST###
//    private static final Map<String, Parameters> stresscpu_host_p;
//    static
//    {
//        stresscpu_host_p = new HashMap<>();
//        stresscpu_host_p.put("stress_cpu", new Parameters("--cpu=8 --cpu-load={{90 100}}"));
//    }
//    public static final Anomaly stresscpu_host = new Anomaly(
//            idSetCPU,"stresscpu_host", stresscpu_host_p, false);
//
//    //####CPUSTRESS_VM###
//    private static final Map<String, Parameters> stresscpu_vm_p;
//    static
//    {
//        stresscpu_vm_p = new HashMap<>();
//        stresscpu_vm_p.put("stress_cpu", new Parameters("--cpu=2 --cpu-load={{90 100}}"));
//    }
//    public static final Anomaly stresscpu_vm = new Anomaly(
//            idSetCPU,"stresscpu_vm", stresscpu_vm_p, false);
//
//    //####HDDSTRESS###
//    private static final Map<String, Parameters> hddstress_p;
//    static
//    {
//        hddstress_p = new HashMap<>();
//        hddstress_p.put("stress_hdd", new Parameters("--hdd={{1 3}}"));
//    }
//    public static final Anomaly hddstress = new Anomaly(
//            idSetHDD,"hddstress", hddstress_p, false);
//
//    //####Download###
//    private static final Map<String, Parameters> download_params;
//    static
//    {
//        download_params = new HashMap<>();
//        download_params.put("download", new Parameters(""));
//    }
//    public static final Anomaly download = new Anomaly(
//            idSetDownload,"download", download_params, false);
//
//    //####MEMSTRESS_HOST###
//    private static final Map<String, Parameters> memstress_host_p;
//    static
//    {
//        memstress_host_p = new HashMap<>();
//        memstress_host_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{5000 6000}}m"));
//    }
//    public static final Anomaly memstress_host = new Anomaly(
//            idSetMem,"memstress_host", memstress_host_p, false);
//
//    //####MEMSTRESS_VM###
//    private static final Map<String, Parameters> memstress_vm_p;
//    static
//    {
//        memstress_vm_p = new HashMap<>();
//        memstress_vm_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{500 1000}}m"));
//    }
//    public static final Anomaly memstress_vm = new Anomaly(
//            idSetMem,"memstress_vm", memstress_vm_p, false);
//
//    //####MEMLEAK_HOST###
//    private static final Map<String, Parameters> memleak_host_p;
//    static
//    {
//        memleak_host_p = new HashMap<>();
//        memleak_host_p.put("mem_leak", new Parameters("--sleep={{1 20}} --memory={{50 500}} --max_memory=6000"));
//    }
//    public static final Anomaly memleak_host = new Anomaly(
//            idLeakMem,"memleak_host", memleak_host_p, false);
//
//    //####MEMLEAK_VM###
//    private static final Map<String, Parameters> memleak_vm_p;
//    static
//    {
//        memleak_vm_p = new HashMap<>();
//        memleak_vm_p.put("mem_leak", new Parameters("--sleep={{1 20}} --memory={{20 100}} --max_memory=1300"));
//    }
//    public static final Anomaly memleak_vm = new Anomaly(
//            idLeakMem,"memleak_vm", memleak_vm_p, false);
//
//    //####Latency_HOST###
//    private static final Map<String, Parameters> latency_host_p;
//    static
//    {
//        latency_host_p = new HashMap<>();
//        latency_host_p.put("latency", new Parameters("eno1 enp2s0 lo {{50 200}}"));
//    }
//    public static final Anomaly latency_host = new Anomaly(
//            idSetLatency,"latency_host", latency_host_p, false);
//
//    //####Latency_VM###
//    private static final Map<String, Parameters> latency_vm_p;
//    static
//    {
//        latency_vm_p = new HashMap<>();
//        latency_vm_p.put("latency", new Parameters("ens3 lo {{50 200}}"));
//    }
//    public static final Anomaly latency_vm = new Anomaly(
//            idSetLatency,"latency_vm", latency_vm_p, false);
//
//    //####Packet_loss_HOST###
//    private static final Map<String, Parameters> packetloss_host_p;
//    static
//    {
//        packetloss_host_p = new HashMap<>();
//        packetloss_host_p.put("packet_loss", new Parameters("eno1 enp2s0 lo {{5 15}}"));
//    }
//    public static final Anomaly packetloss_host = new Anomaly(
//            idSetPacketLoss,"packetloss_host", packetloss_host_p, false);
//
//    //####Packet_loss_VM###
//    private static final Map<String, Parameters> packetloss_vm_p;
//    static
//    {
//        packetloss_vm_p = new HashMap<>();
//        packetloss_vm_p.put("packet_loss", new Parameters("ens3 lo {{5 15}}"));
//    }
//    public static final Anomaly packetloss_vm = new Anomaly(
//            idSetPacketLoss,"packetloss_vm", packetloss_vm_p, false);
//
//    //####Bandwidth_HOST_BACKEND###
//    private static final Map<String, Parameters> bandwidth_host_be_p;
//    static
//    {
//        bandwidth_host_be_p = new HashMap<>();
//        bandwidth_host_be_p.put("bandwidth", new Parameters("eno1 enp2s0 lo {{15 30}}Mbps"));
//    }
//    public static final Anomaly bandwidth_host_be = new Anomaly(
//            idSetBandwidth,"bandwidth_host_be", bandwidth_host_be_p, false);
//
//    //####Bandwidth_HOST_LOAD_BALANCER###
//    private static final Map<String, Parameters> bandwidth_host_lb_p;
//    static
//    {
//        bandwidth_host_lb_p = new HashMap<>();
//        bandwidth_host_lb_p.put("bandwidth", new Parameters("eno1 enp2s0 lo {{120 190}}Mbps"));
//    }
//    public static final Anomaly bandwidth_host_lb = new Anomaly(
//            idSetBandwidth,"bandwidth_host_lb", bandwidth_host_lb_p, false);
//
//    //####Bandwidth_VM_BACKEND###
//    private static final Map<String, Parameters> bandwidth_vm_be_p;
//    static
//    {
//        bandwidth_vm_be_p = new HashMap<>();
//        bandwidth_vm_be_p.put("bandwidth", new Parameters("ens3 lo {{12 20}}Mbps"));
//    }
//    public static final Anomaly bandwidth_vm_be = new Anomaly(
//            idSetBandwidth,"bandwidth_vm_be", bandwidth_vm_be_p, false);
//
//    //####Bandwidth_VM_LOAD_BALANCER###
//    private static final Map<String, Parameters> bandwidth_vm_lb_p;
//    static
//    {
//        bandwidth_vm_lb_p = new HashMap<>();
//        bandwidth_vm_lb_p.put("bandwidth", new Parameters("ens3 lo {{30 45}}Mbps"));
//    }
//    public static final Anomaly bandwidth_vm_lb = new Anomaly(
//            idSetBandwidth,"bandwidth_vm_lb", bandwidth_vm_lb_p, false);
//
//    //####Packet_Duplicate_HOST###
//    private static final Map<String, Parameters> packet_duplicate_host_p;
//    static
//    {
//        packet_duplicate_host_p = new HashMap<>();
//        packet_duplicate_host_p.put("packet_duplicate", new Parameters("eno1 enp2s0 lo {{5 15}}"));
//    }
//    public static final Anomaly packet_duplicate_host = new Anomaly(
//            idSetPacketDuplicate,"packet_duplicate_host", packet_duplicate_host_p, false);
//
//    //####Packet_Duplicate_VM###
//    private static final Map<String, Parameters> packet_duplicate_vm_p;
//    static
//    {
//        packet_duplicate_vm_p = new HashMap<>();
//        packet_duplicate_vm_p.put("packet_duplicate", new Parameters("ens3 lo {{5 15}}"));
//    }
//    public static final Anomaly packet_duplicate_vm = new Anomaly(
//            idSetPacketDuplicate,"packet_duplicate_vm", packet_duplicate_vm_p, false);
//
//    //####Packet_corruption_HOST###
//    private static final Map<String, Parameters> packet_corruption_host_p;
//    static
//    {
//        packet_corruption_host_p = new HashMap<>();
//        packet_corruption_host_p.put("packet_corruption", new Parameters("eno1 enp2s0 lo {{10 25}}"));
//    }
//    public static final Anomaly packet_corruption_host = new Anomaly(
//            idSetPacketCorruption,"packet_corruption_host", packet_corruption_host_p, false);
//
//    //####Packet_corruption_vm###
//    private static final Map<String, Parameters> packet_corruption_vm_p;
//    static
//    {
//        packet_corruption_vm_p = new HashMap<>();
//        packet_corruption_vm_p.put("packet_corruption", new Parameters("ens3 lo {{10 25}}"));
//    }
//    public static final Anomaly packet_corruption_vm = new Anomaly(
//            idSetPacketCorruption,"packet_corruption_vm", packet_corruption_vm_p, false);
//
//
//    public static final List HOST_ANOMALIES_BACKEND = new ArrayList() { {
//        add(stresscpu_host); add(memleak_host);
//        add(memstress_host); add(latency_host); add(packetloss_host);
//        add(bandwidth_host_be); add(packet_duplicate_host);
//    }};
//
//    public static final List HOST_ANOMALIES_BALANCER = new ArrayList() { {
//        add(stresscpu_host); add(memleak_host);
//        add(memstress_host); add(latency_host); add(packetloss_host);
//        add(bandwidth_host_lb); add(packet_duplicate_host);
//    }};
//
//    public static final List VM_ANOMALIES_BALANCER = new ArrayList() { {
//        add(stresscpu_vm); add(memstress_vm); add(memleak_vm);
//        add(latency_vm); add(packetloss_vm); add(bandwidth_vm_lb);
//        add(packet_duplicate_vm);
//    }};
//
//    public static final List VM_ANOMALIES_BACKEND = new ArrayList() { {
//        add(stresscpu_vm); add(memstress_vm); add(memleak_vm); add(hddstress);
//        add(latency_vm); add(packetloss_vm); add(bandwidth_vm_be); add(download);
//        add(packet_duplicate_vm);
//    }};
//
//    @Override
//    public List<Anomaly> getAnomalyGroups(Object key) {
//        String group = (String) key;
//        if (group.endsWith("vod-host-balancer")){
//            return HOST_ANOMALIES_BALANCER;
//        }else if(group.endsWith("vod-host-backend")){
//            return HOST_ANOMALIES_BACKEND;
//        }else if(group.endsWith("vod-balancer")){
//            return VM_ANOMALIES_BALANCER;
//        }else if(group.endsWith("vod-video")) {
//            return VM_ANOMALIES_BACKEND;
//        }else {
//            return null;
//        }
//    }
//}
