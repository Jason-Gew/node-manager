package gew.nodemanager.common.service;

import gew.nodemanager.common.model.BasicNodeMsg;
import gew.nodemanager.common.model.PubSubMsgType;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
@Service
public class NodeMsgProcessServiceImpl implements NodeMsgProcessService {


    @Async
    @Override
    public Boolean process(String topic, BasicNodeMsg<String> nodeMsg) {
        log.info("Received NodeMsg from Topic [{}]: {}", topic, nodeMsg);
        return true;
    }

    @Async
    @Override
    public Boolean process(String topic, Object rawMsg, PubSubMsgType msgType) {
        log.info("Received RawMsg from Topic [{}], Type={}", topic, msgType);
        return true;
    }

    @Async
    @Override
    public Boolean process(String topic, Map<String, Object> jsonMsg) {
        log.info("Received JSON from Topic [{}]: {}", topic, jsonMsg.toString());
        return true;
    }
}
