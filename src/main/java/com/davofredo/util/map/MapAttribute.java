package com.davofredo.util.map;

import org.apache.commons.lang3.StringUtils;

class MapAttribute {
    static final String TYPE_LIST = "list";
    static final String TYPE_MAP = "map";
    static final String TYPE_OBJECT = "object";

    private final String path;
    private String name;
    private String type;
    private Integer index;
    private final String remainingPath;

    MapAttribute(String path) {
        this.path = path;
        String[] pathArray = path.split("\\.");
        this.name = pathArray[0].trim();
        remainingPath = pathArray.length > 1 && !StringUtils.isBlank(pathArray[1])
                ? path.substring(path.indexOf(".") + 1).trim()
                : null;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getRemainingPath() {
        return remainingPath;
    }

}
