package bitflow.distributedexperiments.utils.filter;

import bitflow.distributedexperiments.objects.targets.Target;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 27.08.17.
 */
public class TargetNameFilter {

    private final Filter.NameFilter filter;


    public TargetNameFilter(Filter.NameFilter filter) {
        this.filter = filter;
    }

    public Map<String, List<Target>> filterTargets(Map<String, List<Target>> targets){
        List<Target> tmp;
        for(Iterator<Map.Entry<String, List<Target>>> it = targets.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, List<Target>> entry = it.next();
            tmp = doFilterTargets(entry.getValue());
            if (tmp.isEmpty())
                it.remove();
            else
                targets.put(entry.getKey(), tmp);
        }

        return targets;
    }

    private List<Target> doFilterTargets(List<Target> targets){
        List<Target> result = new ArrayList<>();
        for(Target t : targets){
            if(this.filter.shouldInclude(t.getName()))
                result.add(t);
        }
        return result;
    }
}
