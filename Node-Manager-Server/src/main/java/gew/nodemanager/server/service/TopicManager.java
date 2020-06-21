package gew.nodemanager.server.service;

import gew.nodemanager.common.entity.TopicSubscription;

import java.util.List;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public interface TopicManager {

    TopicSubscription addSubscribeTopic(TopicSubscription topicSubscription);

    List<TopicSubscription> getSubscribeTopic(final String nodeUuid);

    TopicSubscription getSubscribeTopic(final String nodeUuid, final String topic);

    TopicSubscription updateSubscribeTopic(TopicSubscription topicSubscription);

    Boolean deleteSubscribeTopic(final String nodeUuid, final String topic);

    Boolean deleteSubscribeTopic(final Long id);

    boolean exists(final String nodeUuid, final String topic);

    long countNodeSubscribeTopics(final String nodeUuid);

    long countSubscribeTopics(final String topic);
}
