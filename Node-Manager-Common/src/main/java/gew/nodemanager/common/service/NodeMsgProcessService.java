package gew.nodemanager.common.service;

import gew.nodemanager.common.model.BasicNodeMsg;
import gew.nodemanager.common.model.PubSubMsgType;

import java.util.Map;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public interface NodeMsgProcessService {

    Boolean process(final String topic, BasicNodeMsg<String> nodeMsg);

    Boolean process(final String topic, Object rawMsg, PubSubMsgType msgType);

    Boolean process(final String topic, Map<String, Object> jsonMsg);

}
