package com.davofredo.util.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to perform actions on instances of java.util.Map
 */
public class MapUtils {
    private MapUtils() {}

    /**
     * <p>Clones the map provided recursively.</p>
     *
     * <p>"Recursively" means that every inner instance of <b>java.util.Map</b>
     * or <b>java.util.List</b> will be cloned as well.</p>
     *
     * @param from Source map to be cloned
     * @return A recursive clone of "from"
     * @param <K> Java type of key values
     */
    public static <K> Map<K, Object> cloneRecursively(Map<K, Object> from) {
        return from.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), cloneObjectRecursively(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static List<?> cloneListRecursively(List<?> from) {
        return new ArrayList<>(
            from.stream()
                .map(MapUtils::cloneObjectRecursively)
                .toList()
        );
    }

    @SuppressWarnings("unchecked")
    private static Object cloneObjectRecursively(Object from) {
        if (from instanceof Map)
            return cloneRecursively((Map<?, Object>) from);
        if (from instanceof List)
            return cloneListRecursively((List<Object>) from);
        // Only instances of Map or List will be cloned recursively
        return from;
    }

}

class Constants {
    static final String FMT_LIST_ITEM = "%s[%s]";

    private Constants() {}
}
