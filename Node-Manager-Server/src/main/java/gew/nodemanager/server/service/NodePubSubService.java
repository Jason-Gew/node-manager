package gew.nodemanager.server.service;

import gew.nodemanager.common.entity.TopicSubscription;
import gew.nodemanager.common.model.BasicNodeMsg;
import gew.nodemanager.common.model.BriefTopicInfo;
import gew.nodemanager.common.model.PubSubMsgType;

import java.util.List;
import java.util.Map;


/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public interface NodePubSubService {

    @SuppressWarnings("rawtypes")
    String publish(final BasicNodeMsg msg);

    String publish(final String topic, final String message, final String operator);

    List<BriefTopicInfo> listRecentPubTopics(final String signature);

    TopicSubscription subscribe(final String topic, final PubSubMsgType msgType, final String operator);

    TopicSubscription subscribe(TopicSubscription topicSubscription);

    Boolean unsubscribe(final String topic, final PubSubMsgType msgType, final String operator);

    Boolean unsubscribe(TopicSubscription topicSubscription);

    List<BriefTopicInfo> listRecentSubTopics(final String nodeUuid);
}
