package anomaly.experiment.controller.main;

import anomaly.experiment.controller.objects.AnomalyGroup;
import anomaly.experiment.controller.objects.Host;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 20.02.19.
 */
public class TestMain {

//    private static class MyConstructor extends Constructor {
//        public MyConstructor(Class<? extends Object> theRoot) {
//            super(theRoot);
//            this.yamlConstructors.put(null, new ConstructYamlObject());
//        }
//
//        @Override
//        protected Object constructObject(Node node) {
//            return super.constructObject(node);
//        }
//
//        /**
//         * Construct an instance when the runtime class is not known but a global
//         * tag with a class name is defined. It delegates the construction to the
//         * appropriate constructor based on the node kind (scalar, sequence,
//         * mapping)
//         */
//        protected class ConstructYamlObject implements Construct {
//
//            private Construct getConstructor(Node node) {
//                Class<?> cl = getClassForNode(node);
//                node.setType(cl);
//                // call the constructor as if the runtime class is defined
//                Object nodeID = node.getNodeId();
//                Construct constructor = yamlClassConstructors.get(node.getNodeId());
//                return constructor;
//            }
//
//            public Object construct(Node node) {
//                System.out.println("hey");
//                try {
//                    return getConstructor(node).construct(node);
//                } catch (ConstructorException e) {
//                    throw e;
//                } catch (Exception e) {
//                    throw new ConstructorException(null, null, "Can't construct a java object for "
//                            + node.getTag() + "; exception=" + e.getMessage(), node.getStartMark(), e);
//                }
//            }
//
//            public void construct2ndStep(Node node, Object object) {
//                try {
//                    getConstructor(node).construct2ndStep(node, object);
//                } catch (Exception e) {
//                    throw new ConstructorException(
//                            null, null, "Can't construct a second step for a java object for "
//                            + node.getTag() + "; exception=" + e.getMessage(),
//                            node.getStartMark(), e);
//                }
//            }
//        }
//
//        public class ConstructorException extends MarkedYAMLException {
//            private static final long serialVersionUID = -8816339931365239910L;
//
//            protected ConstructorException(String context, Mark contextMark, String problem,
//                                           Mark problemMark, Throwable cause) {
//                super(context, contextMark, problem, problemMark, cause);
//            }
//
//            protected ConstructorException(String context, Mark contextMark, String problem,
//                                           Mark problemMark) {
//                this(context, contextMark, problem, problemMark, null);
//            }
//        }
//    }




    public static void main(String[] args) {

        String path = "/home/alex/workspace/distributed-anomaly-injection/Anomaly-Experiment-Controller/src/main/resources/example.yml";
        try {
            //final InputStream input = new FileInputStream(new File(path));
            //final Yaml yaml = new Yaml(constructor);
            //Object object = yaml.load(input);

            Yaml yaml = new Yaml(new Constructor(Items.class));
            final InputStream input = new FileInputStream(new File(path));
            Items scenario = (Items) yaml.load(input);


            System.out.println("Loaded");


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        finally {
        }
    }

    /**
     * Helper class to deserialize anomaly scenario definition YAML file.
     */
    static class Items {
        public Map<String, List<AnomalyGroup>> anomalyGroups;
        public List<Host> hosts;
    }

}
