package gew.nodemanager.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gew.nodemanager.common.model.BasicNodeMsg;
import gew.nodemanager.common.model.NodeEventMsg;
import gew.nodemanager.common.model.PubSubMsgType;
import gew.nodemanager.common.service.NodeMsgProcessService;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
public class RedisMsgListener implements MessageListener {

    private final Long id;

    private String topic;

    private PubSubMsgType pubSubMsgType;

    private NodeMsgProcessService msgProcessService;


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public RedisMsgListener(Long id, PubSubMsgType pubSubMsgType) {
        this.id = id;
        this.pubSubMsgType = pubSubMsgType;
    }

    public RedisMsgListener(Long id, PubSubMsgType pubSubMsgType, NodeMsgProcessService msgProcessService) {
        this.id = id;
        this.pubSubMsgType = pubSubMsgType;
        this.msgProcessService = msgProcessService;
    }

    private void msgProcess(String channel, byte[] body) throws IOException {
        switch (this.pubSubMsgType) {
            case BASIC_NODE_MSG: {
                BasicNodeMsg<String> nodeMsg = OBJECT_MAPPER.readValue(body, BasicNodeMsg.class);
                msgProcessService.process(channel, nodeMsg);
                break;
            }
            case NODE_EVENT_MSG: {
                NodeEventMsg eventMsg = OBJECT_MAPPER.readValue(body, NodeEventMsg.class);
                msgProcessService.process(channel, eventMsg);
                break;
            }
            case JSON: {
                JSONObject jsonObject = new JSONObject(new String(body, StandardCharsets.UTF_8));
                msgProcessService.process(channel, jsonObject.toMap());
                break;
            }
            case TEXT: {
                msgProcessService.process(channel, new String(body, StandardCharsets.UTF_8), PubSubMsgType.TEXT);
                break;
            }
            case FILE: {
                msgProcessService.process(channel, body, PubSubMsgType.FILE);
                break;
            }
            case BYTES: {
                msgProcessService.process(channel, body, PubSubMsgType.BYTES);
                break;
            }
            default: {
                log.warn("Unrecognized Registration Message Type [{}], System Drop Message", pubSubMsgType);
            }
        }
    }

    @Override
    public void onMessage(@NotNull Message message, byte[] pattern) {
        if (msgProcessService == null) {
            log.info("Received Redis Message From Topic [{}]: {}", new String(message.getChannel()),
                    new String(message.getBody(), StandardCharsets.UTF_8));
        } else {
            log.info("Received Redis Message From Topic [{}] with Registered Type: {}",
                    new String(message.getChannel()), this.pubSubMsgType);
            try {
                msgProcess(new String(message.getChannel(), StandardCharsets.UTF_8), message.getBody());
            } catch (Exception err) {
                log.error("Message Process Failed Topic [{}]: {}", new String(message.getChannel()),
                        new String(message.getBody()));
            }
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisMsgListener that = (RedisMsgListener) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(topic, that.topic) &&
                pubSubMsgType == that.pubSubMsgType &&
                Objects.equals(msgProcessService, that.msgProcessService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, topic, pubSubMsgType, msgProcessService);
    }
}
