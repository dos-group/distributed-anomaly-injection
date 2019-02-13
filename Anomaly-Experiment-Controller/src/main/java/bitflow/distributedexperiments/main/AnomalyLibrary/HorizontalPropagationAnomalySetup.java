package bitflow.distributedexperiments.main.AnomalyLibrary;

import bitflow.distributedexperiments.objects.Anomaly;
import bitflow.distributedexperiments.objects.Parameters;

import java.util.*;

/**
 * Created by alex on 06.11.17.
 */
public class HorizontalPropagationAnomalySetup implements AnomalySetUp{

    private static final Set<String> idSetBandwidth = new HashSet<>(Arrays.asList("bandwidth"));
    private static final Set<String> idSetPacketLoss = new HashSet<>(Arrays.asList("packet_loss"));
    private static final Set<String> idSetPacketDuplicate = new HashSet<>(Arrays.asList("packet_duplicate"));
    private static final Set<String> idSetPacketCorruption = new HashSet<>(Arrays.asList("packet_corruption"));
    private static final Set<String> idSetPacketReordering = new HashSet<>(Arrays.asList("packet_reordering"));

    //####Packet_loss_HOST###
    private static final Map<String, Parameters> packetloss_host_p;
    static
    {
        packetloss_host_p = new HashMap<>();
        packetloss_host_p.put("packet_loss", new Parameters("eno1 enp2s0 50"));
    }
    public static final Anomaly packetloss_host = new Anomaly(
            idSetPacketLoss,"packetloss_host", packetloss_host_p, false);

    //####Packet_loss_VM###
    private static final Map<String, Parameters> packetloss_vm_p;
    static
    {
        packetloss_vm_p = new HashMap<>();
        packetloss_vm_p.put("packet_loss", new Parameters("ens3 50"));
    }
    public static final Anomaly packetloss_vm = new Anomaly(
            idSetPacketLoss,"packetloss_vm", packetloss_vm_p, false);

    //####Bandwidth_HOST###
    private static final Map<String, Parameters> bandwidth_host_p;
    static
    {
        bandwidth_host_p = new HashMap<>();
        bandwidth_host_p.put("bandwidth", new Parameters("eno1 enp2s0 5Kbps"));
    }
    public static final Anomaly bandwidth_host = new Anomaly(
            idSetBandwidth,"bandwidth_host", bandwidth_host_p, false);

    //####Bandwidth_VM###
    private static final Map<String, Parameters> bandwidth_vm_p;
    static
    {
        bandwidth_vm_p = new HashMap<>();
        bandwidth_vm_p.put("bandwidth", new Parameters("ens3 5Kbps"));
    }
    public static final Anomaly bandwidth_vm = new Anomaly(
            idSetBandwidth,"bandwidth_vm", bandwidth_vm_p, false);

    //####Packet_Duplicate_HOST###
    private static final Map<String, Parameters> packet_duplicate_host_p;
    static
    {
        packet_duplicate_host_p = new HashMap<>();
        packet_duplicate_host_p.put("packet_duplicate", new Parameters("eno1 enp2s0 50"));
    }
    public static final Anomaly packet_duplicate_host = new Anomaly(
            idSetPacketDuplicate,"packet_duplicate_host", packet_duplicate_host_p, false);

    //####Packet_Duplicate_VM###
    private static final Map<String, Parameters> packet_duplicate_vm_p;
    static
    {
        packet_duplicate_vm_p = new HashMap<>();
        packet_duplicate_vm_p.put("packet_duplicate", new Parameters("ens3 50"));
    }
    public static final Anomaly packet_duplicate_vm = new Anomaly(
            idSetPacketDuplicate,"packet_duplicate_vm", packet_duplicate_vm_p, false);

    //####Packet_corruption_HOST###
    private static final Map<String, Parameters> packet_corruption_host_p;
    static
    {
        packet_corruption_host_p = new HashMap<>();
        packet_corruption_host_p.put("packet_corruption", new Parameters("eno1 enp2s0 50"));
    }
    public static final Anomaly packet_corruption_host = new Anomaly(
            idSetPacketCorruption,"packet_corruption_host", packet_corruption_host_p, false);

    //####Packet_corruption_vm###
    private static final Map<String, Parameters> packet_corruption_vm_p;
    static
    {
        packet_corruption_vm_p = new HashMap<>();
        packet_corruption_vm_p.put("packet_corruption", new Parameters("ens3 50"));
    }
    public static final Anomaly packet_corruption_vm = new Anomaly(
            idSetPacketCorruption,"packet_corruption_vm", packet_corruption_vm_p, false);

    //####Packet_reordering_HOST###
    private static final Map<String, Parameters> packet_reordering_host_p;
    static
    {
        packet_reordering_host_p = new HashMap<>();
        packet_reordering_host_p.put("packet_reordering", new Parameters("eno1 enp2s0 50"));
    }
    public static final Anomaly packet_reordering_host = new Anomaly(
            idSetPacketReordering,"packet_reordering_host", packet_reordering_host_p, false);

    //####Packet_reordering_vm###
    private static final Map<String, Parameters> packet_reordering_vm_p;
    static
    {
        packet_reordering_vm_p = new HashMap<>();
        packet_reordering_vm_p.put("packet_reordering", new Parameters("ens3 50"));
    }
    public static final Anomaly packet_reordering_vm = new Anomaly(
            idSetPacketReordering,"packet_reordering_vm", packet_reordering_vm_p, false);


    public static final List<Anomaly> HOST_ANOMALIES = new ArrayList<Anomaly>() { {
        add(bandwidth_host); add(packet_corruption_host); add(packetloss_host);
        add(packet_duplicate_host); add(packet_reordering_host);
    }};

    public static final List<Anomaly> VM_ANOMALIES = new ArrayList<Anomaly>() { {
         add(packetloss_vm); add(bandwidth_vm); add(packet_duplicate_vm);
         add(packet_corruption_vm); add(packet_reordering_vm);
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
