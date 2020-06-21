package gew.nodemanager.common.model;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class NodeEventMsg extends BasicNodeMsg<String> {

    private NodeEventType eventType;


    public NodeEventMsg() {
        super();
    }

    public NodeEventMsg(String topic) {
        super(topic);
    }

    public NodeEventMsg(String topic, String payload) {
        super(topic, payload);
    }

    public NodeEventMsg(String topic, String payload, NodeEventType eventType) {
        super(topic, payload);
        this.eventType = eventType;
    }

    public NodeEventType getEventType() {
        return eventType;
    }

    public void setEventType(NodeEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "NodeEventMsg{" +
                "eventType=" + eventType +
                '}';
    }
}
