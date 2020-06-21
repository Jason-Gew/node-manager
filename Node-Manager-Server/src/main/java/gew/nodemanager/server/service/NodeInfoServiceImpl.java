package gew.nodemanager.server.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import gew.nodemanager.common.model.AbstractNodeInfo;
import gew.nodemanager.common.model.BriefNodeInfo;
import gew.nodemanager.common.model.ConstantParams;
import gew.nodemanager.common.model.HeartBeatInfo;
import gew.nodemanager.common.model.NodeCrudException;
import gew.nodemanager.common.model.ServiceNodeInfo;
import gew.nodemanager.common.util.TimeIntervalHelper;
import gew.nodemanager.server.config.NodeManagerConfig;
import gew.nodemanager.server.repository.NodeInfoRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Jason/GeW
 * @since 2019-03-24
 */
@Log4j2
@Service
public class NodeInfoServiceImpl implements NodeInfoService, InitializingBean {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NodeInfoRepository repository;

    @Autowired
    private NodeManagerConfig config;

    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Override
    public void afterPropertiesSet() throws Exception {
        initNodeList();
    }

    private void initNodeList() {
        Boolean nodeListKeyExist = redisTemplate.hasKey(config.getNodeListRowKey());
        if (nodeListKeyExist == null || !nodeListKeyExist) {
            Map<String, String> managerUidAndHeartBeatKey = genUidAndHeartBeatKeyMap(config.getId(),
                    config.getHeartBeatRowKey());
            redisTemplate.opsForSet().add(config.getNodeListRowKey(), managerUidAndHeartBeatKey);
            log.info("Redis Cluster Does Not Have NodeListKey, System Set: {}", config.getNodeListRowKey());

        } else if (config.getNodeListTtl() != null && config.getNodeListTtl() > 1) {
            Boolean setTtl = redisTemplate.expire(config.getNodeListRowKey(), config.getNodeListTtl().longValue(),
                    ConstantParams.DEFAULT_HEARTBEAT_TIME_UNIT);
            log.info("NodeListKey TTL [{} {}] Has Been Configured {}", config.getNodeListTtl(),
                    ConstantParams.DEFAULT_HEARTBEAT_TIME_UNIT, setTtl);
        }
    }

    private void checkNodeInfo(AbstractNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            throw new IllegalArgumentException("NodeInfo is Null");
        } else if (StringUtils.isBlank(nodeInfo.getUuid())) {
            throw new NodeCrudException("Invalid NodeInfo UUID");
        } else if (StringUtils.isBlank(nodeInfo.getName())) {
            throw new NodeCrudException("Invalid NodeInfo Name");
        }
    }

    @Override
    public Optional<ServiceNodeInfo> getNodeInfoByUuid(String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            return Optional.empty();
        }
        return repository.findById(uuid);
    }

    @Override
    public List<ServiceNodeInfo> getNodeInfoByName(String name) {
        return null;
    }

    @Override
    @Transactional
    public String addOrUpdateNode(final ServiceNodeInfo node, boolean add) {
        checkNodeInfo(node);
        boolean exist = existNode(node.getUuid());
        if (add && exist) {
            throw new NodeCrudException("Node Already Exist");
        } else if (!add && !exist) {
            throw new NodeCrudException("Node Does Not Exist");
        } else if (node.getHeartBeatInfo() != null && StringUtils.isNotBlank(node.getHeartBeatInfo().getKey())) {
            ServiceNodeInfo nodeInfo;
            try {
                ServiceNodeInfo nodeCopy = (ServiceNodeInfo) node.clone();
                nodeCopy.getHeartBeatInfo().setInfo(null);
                nodeInfo = nodeCopy;

            } catch (CloneNotSupportedException err) {
                log.warn("ServiceNodeInfo [name={}, uuid={}] Clone Failed: {}, System Store Original",
                        node.getName(), node.getUuid(), err.getMessage());
                nodeInfo = node;
            }
            Long addToNodeList = appendToNodeList(nodeInfo, nodeInfo.getHeartBeatInfo());
            if (addToNodeList < 1) {
                removeNode(nodeInfo.getUuid());
                throw new NodeCrudException("Duplicate HeartBeat Key");
            } else {
                repository.save(nodeInfo);
                log.info("Save Add Node [name={}, uuid={}, ip={}, heartBeatKey={}] And Add to List Success",
                        nodeInfo.getName(), nodeInfo.getUuid(), nodeInfo.getIp(), nodeInfo.getHeartBeatInfo().getKey());
            }
            return nodeInfo.getUuid();
        } else {
            ServiceNodeInfo nodeInfo = repository.save(node);
            log.info("Save Add Node [name={}, uuid={}, ip={}] Success",
                    nodeInfo.getName(), nodeInfo.getUuid(), nodeInfo.getIp());
            return nodeInfo.getUuid();
        }
    }

    private Map<String, String> genUidAndHeartBeatKeyMap(final String uuid, final String heartBeatKey) {
        Map<String, String> map = new HashMap<>();
        map.put(ConstantParams.UUID_KEY, uuid);
        map.put(ConstantParams.HEARTBEAT_KEY, heartBeatKey);
        return map;
    }


    @Override
    public String refreshHeartBeat(AbstractNodeInfo node, HeartBeatInfo heartBeatInfo) {
        checkNodeInfo(node);
        if (!existNode(node.getUuid())) {
            throw new NodeCrudException("Node Does Not Exist");
        }
        if (heartBeatInfo == null) {
            throw new IllegalArgumentException("Invalid HeartBeatInfo");
        } else if (StringUtils.isBlank(heartBeatInfo.getKey())) {
            String heartBeatKey = String.join(node.getName(), ConstantParams.REDIS_KEY_PREFIX_DELIMITER,
                    node.getUuid());
            log.warn("Empty HeartBeatInfo RowKey for Node [name={}, uuid={}], System Use Combo Value: {}",
                    node.getName(), node.getUuid(), heartBeatKey);
            heartBeatInfo.setKey(heartBeatKey);
        } else if (!heartBeatInfo.isValid()) {
            log.warn("Invalid HeartBeatInfo TTL/TimeUnit for Node [name={}, uuid={}], System Use Combo Value: {} {}",
                    node.getName(), node.getUuid(), ConstantParams.DEFAULT_HEARTBEAT_TTL,
                    ConstantParams.DEFAULT_HEARTBEAT_TIME_UNIT);
            heartBeatInfo.setTtl(ConstantParams.DEFAULT_HEARTBEAT_TTL.longValue());
            heartBeatInfo.setTimeUnit(ConstantParams.DEFAULT_HEARTBEAT_TIME_UNIT.toString());
        }
        try {
            Boolean rowKeyExist = redisTemplate.hasKey(heartBeatInfo.getKey());
            if (rowKeyExist == null || !rowKeyExist) {
                log.info("Set New HeartBeat Info for Node [name={}, uuid={}] on HeartBeatKey={}",
                        node.getName(), node.getUuid(), heartBeatInfo.getKey());
            }
            BriefNodeInfo briefNodeInfo = new BriefNodeInfo();
            BeanUtils.copyProperties(node, briefNodeInfo);
            briefNodeInfo.addExtraInfo(heartBeatInfo.getInfo());
            redisTemplate.opsForValue().set(heartBeatInfo.getKey(), briefNodeInfo, heartBeatInfo.getTtl(),
                    TimeIntervalHelper.toTimeUnit(heartBeatInfo.getTimeUnit()));
            return heartBeatInfo.getKey();

        } catch (Exception err) {
            log.error("Refresh HeartBeatInfo [key={}] for Node [name={}, uuid={}] Failed: {}",
                    heartBeatInfo.getKey(), node.getName(), node.getUuid(), err.getMessage());
            throw new NodeCrudException("Refresh HeartBeat Failed: " + err.getMessage());
        }
    }

    @Override
    @Transactional
    public Long appendToNodeList(AbstractNodeInfo node, HeartBeatInfo heartBeatInfo) {
        checkNodeInfo(node);
        if (heartBeatInfo == null || StringUtils.isBlank(heartBeatInfo.getKey())) {
            throw new NodeCrudException("Invalid HeartBeat Key");
        }
        initNodeList();
        Map<String, String> uidAndHeartBeatMap = genUidAndHeartBeatKeyMap(node.getUuid(), heartBeatInfo.getKey());
        Cursor<Object> cursor = redisTemplate.opsForSet().scan(config.getNodeListRowKey(), ScanOptions.NONE);
        while (cursor.hasNext()) {
            Object object = cursor.next();
            Map<String, Object> map;
            try {
                if (object instanceof Map) {
                    map = (Map) object;
                } else if (object instanceof String) {
                    JSONObject jsonObject = new JSONObject(object);
                    map = jsonObject.toMap();
                } else {
                    JSONObject jsonObject = new JSONObject(object);
                    map = jsonObject.toMap();
                }

                boolean correctKeys = map.containsKey(ConstantParams.UUID_KEY)
                        && map.containsKey(ConstantParams.HEARTBEAT_KEY);
                if (!correctKeys) {
                    log.warn("NodeList [{}] Element Does Not Contain Correct Keys: {}",
                            config.getNodeListRowKey(), object);
                    continue;
                }
                if (heartBeatInfo.getKey().equalsIgnoreCase((String) map.get(ConstantParams.HEARTBEAT_KEY))) {
                    cursor.close();
                    if (node.getUuid().equalsIgnoreCase((String) map.get(ConstantParams.UUID_KEY))) {
                        log.info("Node [{}] Combo Already Exists", uidAndHeartBeatMap);
                        return 1L;
                    } else {
                        log.error("Node [{}] Uses the Same heartBeat Key of Node [uuid={}]",
                                uidAndHeartBeatMap, map.get(ConstantParams.UUID_KEY));
                        return -1L;
                    }
                }
            } catch (Exception err) {
                log.warn("Process Node HeartBeat Key Verification Error: {}", err.getMessage());
            }
        }
        try {
            cursor.close();
        } catch (IOException ioe) {
            log.warn("Close Redis Cursor for Scanning NodeList Failed: {}", ioe.getMessage());
        }
        Long seq = redisTemplate.opsForSet().add(config.getNodeListRowKey(), uidAndHeartBeatMap);
        log.info("Add Node [uuid={}, hearBeatKey={}] to NodeList : {}", node.getUuid(), heartBeatInfo.getKey(),
                config.getNodeListRowKey());
        return seq;
    }

    @Override
    public boolean removeFromNodeList(AbstractNodeInfo node, HeartBeatInfo heartBeatInfo) {
        checkNodeInfo(node);
        if (heartBeatInfo == null || StringUtils.isBlank(heartBeatInfo.getKey())) {
            return false;
        }
        Boolean nodeListKeyExist = redisTemplate.hasKey(config.getNodeListRowKey());
        if (nodeListKeyExist == null || !nodeListKeyExist) {
            return true;
        }
        Map<String, String> uidAndHeartBeatMap = genUidAndHeartBeatKeyMap(node.getUuid(), heartBeatInfo.getKey());
        Long seq = redisTemplate.opsForSet().remove(config.getNodeListRowKey(), uidAndHeartBeatMap);
        return seq != null && seq > 0;
    }


    @Override
    public boolean existNode(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return false;
        }
        return repository.existsById(uuid);
    }

    @Override
    @Transactional
    public boolean removeNode(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return false;
        }
        if (existNode(uuid)) {
            Optional<ServiceNodeInfo> serviceNodeInfo = getNodeInfoByUuid(uuid);
            if (serviceNodeInfo.isPresent() && serviceNodeInfo.get().getHeartBeatInfo() != null) {
                boolean remove = removeFromNodeList(serviceNodeInfo.get(), serviceNodeInfo.get().getHeartBeatInfo());
                log.info("Remove Node [uuid={}, heartBeatKey={}] From NodeList: {}", uuid,
                        serviceNodeInfo.get().getHeartBeatInfo().getKey(), remove ? "Success" : "Failed");
            }
            repository.deleteById(uuid);
            return true;

        } else {
            return false;
        }
    }


    @Override
    public Page<ServiceNodeInfo> listNodeInfo(Pageable pageable) {
        if (pageable == null) {
            log.warn("Invalid PageRequest, System Use Set Start Page:1, Size:{}",ConstantParams.DEFAULT_PAGE_SIZE);
            pageable = PageRequest.of(0, ConstantParams.DEFAULT_PAGE_SIZE);
        }
        Page<ServiceNodeInfo> page = repository.findAll(pageable);
        log.info("List All Nodes Found {} on Page: {}", page.getNumber(), pageable.getPageNumber());
        return page;
    }

    @Override
    public List<BriefNodeInfo> listBriefNodeInfo(Pageable pageable) {
        return listNodeInfo(pageable).stream()
                .map(ServiceNodeInfo::toBriefNodeInfo)
                .collect(Collectors.toList());
    }


    @Override
    public Integer countNodesByName(String name) {
        return null;
    }

    @Override
    public Integer countAllNodes() {
        if (StringUtils.isBlank(config.getNodeListRowKey())) {
            throw new NodeCrudException("Invalid Node List Row Key");
        }
        Boolean hasKey = redisTemplate.hasKey(config.getNodeListRowKey());
        if (hasKey == null || !hasKey) {
            return 0;
        } else {
            Long members = redisTemplate.boundSetOps(config.getNodeListRowKey()).size();
            if (members == null || members == 0) {
                return 0;
            } else {
                return members.intValue();
            }
        }
    }
}
