package com.davofredo.util.map;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
class MapAttribute {
    static final String TYPE_LIST = "list";
    static final String TYPE_MAP = "map";
    static final String TYPE_OBJECT = "object";

    private String path;
    private String name;
    private String type;
    private Integer index;
    private String remainingPath;

    MapAttribute(String path) {
        this.path = path;
        String[] pathArray = path.split("\\.");
        this.name = pathArray[0].trim();
        if (pathArray.length > 1 && !StringUtils.isBlank(pathArray[1]))
            remainingPath = path.substring(path.indexOf(".") + 1).trim();
    }
}
