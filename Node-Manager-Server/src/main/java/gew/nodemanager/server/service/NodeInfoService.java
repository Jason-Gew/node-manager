package gew.nodemanager.server.service;

import gew.nodemanager.common.model.AbstractNodeInfo;
import gew.nodemanager.common.model.BriefNodeInfo;
import gew.nodemanager.common.model.HeartBeatInfo;
import gew.nodemanager.common.model.ServiceNodeInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Distributed Service Node CRUD Service based on RedisTemplate and Spring Data Redis.
 * @author Jason/GeW
 * @since 2019-03-24
 */
public interface NodeInfoService {

    Optional<ServiceNodeInfo> getNodeInfoByUuid(final String uuid);

    List<ServiceNodeInfo> getNodeInfoByName(final String name);

    String addOrUpdateNode(ServiceNodeInfo node, boolean add);

    String refreshHeartBeat(AbstractNodeInfo node, HeartBeatInfo heartBeatInfo);

    Long appendToNodeList(AbstractNodeInfo node, HeartBeatInfo heartBeatInfo);

    boolean removeFromNodeList(AbstractNodeInfo node, HeartBeatInfo heartBeatInfo);

    boolean existNode(final String uuid);

    boolean removeNode(final String uuid);

    Page<ServiceNodeInfo> listNodeInfo(final Pageable pageable);

    List<BriefNodeInfo> listBriefNodeInfo(final Pageable pageable);

    Integer countNodesByName(final String name);

    Integer countAllNodes();
}
