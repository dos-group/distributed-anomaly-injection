//package bitflow.distributedexperiments;
//
//import bitflow.distributedexperiments.objects.targets.Target;
//import com.google.common.io.CharSource;
//import com.google.common.io.Files;
//import org.apache.commons.codec.Charsets;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
///**
// *
// * @author fschmidt and alex
// */
//public class TargetFactory {
//
//    private final static String JSN_KEY_VARS = "vars";
//    private final static String JSN_KEY_HOSTS = "hosts";
//    private final static String JSN_KEY_INJECTOR = "injector-endpoint";
//    private final static String JSN_KEY_COLLECTOR = "ansible_host";
//    private final static String JSN_KEY_SERVICE_GROUP = "vnf_service";
//
//    private final static String PHYSICAL_HOST_SERVICE_NAME = "hypervisor";
//
//    private final static String DEFAULT_COLLECTOR_PORT = "7777";
//
//    private TargetGrouper grouper;
//
//    private interface TargetParser{
//        Target parseTarget(JSONObject jsn);
//    }
//
//    public interface TargetGrouper{
//        Target group(Target target);
//    }
//
//    public TargetFactory(TargetGrouper grouper){
//        this.grouper = grouper;
//    }
//
//    public Map<String, List<Target>> parseJSONHostsFileForCollectorTargets(String pathToJSONFile)
//            throws IOException{
//        //return this.parseJSONHostsFile(pathToJSONFile, jsn -> this.parseCollectorTarget(jsn));
//        return null;
//    }
//
//    public Map<String, List<Target>> parseJSONHostsFileForInjectorTargets(String pathToJSONFile)
//            throws IOException{
//        //return this.parseJSONHostsFile(pathToJSONFile, jsn -> this.parseInjectorTarget(jsn));
//        return null;
//    }
//
//    private Map<String, List<Target>> parseJSONHostsFile(String pathToJSONFile, TargetParser parser)
//            throws IOException {
//        Map<String, List<Target>> result = new HashMap<>();
//        Target target;
//
//        CharSource text = Files.asCharSource(new File(pathToJSONFile), Charsets.UTF_8);
//        String finalText = text.read();
//        JSONObject jObj = new JSONObject(finalText);
//        for (String key : jObj.keySet()) {
//            JSONObject subObj = jObj.getJSONObject(key);
//            if (subObj.keySet().contains("hosts") && subObj.keySet().contains("vars")) {
//                target = parser.parseTarget(subObj);
//                if(target != null){
//                    String group = target.getGroup();
//                    if(result.containsKey(group)) {
//                        result.get(group).add(target);
//                    }else{
//                        result.put(group, new ArrayList<>());
//                        result.get(group).add(target);
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//    /*private CollectorTarget parseCollectorTarget(JSONObject jsn){
//        CollectorTarget result = null;
//
//        JSONObject vars = jsn.getJSONObject(JSN_KEY_VARS);
//        JSONArray hosts = jsn.getJSONArray(JSN_KEY_HOSTS);
//
//        String address, name = "", service_name = "";
//        if(vars.keySet().contains(JSN_KEY_COLLECTOR)) {
//            address = vars.getString(JSN_KEY_COLLECTOR);
//            if (!hosts.isNull(0)) {
//                name = hosts.getString(0);
//            }
//            if(vars.keySet().contains(JSN_KEY_SERVICE_GROUP))
//                service_name = vars.getString(JSN_KEY_SERVICE_GROUP);
//            else
//                service_name = PHYSICAL_HOST_SERVICE_NAME;
//            result = new CollectorTarget(name, address, DEFAULT_COLLECTOR_PORT, service_name);
//            grouper.group(result);
//        }
//        return result;
//    }*/
//
//   /* private InjectorTarget parseInjectorTarget(JSONObject jsn){
//        InjectorTarget result = null;
//
//        JSONObject vars = jsn.getJSONObject(JSN_KEY_VARS);
//        JSONArray hosts = jsn.getJSONArray(JSN_KEY_HOSTS);
//
//        String address_port, name = "", service_name = "";
//        if(vars.keySet().contains(JSN_KEY_INJECTOR)) {
//            address_port = vars.getString(JSN_KEY_INJECTOR);
//            if (!hosts.isNull(0)) {
//                name = hosts.getString(0);
//            }
//            if(vars.keySet().contains(JSN_KEY_SERVICE_GROUP))
//                service_name = vars.getString(JSN_KEY_SERVICE_GROUP);
//            else
//                service_name = PHYSICAL_HOST_SERVICE_NAME;
//            result = new InjectorTarget(name, address_port.split(":")[0],
//                    address_port.split(":")[1], service_name);
//            grouper.group(result);
//        }
//        return result;
//    }*/
//
//
//
//    public static class GroupByHostname implements TargetGrouper{
//        @Override
//        public Target group(Target target) {
//            target.setGroup(target.getName());
//            return target;
//        }
//    }
//
//    public static class GroupVideoOnDemandHosts implements TargetGrouper{
//
//        public final static String VM_GROUP = "vnf_service_regex";
//
//        public final static String GROUP_NAME_BALANCER_HOST = "vod-host-balancer";
//        private static final List<String> BALANCER_HOSTS = Arrays.asList(
//                "wally192", "wally183");
//
//        public final static String GROUP_NAME_CLIENT_HOST = "vod-host-client";
//        private static final List<String> CLIENT_HOSTS = Arrays.asList(
//                "wally199", "wally200");
//
//        public final static String GROUP_NAME_BACKEND_HOST = "vod-host-backend";
//        private static final List<String> BACKEND_HOSTS = Arrays.asList(
//                "wally198", "wally197", "wally195", "wally194", "wally193");
//
//        @Override
//        public Target group(Target target) {
//            if(target.getService_name().equals(PHYSICAL_HOST_SERVICE_NAME))
//                target.setGroup(getStaticGroup(target.getName()));
//            else
//                target.setGroup(target.getService_name());
//            return target;
//        }
//
//        private String getStaticGroup(String name) {
//            if(BACKEND_HOSTS.contains(name)){
//                return GROUP_NAME_BACKEND_HOST;
//            }else if(BALANCER_HOSTS.contains(name)){
//                return GROUP_NAME_BALANCER_HOST;
//            }else if(CLIENT_HOSTS.contains(name)){
//                return GROUP_NAME_CLIENT_HOST;
//            }else{
//                return "";
//            }
//        }
//    }
//
//
//}
