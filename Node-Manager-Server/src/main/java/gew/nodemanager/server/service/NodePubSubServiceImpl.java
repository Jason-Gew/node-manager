package gew.nodemanager.server.service;

import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gew.nodemanager.common.entity.TopicSubscription;
import gew.nodemanager.common.model.BasicNodeMsg;
import gew.nodemanager.common.model.BriefTopicInfo;
import gew.nodemanager.common.model.ConstantParams;
import gew.nodemanager.common.model.PubSubMsgType;
import gew.nodemanager.common.model.TopicCrudException;
import gew.nodemanager.common.service.NodeMsgProcessService;
import gew.nodemanager.common.util.HashUtil;
import gew.nodemanager.common.util.NetworkInfo;
import gew.nodemanager.common.util.UidGenerator;
import gew.nodemanager.server.config.NodeManagerConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static gew.nodemanager.common.model.ConstantParams.INTEGER_DT_PREFIX;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
@Service
public class NodePubSubServiceImpl implements NodePubSubService, InitializingBean {

    @Autowired
    private NodeManagerConfig nodeManagerConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier(value = "redisMsgListenContainer")
    private RedisMessageListenerContainer redisMsgListenContainer;

    @Autowired(required = false)
    private NodeMsgProcessService msgProcessService;

    @Autowired
    private TopicManager topicManager;

    private Cache<String, RedisMsgListener> subTopicCache;

    private Cache<String, Set<BriefTopicInfo>> pubTopicCache;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (subTopicCache == null) {
            subTopicCache = CacheBuilder.newBuilder()
                    .initialCapacity(32)
                    .maximumSize(256)
                    .build();
        }
        if (pubTopicCache == null) {
            pubTopicCache = CacheBuilder.newBuilder()
                    .initialCapacity(128)
                    .maximumSize(1024)
                    .expireAfterAccess(24, TimeUnit.HOURS)
                    .build();
        }
        try {
            List<TopicSubscription> subscriptions = topicManager.getSubscribeTopic(nodeManagerConfig.getId());
            if (!CollectionUtils.isEmpty(subscriptions)) {
               for (TopicSubscription subscription : subscriptions) {
                   if (subscription.getSubscribing()) {
                       this.subscribe(subscription);
                       log.info("Re-subscribe to Topic [{}] by System", subscription.getTopic());
                   }
               }
            }
        } catch (Exception err) {
            log.error("Re-subscribe Topic(s) Failed: {}", err.getMessage());
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String publish(BasicNodeMsg msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Invalid Message");
        } else if (StringUtils.isBlank(msg.getTopic())) {
            throw new IllegalArgumentException("Invalid Topic");
        } else if (StringUtils.containsAny(msg.getTopic(), ConstantParams.INVALID_TOPIC_SYMBOLS)) {
            throw new IllegalArgumentException("Topic Contains Invalid Symbol(s)");
        } else if (msg.getPayload() == null) {
            throw new IllegalArgumentException("Invalid Message Payload");
        }
        if (StringUtils.isBlank(msg.getUuid()) || msg.getUuid().length() < 16) {
            String uuid = UidGenerator.generate(INTEGER_DT_PREFIX.format(msg.getDatetime()), 32);
            log.warn("Message Send to Topic [{}] From Signature [{}] Has Invalid UUID, System Generate: {}",
                    msg.getTopic(), msg.getSignature(), uuid);
            msg.setUuid(uuid);
        }
        try {
            redisTemplate.convertAndSend(msg.getTopic(), msg);
            Set<BriefTopicInfo> topicInfoSet = pubTopicCache.getIfPresent(msg.getTopic());
            if (topicInfoSet == null) {
                topicInfoSet = new HashSet<>();
            }
            topicInfoSet.add(new BriefTopicInfo(msg.getTopic(), msg.getSignature(), Instant.now()));
            pubTopicCache.put(msg.getTopic(), topicInfoSet);
            return msg.getUuid();

        } catch (Exception err) {
            log.error("Redis Publish Message ({}) to Topic [{}] Failed: {}",
                    msg.getUuid(), msg.getTopic(), err.getMessage());
        }
        return null;
    }

    @Override
    public String publish(final String topic, final String message, final String operator) {
        BasicNodeMsg<String> msg = new BasicNodeMsg<>();
        msg.setSignature(StringUtils.defaultString(operator, HashUtil.md5(NetworkInfo.getMacAddress())));
        msg.setPriority(1);
        msg.setPayload(message);
        return publish(msg);
    }

    @Override
    public List<BriefTopicInfo> listRecentPubTopics(String signature) {
        List<BriefTopicInfo> topics = new ArrayList<>();
        for (Map.Entry<String, Set<BriefTopicInfo>> pubTopic : pubTopicCache.asMap().entrySet()) {
            if (pubTopic.getValue() == null || pubTopic.getValue().isEmpty()) {
                BriefTopicInfo topic = new BriefTopicInfo();
                topic.setTopic(pubTopic.getKey());
                topic.setOperator("");
                topics.add(topic);
            } else {
                for (BriefTopicInfo topic : pubTopic.getValue()) {
                    if (StringUtils.isBlank(signature)) {
                        topics.add(topic);
                    } else if (topic.getOperator().equalsIgnoreCase(signature)) {
                        topics.add(topic);
                    }
                }
            }
        }
        return topics;
    }

    @Override
    public TopicSubscription subscribe(String topic, PubSubMsgType msgType, final String operator) {
        TopicSubscription topicSubscription = buildDefaultSubscription(topic, msgType,
                StringUtils.defaultString(operator, HashUtil.md5(NetworkInfo.getMacAddress())));
        return subscribe(topicSubscription);
    }

    @Override
    public TopicSubscription subscribe(TopicSubscription topicSubscription) {
        TopicSubscription record;
        if (subTopicCache.asMap().containsKey(topicSubscription.getTopic())) {
            log.error("Topic [{}] is Still Subscribing...", topicSubscription.getTopic());
            throw new IllegalArgumentException("Topic is Already in Subscription");
        } else if (!topicSubscription.getNodeUuid().equalsIgnoreCase(nodeManagerConfig.getId())) {
            log.error("NodeManger UUID [{}] Does Not Match", topicSubscription.getNodeUuid());
            throw new IllegalArgumentException("Node Manager UUID Does Not Match");
        }
        if (StringUtils.isBlank(topicSubscription.getNodeUuid())) {
            topicSubscription.setNodeUuid(nodeManagerConfig.getId());
        }
        if (topicManager.exists(topicSubscription.getNodeUuid(), topicSubscription.getTopic())) {
            record = topicManager.updateSubscribeTopic(topicSubscription);
        } else {
            record = topicManager.addSubscribeTopic(topicSubscription);
        }
        RedisMsgListener msgListener = new RedisMsgListener(record.getId(), record.getPubSubMsgType(),
                this.msgProcessService);
        msgListener.setTopic(record.getTopic());
        try {
            Topic topic = record.getTopic().contains(ConstantParams.PATTERN_TOPIC_SYMBOL) ?
                    new PatternTopic(record.getTopic()) : new ChannelTopic(record.getTopic());
            redisMsgListenContainer.addMessageListener(msgListener, topic);
            subTopicCache.put(record.getTopic(), msgListener);
            return record;

        } catch (Exception err) {
            log.error("Subscribe to Topic [{}] with MessageType [{}] Failed: {}",
                    record.getTopic(), record.getPubSubMsgType(), err.getMessage());
            throw new TopicCrudException("Subscribe Topic Failed: " + err.getMessage());
        }
    }

    @Override
    public Boolean unsubscribe(String topic, PubSubMsgType msgType, String operator) {
        RedisMsgListener msgListener = subTopicCache.getIfPresent(topic);
        if (msgListener != null) {
            redisMsgListenContainer.removeMessageListener(msgListener);
            topicManager.deleteSubscribeTopic(msgListener.getId());
            subTopicCache.invalidate(topic);
            return true;
        }
        log.warn("Topic [{}] is not in cache for subscription", topic);
        TopicSubscription subscription = buildDefaultSubscription(topic, msgType,
                StringUtils.defaultString(operator, HashUtil.md5(NetworkInfo.getMacAddress())));
        return unsubscribe(subscription);
    }

    @Override
    public Boolean unsubscribe(TopicSubscription topicSubscription) {
        if (topicSubscription == null) {
            throw new IllegalArgumentException("Invalid TopicSubscription");
        } else if (StringUtils.isBlank(topicSubscription.getNodeUuid())) {
            throw new IllegalArgumentException("Invalid Node UUID");
        } else if (StringUtils.isBlank(topicSubscription.getTopic())) {
            throw new IllegalArgumentException("Invalid Topic");
        }
        TopicSubscription record = topicManager.getSubscribeTopic(topicSubscription.getNodeUuid(),
                topicSubscription.getTopic());
        if (record == null) {
            return false;
        } else {
            RedisMsgListener msgListener = subTopicCache.getIfPresent(topicSubscription.getTopic());
            if (msgListener == null) {
                return false;
            }
            Topic topic = record.getTopic().contains(ConstantParams.PATTERN_TOPIC_SYMBOL) ?
                    new PatternTopic(record.getTopic()) : new ChannelTopic(record.getTopic());
            redisMsgListenContainer.removeMessageListener(msgListener, topic);
            topicManager.deleteSubscribeTopic(msgListener.getId());
            subTopicCache.invalidate(topicSubscription.getTopic());
            return true;
        }
    }

    @Override
    public List<BriefTopicInfo> listRecentSubTopics(String nodeUuid) {
        List<BriefTopicInfo> topics = new ArrayList<>();
        if (StringUtils.isBlank(nodeUuid)) {
            for (Map.Entry<String, RedisMsgListener> subscriptions : subTopicCache.asMap().entrySet()) {
                BriefTopicInfo topic = new BriefTopicInfo();
                TopicSubscription subscription = topicManager.getSubscribeTopic(nodeManagerConfig.getId(),
                        subscriptions.getKey());
                String operator = subscription == null ? "System" : subscription.getOperator();
                topic.setTopic(subscriptions.getKey());
                topic.setOperator(operator);
                topic.setGmt(subscription == null ? null : subscription.getUpdateDate()
                        .atZone(ZoneId.systemDefault()).toInstant());
                topics.add(topic);
            }
            return topics;
        }
        for (Map.Entry<String, RedisMsgListener> subscriptions : subTopicCache.asMap().entrySet()) {
            BriefTopicInfo topic = new BriefTopicInfo();
            TopicSubscription subscription = topicManager.getSubscribeTopic(nodeUuid, subscriptions.getKey());
            if (subscription != null) {
                topic.setTopic(subscription.getTopic());
                topic.setOperator(subscription.getOperator());
                topic.setGmt(Instant.from(subscription.getUpdateDate()));
                topics.add(topic);
            }
        }
        return topics;
    }

    private TopicSubscription buildDefaultSubscription(String topic, PubSubMsgType msgType, String operator) {
        return TopicSubscription.builder()
                .topic(topic)
                .msgType(msgType.getType())
                .nodeUuid(nodeManagerConfig.getId())
                .operator(operator)
                .build();
    }

}
