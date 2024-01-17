package com.davofredo.util.map;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     This class allows to handle hierarchical trees of maps as
 *     object-oriented structures.
 * </p>
 *
 * <br/>
 * <p>
 *      From this class, developers can read and write data into
 *      inner Map elements (instances of <b>java.util.Map</b> and <b>java.util.List</b>)
 *      in a similar way as JSON objects can be read in JavaScript.
 * </p>
 *
 * <br/>
 * <p>
 *     <b>MapScanner</b> can be initialized in the following ways:
 * </p>
 *
 * <br/>
 * <pre>    var mapScanner = new MapScanner()</pre>
 * <p>Creates an empty mapScanner, ready to get filled with data, from scratch</p><br/>
 *
 * <br/>
 * <pre>    var mapScanner = new MapScanner(map)</pre>
 * <p>Clones "map" recursively and allows to read and write data into the clone created</p>
 */
public class MapScanner {
    private final Map<String, Object> map;

    /**
     * <p>
     *     Creates a new instance of MapScanner, which contains
     *     a clone of <b>map</b>. In consequence, <b>map</b> won't be
     *     modified by any action performed in the MapScanner instance
     *     created from this constructor.
     * </p>
     *
     * <br/>
     * <p>
     *     Use <b>MapScanner::toMap</b> to export MapScanner content into a
     *     <b>java.util.Map</b>.
     * </p>
     *
     * @param map Source Map. A copy of <b>map</b> will be stored in the instance created.
     */
    public MapScanner(Map<String, Object> map) {
        this(map, true);
    }

    /**
     * <p>
     *     Creates a new instance of MapScanner.
     * </p>
     *
     * <br/>
     * <p>
     *     The new instance can store <b>map</b> itself or a clone of <b>map</b>,
     *     depending on the value of <b>clone</b>.
     * </p>
     *
     * <br/>
     * <p>
     *     If <b>clone</b> is <b>true</b>, <b>map</b> won't be
     *     modified by any action performed in the <b>MapScanner</b> instance
     *     created from this constructor. Otherwise any actions performed in
     *     the instance created will be reflected in <b>map</b>".
     * </p>
     *
     * @param map Source Map. Can be cloned or not, depending on the value of <b>clone</b>.
     * @param clone <b>true</b> to store a clone of <b>map</b>. Otherwise, false.
     */
    protected MapScanner(Map<String, Object> map, boolean clone) {
        if (map == null) this.map = new HashMap<>();
        else this.map = clone ? MapUtils.cloneRecursively(map) : map;
    }

    /**
     * <p>
     *     Creates a new instance of MapScanner. Map data can be created from
     *     scratch in the empty instance created from this constructor.
     * </p>
     *
     * <br/>
     * <p>
     *     Use <b>MapScanner::toMap</b> to export MapScanner content into a
     *     <b>java.util.Map</b>.
     * </p>
     */
    public MapScanner() {
        this.map = new HashMap<>();
    }

    /**
     * <p>Exports <b>MapScanner</b> contents into an instance of <b>java.util.Map</b>.</p>
     *
     * <br/>
     * <p>Please note that the output will be a copy of <b>MapScanner's</b> content, NOT the content itself.</p>
     *
     * @return A copy of <b>MapScanner's</b> content.
     */
    public Map<String, Object> toMap() {
        return MapUtils.cloneRecursively(map);
    }

    /**
     * <p>
     *     Provides a new instance of <b>MapScanner</b>, which contains the Map node
     *     indicated in <b>fieldName</b>. If the node indicated in <b>fieldName</b> doesn't exist,
     *     then the node will be created.
     * </p>
     *
     * <br/>
     * <p>
     *     Changes performed in the <b>MapScanner</b> instance provided, will be reflected in the parent
     *     node. So, the following code:
     * </p>
     * <pre>    mapScanner.getSubScanner("person").set("firstName", "John")</pre>
     *
     * <br/>
     * <p>Is equivalent to:</p>
     * <pre>    mapScanner.set("person.firstName", "John")</pre>
     *
     * @param fieldName name of an inner node to use as the source Map. Example <code>"person.address"</code>
     * @return A new <b>MapScanner</b> instance containing the inner node indicated in <b>fieldName</b>.
     */
    @SuppressWarnings("unchecked")
    public MapScanner getSubScanner(String fieldName) {
        Map<String, Object> subMap = get(fieldName, Map.class);
        if (subMap == null) {
            subMap = new HashMap<>();
            set(fieldName, subMap);
        }
        return new MapScanner(subMap, false);
    }

    /**
     * <p>Provides the value of the field indicated in <b>fieldName</b>.</p>
     * <p>
     *     If the value is not an instance of <b>valueType</b>,
     *     <b>com.davofredo.util.map.TypeCastException</b> will be thrown.
     * </p>
     * <p>Returns <b>null</b> when:</p>
     * <ul>
     *     <li>The stored value is <b>null</b></li>
     *     <li>The field doesn't exist</li>
     * </ul>
     *
     * @param fieldName Name of the inner field to read value from. Example: <code>"person.firstName"</code>
     * @param valueType Class of the expected value. Example: <code>String.class</code>
     * @return The value stored in the field indicated by <b>fieldName</b>
     * @param <T> Expected value type
     * @throws com.davofredo.util.map.TypeCastException if <b>valueType</b> is not assignable from the resulting value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String fieldName, Class<T> valueType) {
        if (valueType == null)
            throw new IllegalArgumentException("\"valueType\" argument is mandatory.");
        Object value = get(fieldName);
        if (value == null)
            return null;
        if (valueType.isAssignableFrom(value.getClass()))
            return (T) get(fieldName);
        throw new TypeCastException(String.format("Class %s is not assignable from %s", valueType.getName(), value.getClass().getName()));
    }

    /**
     * <p>Provides the value of the field indicated in <b>fieldName</b>.</p>
     * <p>Returns <b>null</b> when:</p>
     * <ul>
     *     <li>The stored value is <b>null</b></li>
     *     <li>The field doesn't exist</li>
     * </ul>
     *
     * @param fieldName Name of the inner field to read value from. Example: <code>"person.firstName"</code>
     * @return The value stored in the field indicated by <b>fieldName</b>
     */
    @SuppressWarnings("unchecked")
    public Object get(String fieldName) {
        MapAttribute attribute = toAttribute(fieldName);
        Object value = map.get(attribute.getName());
        if (MapAttribute.TYPE_MAP.equals(attribute.getType()) && value instanceof Map)
            return getValueFromSubMap(attribute, (Map<String, Object>) value);
        if (MapAttribute.TYPE_LIST.equals(attribute.getType()) && value instanceof List)
            return getValueFromList(attribute, (List<Object>) value);
        // Assume this is object type
        return value;
    }

    /**
     * <p>Assigns the specified value (<b>value</b>) into the specified field (<b>fieldName</b>).</p>
     * <p>
     *     If the field doesn't exists or any of it's parent nodes is missing, the required
     *     node tree will be created before setting the value.
     * </p>
     *
     * <br/>
     * <p>
     *     <b>Warning: </b>
     *     <ul>
     *         if you use this method to assign an inner instance of <b>java.util.Map</b>,
     *         the Map won't be cloned, so any changes performed in the <b>MapScanner</b>, over the
     *         provided node, will be reflected in the original Map.
     *     </ul>
     * </p>
     *
     * @param fieldName Name of the field to assign the value into. Example: <code>"person.firstName"</code>
     * @param value Value to be assigned into the field indicated by <b>fieldName</b>.
     * @return The current <b>MapScanner</b> instance
     */
    public MapScanner set(String fieldName, Object value) {
        MapAttribute attribute = toAttribute(fieldName);
        if (MapAttribute.TYPE_OBJECT.equals(attribute.getType()))
            map.put(fieldName, value);
        else if (MapAttribute.TYPE_MAP.equals(attribute.getType()))
            putValueIntoSubMap(attribute, value);
        else if (MapAttribute.TYPE_LIST.equals(attribute.getType()))
            putValueIntoList(attribute, value);
        return this;
    }

    /**
     * <p>Removes the specified field (<b>fieldName</b>) from the node tree.</p>
     * <p>Full lists and list items can be removed as well.</p>
     *
     * @param fieldName Name of the field to be removed from the node tree
     * @return The current <b>MapScanner</b> instance
     */
    public MapScanner remove(String fieldName) {
        MapAttribute attribute = toAttribute(fieldName);
        if (!map.containsKey(attribute.getName()))
            // no key means nothing to remove
            return this;
        if (MapAttribute.TYPE_OBJECT.equals(attribute.getType()))
            map.remove(fieldName);
        else if (MapAttribute.TYPE_MAP.equals(attribute.getType()))
            getSubScanner(attribute.getName()).remove(attribute.getRemainingPath());
        else if (MapAttribute.TYPE_LIST.equals(attribute.getType()))
            removeFromList(attribute);
        return this;
    }

    private void removeFromList(MapAttribute attribute) {
        var list = get(attribute.getName(), List.class);
        if (list == null)
            return; // null list
        if (attribute.getIndex() != null) {
            if (attribute.getIndex() >= list.size())
                return; // Out of bounds
            if (StringUtils.isBlank(attribute.getRemainingPath())) {
                // Remove indicated item
                list.remove((int) attribute.getIndex());
                // Else, the index is out of bounds
            } else {
                // Remove field from item
                getSubScanner(String.format(Constants.FMT_LIST_ITEM, attribute.getName(), attribute.getIndex()))
                        .remove(attribute.getRemainingPath());
            }
        } else {
            if (StringUtils.isBlank(attribute.getRemainingPath())) {
                // clear the list
                map.remove(attribute.getName());
            } else {
                // Remove field from all the items
                for (var i = 0; i < list.size(); i++) {
                    getSubScanner(String.format(Constants.FMT_LIST_ITEM, attribute.getName(), i))
                            .remove(attribute.getRemainingPath());
                }
            }
        }
    }

    private Object getValueFromSubMap(MapAttribute attribute, Map<String, Object> map) {
        MapScanner mapScanner = new MapScanner(map, false);
        return mapScanner.get(attribute.getRemainingPath());
    }

    private void putValueIntoSubMap(MapAttribute attribute, Object value) {
        map.put(attribute.getName(), setInnerValue(attribute, value, map.get(attribute.getName())));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> setInnerValue(MapAttribute attribute, Object value, Object subMap) {
        if (subMap != null && !Map.class.isAssignableFrom(subMap.getClass()))
            throw new TypeCastException(attribute, subMap, Map.class, true);
        MapScanner subScanner = new MapScanner((Map<String, Object>) subMap, false);
        subScanner.set(attribute.getRemainingPath(), value);
        return subScanner.map;
    }

    @SuppressWarnings("unchecked")
    private Object getValueFromList(MapAttribute attribute, List<Object> list) {
        // Prevent NullPointerException | ArrayIndexOutOfBoundsException
        if (list == null || (attribute.getIndex() != null && attribute.getIndex() >= list.size()))
            return null;

        if (StringUtils.isBlank(attribute.getRemainingPath())) {
            if (attribute.getIndex() == null)
                // No index will be understood as get full list
                return list;
            // Else get value from index
            return list.get(attribute.getIndex());
        }

        if (attribute.getIndex() == null)
            throw new IllegalStateException(String.format("Field \"%s\" in path \"%s\" is an array, but the index was not specified. Cannot continue through path without an index", attribute.getName(), attribute.getPath()));

        Object value = list.get(attribute.getIndex());
        if (value instanceof Map)
            return getValueFromSubMap(attribute, (Map<String, Object>) value);
        if (value == null)
            return null;
        throw new TypeCastException(String.format(Constants.FMT_LIST_ITEM, attribute.getName(), attribute.getIndex()), attribute.getPath(), value, Map.class, false);
    }

    @SuppressWarnings("unchecked")
    private void putValueIntoList(MapAttribute attribute, Object value) {
        Object obj = map.get(attribute.getName());
        if (obj == null) {
            obj = new ArrayList<>();
            map.put(attribute.getName(), obj);
        }
        else if(!List.class.isAssignableFrom(obj.getClass()))
            throw new TypeCastException(attribute, obj, List.class, true);

        List<Object> list = (List<Object>) obj;
        Object listItem = value;

        if (!StringUtils.isBlank(attribute.getRemainingPath())) {
            // Bring existing list item, if there is, or set null to create a new one
            listItem = attribute.getIndex() != null && list.size() > attribute.getIndex() ?
                    list.get(attribute.getIndex()) : null;
            listItem = setInnerValue(attribute, value, listItem);
        }

        // Set list item
        setIntoList(list, listItem, attribute);
    }

    @SuppressWarnings("unchecked")
    private void setIntoList(List<Object> list, Object listItem, MapAttribute attribute) {
        // Don't assume that the list can be written
        try {
            // Set list item
            if (attribute.getIndex() == null)
                list.add(listItem);
            else
                list.set(attribute.getIndex(), listItem);
        } catch (IndexOutOfBoundsException e) {
            // Preventing any issues with the current list instance...
            setIntoList(list, null, toAttribute(attribute.getName() + "[]"));
            List<Object> writeableList = get(attribute.getName(), List.class);
            // Once ensured that the list is writeable, proceed to fill needed spaces
            while (writeableList.size() <= attribute.getIndex())
                setIntoList(writeableList, null, toAttribute(attribute.getName() + "[]"));
            setIntoList(writeableList, listItem, attribute);
        }
    }

    MapAttribute toAttribute(String fieldPath) {
        MapAttribute attribute = new MapAttribute(fieldPath.trim());
        if (checkForList(attribute)) return attribute;
        if (!StringUtils.isBlank(attribute.getRemainingPath()))
            attribute.setType(MapAttribute.TYPE_MAP);
        else
            attribute.setType(MapAttribute.TYPE_OBJECT);
        return attribute;
    }

    boolean checkForList(MapAttribute attribute) {
        String name = attribute.getName();
        if (!name.contains("[") || !name.endsWith("]"))
            return false;
        // Find index position in name
        int idxStart = name.indexOf("[") + 1;
        int idxEnd = name.length() - 1;
        // List name must not be empty
        if (idxStart < 2)
            return false;
        String strIdx = name.substring(idxStart, idxEnd);
        try {
            if (strIdx.length() > 0)
                attribute.setIndex(Integer.parseInt(strIdx));
            attribute.setType(MapAttribute.TYPE_LIST);
            attribute.setName(name.substring(0, idxStart - 1));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * <p>Indicates if a field exists into the node tree.</p>
     *
     * @param fieldName Name of the field being looked for.
     * @return <b>true</b> if the field is present in the node tree. Otherwise returns <b>false</b>
     */
    public boolean fieldExists(String fieldName) {
        MapAttribute attr = toAttribute(fieldName);

        if (!map.containsKey(attr.getName()))
            return false;

        if (MapAttribute.TYPE_OBJECT.equals(attr.getType()))
            return true;

        if (MapAttribute.TYPE_LIST.equals(attr.getType()) && attr.getIndex() == null) {
            if (StringUtils.isBlank(attr.getRemainingPath()))
                return true; // List field exists, no matter if null as no item was requested
            throw new IllegalStateException(String.format("Field \"%s\" in path \"%s\" is an array, but the index was not specified. Cannot continue through path without an index", attr.getName(), attr.getPath()));
        }

        // A sub-map field or list item is expected, so null is not allowed anymore
        Object obj = map.get(attr.getName());
        if (obj == null) return false;

        if (MapAttribute.TYPE_MAP.equals(attr.getType())) {
            return subMapFieldExists(obj, attr);
        }

        // If not object and not map, then it is a list.
        // Index != null has been confirmed already
        try {
            return listContainsField((List<?>) obj, attr);
        } catch (ClassCastException e) {
            throw new TypeCastException(attr, obj, List.class, false);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean subMapFieldExists(Object subMap, MapAttribute attribute) {
        try {
            return new MapScanner((Map<String, Object>) subMap, false)
                    .fieldExists(attribute.getRemainingPath());
        } catch (ClassCastException e) {
            throw new TypeCastException(attribute, subMap, Map.class, false);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean listContainsField(List<?> list, MapAttribute attribute) {
        if (list.size() <= attribute.getIndex())
            return false; // If index is out of bounds then it doesn't exist
        if (StringUtils.isBlank(attribute.getRemainingPath()))
            return true; // If index is in bounds and item is an object it exists (no matter if null)

        Object subMapItem = list.get(attribute.getIndex());
        // A field from item is requested, so if the item is null then the target field doesn't exist
        if (subMapItem == null)
            return false;

        // In order to search for the target field, the item should be a map
        try {
            return new MapScanner((Map<String, Object>) subMapItem, false)
                    .fieldExists(attribute.getRemainingPath());
        } catch (ClassCastException e) {
            throw new TypeCastException(String.format(Constants.FMT_LIST_ITEM, attribute.getName(), attribute.getIndex()), attribute.getPath(), subMapItem, Map.class, false);
        }
    }

}
