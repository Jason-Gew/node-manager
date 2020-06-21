package gew.nodemanager.server.service;

import gew.nodemanager.common.entity.TopicSubscription;
import gew.nodemanager.common.model.TopicCrudException;
import gew.nodemanager.server.repository.TopicSubscriptionRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
@Service
public class TopicManagerImpl implements InitializingBean, TopicManager {


    @Autowired
    private TopicSubscriptionRepository subscriptionRepository;


    @Override
    public void afterPropertiesSet()  {
        log.info("-> Initializing Topic Manager");
    }

    @Override
    @Transactional
    public TopicSubscription addSubscribeTopic(TopicSubscription topicSubscription) {
        checkTopic(topicSubscription);
        if (exists(topicSubscription.getNodeUuid(), topicSubscription.getTopic())) {
            throw new TopicCrudException("Topic for Node Already Exist");
        }
        try {
            TopicSubscription result = subscriptionRepository.save(topicSubscription);
            log.info("Add Subscribe Topic [{}] For Node [{}] By {} Success", topicSubscription.getTopic(),
                    topicSubscription.getNodeUuid(), topicSubscription.getOperator());
            return result;

        } catch (Exception err) {
            log.error("Add Subscribe Topic [{}] For Node [{}] By {} Failed: {}", topicSubscription.getTopic(),
                    topicSubscription.getNodeUuid(), topicSubscription.getOperator(), err.getMessage());
            throw new TopicCrudException("Add Subscribe Topic Failed: " + err.getMessage(), err.getCause());
        }
    }

    @Override
    public List<TopicSubscription> getSubscribeTopic(final String nodeUuid) {
        if (StringUtils.isBlank(nodeUuid)) {
            return new ArrayList<>(0);
        }
        return subscriptionRepository.findTopicByNodeUuid(nodeUuid);
    }

    @Override
    public TopicSubscription getSubscribeTopic(String nodeUuid, String topic) {
        if (StringUtils.isAnyBlank(nodeUuid, topic)) {
            return null;
        }
        try {
            TopicSubscription record = subscriptionRepository.findTopicByNodeUuidAndTopic(nodeUuid, topic);
            return record;

        } catch (Exception err) {
            log.error("Get Subscribe Topic By [NodeUUID={}, Topic={}] Failed: {}", nodeUuid, topic, err.getMessage());
            throw new TopicCrudException("Get SubScribe Topic Failed: " + err.getMessage());
        }
    }

    @Override
    @Transactional
    public TopicSubscription updateSubscribeTopic(TopicSubscription topicSubscription) {
        checkTopic(topicSubscription);
        if (!exists(topicSubscription.getNodeUuid(), topicSubscription.getTopic())) {
            throw new TopicCrudException("Topic for Node Does Not Exist");
        }
        try {
            TopicSubscription result = subscriptionRepository.save(topicSubscription);
            log.info("Update Subscribe Topic [{}] For Node [{}] By {} Success", topicSubscription.getTopic(),
                    topicSubscription.getNodeUuid(), topicSubscription.getOperator());
            return result;

        } catch (Exception err) {
            log.error("Update Subscribe Topic [{}] For Node [{}] By {} Failed: {}", topicSubscription.getTopic(),
                    topicSubscription.getNodeUuid(), topicSubscription.getOperator(), err.getMessage());
            throw new TopicCrudException("Add Subscribe Topic Failed: " + err.getMessage(), err.getCause());
        }
    }

    @Override
    public Boolean deleteSubscribeTopic(final String nodeUuid, final String topic) {
        TopicSubscription record = getSubscribeTopic(nodeUuid, topic);
        if (record == null) {
            throw new TopicCrudException("Topic Does Not Exist");
        }
        return deleteSubscribeTopic(record.getId());
    }

    @Override
    public Boolean deleteSubscribeTopic(Long id) {
        if (id == null || id < 1) {
            return false;
        }
        try {
            subscriptionRepository.deleteById(id);
            return true;
        } catch (Exception err) {
            log.error("Delete Subscribe Topic ID = {} Failed: {}", id, err.getMessage());
            return false;
        }
    }

    @Override
    public boolean exists(final String nodeUuid, final String topic) {
        if (StringUtils.isAnyBlank(nodeUuid, topic)) {
            return false;
        }
        return subscriptionRepository.existsByNodeUuidAndTopic(nodeUuid, topic);
    }

    @Override
    public long countNodeSubscribeTopics(final String nodeUuid) {
        if (StringUtils.isBlank(nodeUuid)) {
            return 0L;
        }
        return subscriptionRepository.countByNodeUuid(nodeUuid);
    }

    @Override
    public long countSubscribeTopics(final String topic) {
        if (StringUtils.isBlank(topic)) {
            return 0L;
        }
        return subscriptionRepository.countByTopic(topic);
    }

    protected void checkTopic(final TopicSubscription topicSubscription) {
        if (topicSubscription == null) {
            throw new TopicCrudException("TopicSubscription is Null");
        } else if (StringUtils.isBlank(topicSubscription.getNodeUuid())) {
            throw new TopicCrudException("Invalid Node UUID");
        } else if (StringUtils.isBlank(topicSubscription.getTopic())) {
            throw new TopicCrudException("Invalid Topic");
        } else if (topicSubscription.getPubSubMsgType() == null) {
            throw new TopicCrudException("Invalid Topic Msg Type");
        } else if (StringUtils.isEmpty(topicSubscription.getOperator())) {
            throw new TopicCrudException("Invalid Operator");
        }
    }
}
