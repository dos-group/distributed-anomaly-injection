package anomaly.experiment.controller.utils.filter;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by alex on 28.08.17.
 */
public class Filter {

    public interface NameFilter {
        boolean shouldInclude(String name);
    }

    public static class ExcludeNameSelection implements NameFilter {
        private final Set<String> excludedNames;

        public ExcludeNameSelection(String... namesToExclude) {
            excludedNames = new HashSet<>(Arrays.asList(namesToExclude));
        }

        @Override
        public boolean shouldInclude(String name) {
            return !excludedNames.contains(name);
        }
    }

    public static class ExcludeNameRegexSelection implements NameFilter {
        private final List<Pattern> excluded = new ArrayList<>();

        public ExcludeNameRegexSelection(String... excludeRegexes) {
            for (String regexString : excludeRegexes) {
                excluded.add(Pattern.compile(regexString));
            }
        }

        @Override
        public boolean shouldInclude(String name) {
            for (Pattern regex : excluded) {
                if (regex.matcher(name).matches()) {
                    return false;
                }
            }
            return true;
        }

    }

    public static class IncludeNameRegexSelection implements NameFilter {
        private final List<Pattern> included = new ArrayList<>();

        public IncludeNameRegexSelection(String... includeRegexes) {
            for (String regexString : includeRegexes) {
                included.add(Pattern.compile(regexString));
            }
        }

        @Override
        public boolean shouldInclude(String name) {
            for (Pattern regex : included) {
                if (regex.matcher(name).matches()) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class IncludeNameSelection extends ExcludeNameSelection {
        public IncludeNameSelection(String... namesToInclude) {
            super(namesToInclude);
        }

        @Override
        public boolean shouldInclude(String name) {
            return !super.shouldInclude(name);
        }
    }
}
