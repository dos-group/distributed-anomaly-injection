package anomaly.experiment.controller.main;


import anomaly.experiment.controller.objects.Anomaly;
import anomaly.experiment.controller.objects.AnomalyGroup;
import anomaly.experiment.controller.objects.AnomalyScenario;
import anomaly.experiment.controller.objects.Host;
import anomaly.experiment.controller.utils.Config;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 20.02.19.
 */
public class MainTestYAMLTemplate {

    static { Config.initializeLogger(); }
    private static final Logger logger = Logger.getLogger(MainTestYAMLTemplate.class.getName());

    public static void main(String[] args) {

        Anomaly a1 = new Anomaly("idid1", "param,param1");
        Anomaly a2 = new Anomaly("idid2", "param,param2");

        List<Anomaly> anomalyList1 = new ArrayList<>();
        anomalyList1.add(a1);
        anomalyList1.add(a2);

        AnomalyGroup ag1 = new AnomalyGroup("group",anomalyList1);

        List<AnomalyGroup> agList1 = new ArrayList<>();
        agList1.add(ag1);

        Anomaly a3 = new Anomaly("idid3", "param,param3");
        Anomaly a4 = new Anomaly("idid4", "param,param4");

        List<Anomaly> anomalyList2 = new ArrayList<>();
        anomalyList2.add(a3);
        anomalyList2.add(a4);

        Anomaly a5 = new Anomaly("idid5", "param,param5");

        List<Anomaly> anomalyList3 = new ArrayList<>();
        anomalyList2.add(a5);

        AnomalyGroup ag3 = new AnomalyGroup("group3", anomalyList2);
        AnomalyGroup ag4 = new AnomalyGroup("group4", anomalyList3);

        List<AnomalyGroup> agList2 = new ArrayList<>();
        agList2.add(ag3);
        agList2.add(ag4);


        Host h1 = new Host("name1", "ip1", agList1);
        Host h2 = new Host("name2", "ip2", agList2);

        List<Host> hostList = new ArrayList<>();
        hostList.add(h1);
        hostList.add(h2);

        Map<String, List<AnomalyGroup>> groups = new HashMap<>();
        groups.putIfAbsent("groups_host", agList1);
        groups.putIfAbsent("groups_vm", agList2);

        AnomalyScenario scenario = new AnomalyScenario();
        scenario.setHosts(hostList);
        scenario.setAnomalyGroups(groups);


        String inventoryFilePath = "/home/alex/workspace/" +
                "distributed-anomaly-injection/anomaly-experiment-controller/src/main/resources/store.yml";
        try {
            //final InputStream input = new FileInputStream(new File(inventoryFilePath));
            StringWriter writer = new StringWriter();
            Yaml yaml = new Yaml(new org.yaml.snakeyaml.constructor.Constructor(AnomalyScenario.class));
            yaml.dump(scenario, writer);
            //scenario = yaml.load(input);
            System.out.println(writer.toString());
            logger.log(Level.INFO, "Inventory file " + inventoryFilePath + " successfully read.");
        } catch (Exception ex) {
            logger.severe("File " + inventoryFilePath + " does not exist.");
            return;
        } //catch (Exception e){
//            logger.severe("Failed to read inventory file " + inventoryFilePath + ".");
//            e.printStackTrace();
//            return;
//        }
    }

}
