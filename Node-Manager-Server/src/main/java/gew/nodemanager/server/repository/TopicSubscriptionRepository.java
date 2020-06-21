package gew.nodemanager.server.repository;

import gew.nodemanager.common.entity.TopicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Repository
public interface TopicSubscriptionRepository extends JpaRepository<TopicSubscription, Long> {

    List<TopicSubscription> findTopicByNodeUuid(final String nodeUuid);

    TopicSubscription findTopicByNodeUuidAndTopic(final String nodeUuid, final String topic);

    boolean existsByNodeUuidAndTopic(final String nodeUuid, final String topic);

    long countByNodeUuid(final String nodeUuid);

    long countByTopic(final String topic);

}
