package gew.nodemanager.common.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public enum PubSubMsgType {

    BYTES("BYTES"),

    TEXT("TEXT"),

    JSON("JSON"),

    FILE("FILE"),

    BASIC_NODE_MSG("BASIC_NODE_MSG"),

    NODE_EVENT_MSG("NODE_EVENT_MSG");


    private String type;

    PubSubMsgType(final String type) {
        this.type = type;
    }


    public String getType() {
        return this.type;
    }

    public static PubSubMsgType toType(final String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        switch (input.toUpperCase().replace("_", "")) {
            case "BYTEARRAY":
            case "BYTES":
                return BYTES;

            case "STRING":
            case "TEXT":
                return TEXT;

            case "IMAGE":
            case "MEDIA":
            case "FILE":
                return FILE;

            case "JSONSTRING":
            case "JSONOBJECT":
            case "JSON":
                return JSON;

            case "BASICNODEMSG":
                return BASIC_NODE_MSG;

            case "NODEEVENTMSG":
                return NODE_EVENT_MSG;

            default:
                return null;
        }
    }
}
