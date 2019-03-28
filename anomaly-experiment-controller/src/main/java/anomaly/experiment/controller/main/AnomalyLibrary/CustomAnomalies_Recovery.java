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
//public class CustomAnomalies_Recovery {
//
//    private static final Set<String> idSetCPU = new HashSet<>(Arrays.asList("stress_cpu"));
//    private static final Set<String> idSetMem = new HashSet<>(Arrays.asList("stress_mem"));
//    private static final Set<String> idSetHDD = new HashSet<>(Arrays.asList("stress_hdd"));
//    private static final Set<String> idSetDownload = new HashSet<>(Arrays.asList("download"));
//    private static final Set<String> idSetCPUMem = new HashSet<>(Arrays.asList("stress_cpu", "stress_mem"));
//    private static final Set<String> idSetMemHDD = new HashSet<>(Arrays.asList("stress_mem", "stress_hdd"));
//    private static final Set<String> idSetCPUMemHDD = new HashSet<>(Arrays.asList("stress_cpu", "stress_mem", "stress_hdd"));
//
//    //####CPU_40###
//    private static final Map<String, Parameters> cpu_40_params;
//    static
//    {
//        cpu_40_params = new HashMap<>();
//        cpu_40_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=40"));
//    }
//    private static final Map<String, String> cpu_40_recs;
//    static
//    {
//        cpu_40_recs = new HashMap<>();
//        cpu_40_recs.put("Rec", "3|4|53");
//    }
//    public static final Anomaly cpu_40 = new Anomaly(
//            idSetCPU,"stress_cpu_40", cpu_40_params, false,
//            cpu_40_recs);
//
//    //####CPU_70###
//    private static final Map<String, Parameters> cpu_70_params;
//    static
//    {
//        cpu_70_params = new HashMap<>();
//        cpu_70_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=70"));
//    }
//    private static final Map<String, String> cpu_70_recs;
//    static
//    {
//        cpu_70_recs = new HashMap<>();
//        cpu_70_recs.put("Rec", "7|41|55");
//    }
//    public static final Anomaly cpu_70 = new Anomaly(
//            idSetCPU,"stress_cpu_70", cpu_70_params, false,
//            cpu_70_recs);
//
//    //####CPU_95###
//    private static final Map<String, Parameters> cpu_95_params;
//    static
//    {
//        cpu_95_params = new HashMap<>();
//        cpu_95_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=95"));
//    }
//    private static final Map<String, String> cpu_95_recs;
//    static
//    {
//        cpu_95_recs = new HashMap<>();
//        cpu_95_recs.put("Rec", "99");
//    }
//    public static final Anomaly cpu_95 = new Anomaly(
//            idSetCPU,"stress_cpu_95", cpu_95_params, false,
//            cpu_95_recs);
//
//    //####MEM_2_to_4GB###
//    private static final Map<String, Parameters> mem_2_to_4GB_params;
//    static
//    {
//        mem_2_to_4GB_params = new HashMap<>();
//        mem_2_to_4GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{2000 4000}}m"));
//    }
//    private static final Map<String, String> mem_2_to_4GB_recs;
//    static
//    {
//        mem_2_to_4GB_recs = new HashMap<>();
//        mem_2_to_4GB_recs.put("Rec", "10|87");
//    }
//    public static final Anomaly mem_2_to_4GB = new Anomaly(
//            idSetMem,"stress_mem_2_to_4GB", mem_2_to_4GB_params, false,
//            mem_2_to_4GB_recs);
//
//    //####MEM_5_to_7GB###
//    private static final Map<String, Parameters> mem_5_to_7GB_params;
//    static
//    {
//        mem_5_to_7GB_params = new HashMap<>();
//        mem_5_to_7GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{5000 7000}}m"));
//    }
//    private static final Map<String, String> mem_5_to_7GB_recs;
//    static
//    {
//        mem_5_to_7GB_recs = new HashMap<>();
//        mem_5_to_7GB_recs.put("Rec", "36|63");
//    }
//    public static final Anomaly mem_5_to_7GB = new Anomaly(
//            idSetMem,"stress_mem_5_to_7GB", mem_5_to_7GB_params, false,
//            mem_5_to_7GB_recs);
//
//    //####MEM_7_5_to_8_5GB###
//    private static final Map<String, Parameters> mem_7_5_to_8_5GB_params;
//    static
//    {
//        mem_7_5_to_8_5GB_params = new HashMap<>();
//        mem_7_5_to_8_5GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{7500 8500}}m"));
//    }
//    private static final Map<String, String> mem_7_5_to_8_5GB_recs;
//    static
//    {
//        mem_7_5_to_8_5GB_recs = new HashMap<>();
//        mem_7_5_to_8_5GB_recs.put("Rec", "76|93");
//    }
//    public static final Anomaly mem_7_5_to_8_5GB = new Anomaly(
//            idSetMem,"stress_mem_7_5_to_8_5GB", mem_7_5_to_8_5GB_params, false,
//            mem_7_5_to_8_5GB_recs);
//
//    //####HDD_1###
//    private static final Map<String, Parameters> hdd_1_params;
//    static
//    {
//        hdd_1_params = new HashMap<>();
//        hdd_1_params.put("stress_hdd", new Parameters("--hdd=1"));
//    }
//    private static final Map<String, String> hdd_1_recs;
//    static
//    {
//        hdd_1_recs = new HashMap<>();
//        hdd_1_recs.put("Rec", "19|46");
//    }
//    public static final Anomaly stress_hdd_1 = new Anomaly(
//            idSetHDD,"stress_hdd_1", hdd_1_params, false,
//            hdd_1_recs);
//
//    //####HDD_2###
//    private static final Map<String, Parameters> hdd_2_params;
//    static
//    {
//        hdd_2_params = new HashMap<>();
//        hdd_2_params.put("stress_hdd", new Parameters("--hdd=2"));
//    }
//    private static final Map<String, String> hdd_2_recs;
//    static
//    {
//        hdd_2_recs = new HashMap<>();
//        hdd_2_recs.put("Rec", "29|66|70|90");
//    }
//    public static final Anomaly stress_hdd_2 = new Anomaly(
//            idSetHDD,"stress_hdd_2", hdd_2_params, false,
//            hdd_2_recs);
//
//    //####HDD_3###
//    private static final Map<String, Parameters> hdd_3_params;
//    static
//    {
//        hdd_3_params = new HashMap<>();
//        hdd_3_params.put("stress_hdd", new Parameters("--hdd=3"));
//    }
//    private static final Map<String, String> hdd_3_recs;
//    static
//    {
//        hdd_3_recs = new HashMap<>();
//        hdd_3_recs.put("Rec", "22|58|81");
//    }
//    public static final Anomaly stress_hdd_3 = new Anomaly(
//            idSetHDD,"stress_hdd_3", hdd_3_params, false,
//            hdd_3_recs);
//
//    //####Download###
//    private static final Map<String, Parameters> download_params;
//    static
//    {
//        download_params = new HashMap<>();
//        download_params.put("download", new Parameters(""));
//    }
//    private static final Map<String, String> download_recs;
//    static
//    {
//        download_recs = new HashMap<>();
//        download_recs.put("Rec", "12|17|34|84");
//    }
//    public static final Anomaly download = new Anomaly(
//            idSetDownload,"download", download_params, false,
//            download_recs);
//
//    //####cpu_70_mem_5_to_7GB###
//    private static final Map<String, Parameters> cpu_70_mem_5_to_7GB_params;
//    static
//    {
//        cpu_70_mem_5_to_7GB_params = new HashMap<>();
//        cpu_70_mem_5_to_7GB_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=70"));
//        cpu_70_mem_5_to_7GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{5000 7000}}m"));
//    }
//    private static final Map<String, String> cpu_70_mem_5_to_7GB_recs;
//    static
//    {
//        cpu_70_mem_5_to_7GB_recs = new HashMap<>();
//        cpu_70_mem_5_to_7GB_recs.put("Rec", "15|50");
//    }
//    public static final Anomaly cpu_70_mem_5_to_7GB = new Anomaly(
//            idSetCPUMem,"cpu_70_mem_5_to_7GB", cpu_70_mem_5_to_7GB_params, false,
//            cpu_70_mem_5_to_7GB_recs);
//
//    //####cpu_95_mem_2_to_4GB###
//    private static final Map<String, Parameters> cpu_95_mem_2_to_4GB_params;
//    static
//    {
//        cpu_95_mem_2_to_4GB_params = new HashMap<>();
//        cpu_95_mem_2_to_4GB_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=95"));
//        cpu_95_mem_2_to_4GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{2000 4000}}m"));
//    }
//    private static final Map<String, String> cpu_95_mem_2_to_4GB_recs;
//    static
//    {
//        cpu_95_mem_2_to_4GB_recs = new HashMap<>();
//        cpu_95_mem_2_to_4GB_recs.put("Rec", "60");
//    }
//    public static final Anomaly cpu_95_mem_2_to_4GB = new Anomaly(
//            idSetCPUMem,"cpu_95_mem_2_to_4GB", cpu_95_mem_2_to_4GB_params, false,
//            cpu_95_mem_2_to_4GB_recs);
//
//    //####cpu_95_mem_5_to_7GB###
//    private static final Map<String, Parameters> cpu_95_mem_5_to_7GB_params;
//    static
//    {
//        cpu_95_mem_5_to_7GB_params = new HashMap<>();
//        cpu_95_mem_5_to_7GB_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=95"));
//        cpu_95_mem_5_to_7GB_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{5000 7000}}m"));
//    }
//    private static final Map<String, String> cpu_95_mem_5_to_7GB_recs;
//    static
//    {
//        cpu_95_mem_5_to_7GB_recs = new HashMap<>();
//        cpu_95_mem_5_to_7GB_recs.put("Rec", "23|31");
//    }
//    public static final Anomaly cpu_95_mem_5_to_7GB = new Anomaly(
//            idSetCPUMem,"cpu_95_mem_5_to_7GB", cpu_95_mem_5_to_7GB_params, false,
//            cpu_95_mem_5_to_7GB_recs);
//
//    //####mem_5_to_7GB_hdd_2###
//    private static final Map<String, Parameters> mem_5_to_7GB_hdd_2_params;
//    static
//    {
//        mem_5_to_7GB_hdd_2_params = new HashMap<>();
//        mem_5_to_7GB_hdd_2_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{5000 7000}}m"));
//        mem_5_to_7GB_hdd_2_params.put("stress_hdd", new Parameters("--hdd=2"));
//    }
//    private static final Map<String, String> mem_5_to_7GB_hdd_2_recs;
//    static
//    {
//        mem_5_to_7GB_hdd_2_recs = new HashMap<>();
//        mem_5_to_7GB_hdd_2_recs.put("Rec", "43|95");
//    }
//    public static final Anomaly mem_5_to_7GB_hdd_2 = new Anomaly(
//            idSetMemHDD,"mem_5_to_7GB_hdd_2", mem_5_to_7GB_hdd_2_params, false,
//            mem_5_to_7GB_hdd_2_recs);
//
//    //####cpu_40_mem_2_to_4GB_hdd_1###
//    private static final Map<String, Parameters> cpu_40_mem_2_to_4GB_hdd_1_params;
//    static
//    {
//        cpu_40_mem_2_to_4GB_hdd_1_params = new HashMap<>();
//        cpu_40_mem_2_to_4GB_hdd_1_params.put("stress_cpu", new Parameters("--cpu=8 --cpu-load=40"));
//        cpu_40_mem_2_to_4GB_hdd_1_params.put("stress_mem", new Parameters("--vm=1 --vm-bytes={{2000 4000}}m"));
//        cpu_40_mem_2_to_4GB_hdd_1_params.put("stress_hdd", new Parameters("--hdd=1"));
//    }
//    private static final Map<String, String> cpu_40_mem_2_to_4GB_hdd_1_recs;
//    static
//    {
//        cpu_40_mem_2_to_4GB_hdd_1_recs = new HashMap<>();
//        cpu_40_mem_2_to_4GB_hdd_1_recs.put("Rec", "25|39|72");
//    }
//    public static final Anomaly cpu_40_mem_2_to_4GB_hdd_1 = new Anomaly(
//            idSetCPUMemHDD,"cpu_40_mem_2_to_4GB_hdd_1", cpu_40_mem_2_to_4GB_hdd_1_params,
//            false, cpu_40_mem_2_to_4GB_hdd_1_recs);
//
//
//    public static final List HOST_ANOMALIES = new ArrayList() { {
//        add(cpu_40); add(cpu_70); add(cpu_95);
//        add(mem_2_to_4GB); add(mem_5_to_7GB); add(mem_7_5_to_8_5GB);
//        add(stress_hdd_1); add(stress_hdd_2); add(stress_hdd_3);
//        add(download);
//        add(cpu_70_mem_5_to_7GB); add(cpu_95_mem_2_to_4GB); add(cpu_95_mem_5_to_7GB);
//        add(mem_5_to_7GB_hdd_2); add(cpu_40_mem_2_to_4GB_hdd_1);
//    }};
//
//    //####CPU_95_VM###
//    private static final Map<String, Parameters> cpu_95_vm_params;
//    static
//    {
//        cpu_95_vm_params = new HashMap<>();
//        cpu_95_vm_params.put("stress_cpu", new Parameters("--cpu=1 --cpu-load=95"));
//    }
//    private static final Map<String, String> cpu_95_vm_recs;
//    static
//    {
//        cpu_95_vm_recs = new HashMap<>();
//        cpu_95_vm_recs.put("Rec", "27|44");
//    }
//    public static final Anomaly cpu_95_vm = new Anomaly(
//            idSetCPU,"stress_cpu_95_vm", cpu_95_vm_params, false,
//            cpu_95_vm_recs);
//
//    //####HDD_3_VM###
//    private static final Map<String, String> hdd_3_vm_recs;
//    static
//    {
//        hdd_3_vm_recs = new HashMap<>();
//        hdd_3_vm_recs.put("Rec", "48|96|98");
//    }
//    public static final Anomaly stress_hdd_3_vm = new Anomaly(
//            idSetHDD,"stress_hdd_3", hdd_3_params, false,
//            hdd_3_vm_recs);
//
//    //####downlaod_VM###
//    private static final Map<String, String> download_vm_recs;
//    static
//    {
//        download_vm_recs = new HashMap<>();
//        download_vm_recs.put("Rec", "68");
//    }
//    public static final Anomaly download_vm = new Anomaly(
//            idSetDownload,"download", download_params, false,
//            download_vm_recs);
//
//
//    public static final List VM_ANOMALIES = new ArrayList() { {
//        add(cpu_95_vm); add(stress_hdd_3_vm); add(download_vm);
//    }};
//}
