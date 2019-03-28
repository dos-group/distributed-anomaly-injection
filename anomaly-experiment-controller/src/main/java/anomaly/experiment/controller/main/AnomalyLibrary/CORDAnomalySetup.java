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
//public class CORDAnomalySetup implements AnomalySetUp{
//
//    private static final Set<String> idSetCPU = new HashSet<>(Arrays.asList("stress_cpu"));
//    private static final Set<String> idSetHDD = new HashSet<>(Arrays.asList("stress_hdd"));
//    private static final Set<String> idSetMem = new HashSet<>(Arrays.asList("stress_mem"));
//    private static final Set<String> idLeakMem = new HashSet<>(Arrays.asList("mem_leak"));
//    private static final Set<String> idSetDownload = new HashSet<>(Arrays.asList("download"));
//    //private static final Set<String> idSetBandwidth = new HashSet<>(Arrays.asList("bandwidth"));
//
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
//        stresscpu_vm_p.put("stress_cpu", new Parameters("--cpu=8 --cpu-load={{90 100}}"));
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
//        memstress_host_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{1500 2500}}m"));
//    }
//    public static final Anomaly memstress_host = new Anomaly(
//            idSetMem,"memstress_host", memstress_host_p, false);
//
//    //####MEMSTRESS_VM###
//    private static final Map<String, Parameters> memstress_vm_p;
//    static
//    {
//        memstress_vm_p = new HashMap<>();
//        memstress_vm_p.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{4000 8000}}m"));
//    }
//    public static final Anomaly memstress_vm = new Anomaly(
//            idSetMem,"memstress_vm", memstress_vm_p, false);
//
//    //####MEMLEAK_HOST###
//    private static final Map<String, Parameters> memleak_host_p;
//    static
//    {
//        memleak_host_p = new HashMap<>();
//        memleak_host_p.put("mem_leak", new Parameters("--sleep={{1 20}} --memory={{20 200}} --max_memory=2000"));
//    }
//    public static final Anomaly memleak_host = new Anomaly(
//            idLeakMem,"memleak_host", memleak_host_p, false);
//
//    //####MEMLEAK_VM###
//    private static final Map<String, Parameters> memleak_vm_p;
//    static
//    {
//        memleak_vm_p = new HashMap<>();
//        memleak_vm_p.put("mem_leak", new Parameters("--sleep={{1 20}} --memory={{50 500}} --max_memory=8000"));
//    }
//    public static final Anomaly memleak_vm = new Anomaly(
//            idLeakMem,"memleak_vm", memleak_vm_p, false);
//
//
//    public static final List HOST_HEAD1 = new ArrayList() { {
//        add(stresscpu_host); add(hddstress); add(download); add(memstress_host); add(memleak_host);
//    }};
//
//    public static final List VM_COMPUTENODE = new ArrayList() { {
//    	add(stresscpu_vm); add(hddstress); add(download); add(memstress_vm); add(memleak_vm);
//    }};
//
//    @Override
//    public List<Anomaly> getAnomalyGroups(Object key) {
//        String group = (String) key;
//        if (group.endsWith("head1")){
//            return HOST_HEAD1;
//        }else if(group.endsWith("ambitious-farm.cord.lab")){
//            return VM_COMPUTENODE;
//        }else {
//            return null;
//        }
//    }
//}
