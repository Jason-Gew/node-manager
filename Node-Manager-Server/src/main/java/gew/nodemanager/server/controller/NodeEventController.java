package gew.nodemanager.server.controller;

import gew.nodemanager.common.entity.TopicSubscription;
import gew.nodemanager.common.model.BasicNodeMsg;
import gew.nodemanager.common.model.BriefTopicInfo;
import gew.nodemanager.common.model.CommonResponse;
import gew.nodemanager.common.model.Status;
import gew.nodemanager.server.service.TopicManager;
import gew.nodemanager.server.service.NodePubSubService;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

import static gew.nodemanager.common.model.ConstantParams.DEFAULT_JSON_TYPE;


/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
@RestController
@RequestMapping("/event")
@Api(value="event", tags="Node Event")
public class NodeEventController {

    @Autowired
    private NodePubSubService nodePubSubService;

    @Autowired
    private TopicManager topicManager;


    @SuppressWarnings("rawtypes")
    private Optional<String> validate(final BasicNodeMsg msg) {
        if (msg == null) {
            return Optional.of("NodeMsg is null");
        } else if (StringUtils.isBlank(msg.getSignature())) {
            return Optional.of("Invalid Message Signature");
        } else if (StringUtils.isBlank(msg.getUuid())) {
            return Optional.of("Invalid Message UUID");
        } else if (msg.getPriority() < 0 || msg.getPriority() > 10) {
            return Optional.of("Message Priority is Out of Range [0, 10]");
        } else if (!StringUtils.contains(msg.getTopic(), "/")) {
            return Optional.of("Topic is Invalid or Without Levels");
        } else if (msg.getPayload() == null) {
            return Optional.of("Empty Payload");
        } else {
            return Optional.empty();
        }
    }

    @PostMapping(value = "/publish", produces = DEFAULT_JSON_TYPE, consumes = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<String>> regularMsgPublish(@RequestBody BasicNodeMsg<String> nodeMsg,
                                                                    HttpServletRequest httpRequest) {
        log.info("-> Received Regular Msg [UUID={}] by Signature [{}] Publish to Topic [{}], From {}",
                nodeMsg.getUuid(), nodeMsg.getSignature(), nodeMsg.getTopic(), httpRequest.getRemoteAddr());

        CommonResponse<String> response;
        Optional<String> validation = validate(nodeMsg);
        if (validation.isPresent()) {
            response = new CommonResponse<>(400, Status.FAIL, validation.get());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            String uuid = nodePubSubService.publish(nodeMsg);
            if (uuid == null) {
                log.info("Msg [UUID={}] by Signature [{}] Publish on Topic [{}] Failed!",
                        nodeMsg.getUuid(), nodeMsg.getSignature(), nodeMsg.getTopic());
                response = new CommonResponse<>(4200, Status.FAIL, "Message Publish Failed");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            log.info("Msg [UUID={}] by Signature [{}] Publish on Topic [{}] Success",
                    uuid, nodeMsg.getSignature(), nodeMsg.getTopic());
            response = new CommonResponse<>(200, Status.SUCCESS,
                    "Message Publish Success", nodeMsg.getUuid());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception err) {
            log.error("Msg [UUID={}] by Signature [{}] Publish on Topic [{}] Failed: {}",
                    nodeMsg.getUuid(), nodeMsg.getSignature(), nodeMsg.getTopic(), err.getMessage());
            response = new CommonResponse<>(4202, Status.FAIL,
                    "Message Publish Failed: " + err.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    @PostMapping(value = "/subscribe", produces = DEFAULT_JSON_TYPE, consumes = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<TopicSubscription>> subscribe(@RequestBody TopicSubscription subscription,
                                                                       HttpServletRequest httpRequest) {
        log.info("-> Received Subscribe Topic [NodeUUID={}, Topic={}] by Operator [{}] From {}",
                subscription.getNodeUuid(), subscription.getTopic(), subscription.getOperator(),
                httpRequest.getRemoteAddr());

        CommonResponse<TopicSubscription> response;
        try {
            TopicSubscription result = nodePubSubService.subscribe(subscription);
            log.info("Subscribe Topic [{}] By {} Success", result.getTopic(), result.getOperator());
            response = new CommonResponse<>(200, Status.SUCCESS, "Subscribe Success", result);

        } catch (Exception err) {
            log.error("Subscribe Topic [{}] By {} Failed: {}", subscription.getTopic(), subscription.getOperator(),
                    err.getMessage());
            response = new CommonResponse<>(4210, Status.FAIL, "Subscribe Failed: " + err.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/unsubscribe", produces = DEFAULT_JSON_TYPE, consumes = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<String>> unsubscribe(@RequestBody TopicSubscription subscription,
                                                              HttpServletRequest httpRequest) {
        log.info("-> Received Unsubscribe Topic [NodeUUID={}, Topic={}] by Operator [{}] From {}",
                subscription.getNodeUuid(), subscription.getTopic(), subscription.getOperator(),
                httpRequest.getRemoteAddr());

        CommonResponse<String> response;
        try {
            Boolean result = nodePubSubService.unsubscribe(subscription);
            if (result) {
                log.info("Unsubscribe Topic [{}] By {} Success", subscription.getTopic(), subscription.getOperator());
                response = new CommonResponse<>(200, Status.SUCCESS, "Unsubscribe Topic Success",
                        subscription.getTopic());
            } else {
                log.info("Unsubscribe Topic [{}] By {} Failed", subscription.getTopic(), subscription.getOperator());
                response = new CommonResponse<>(4212, Status.FAIL, "Unsubscribe Failed: Check Topic Info");
            }

        } catch (Exception err) {
            log.error("Unsubscribe Topic [{}] By {} Failed: {}", subscription.getTopic(),
                    subscription.getOperator(), err.getMessage());
            response = new CommonResponse<>(4214, Status.FAIL, "Unsubscribe Failed: " + err.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping(value = "/topic-list/{type}", produces = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<List<BriefTopicInfo>>> listTopics(@PathVariable(name = "type") String type,
                                                                           @RequestParam(required = false) String nodeUuid,
                                                                           HttpServletRequest httpRequest) {
        CommonResponse<List<BriefTopicInfo>> response;
        try {
            if (parseTopicType(type.trim()).equals("publish")) {
                List<BriefTopicInfo> topics = nodePubSubService.listRecentPubTopics(nodeUuid);
                response = new CommonResponse<>(200, Status.SUCCESS, "List Recent Publish Topic Success",
                        topics);
            } else {
                List<BriefTopicInfo> topics = nodePubSubService.listRecentSubTopics(nodeUuid);
                response = new CommonResponse<>(200, Status.SUCCESS, "List Recent Subscribe Topic Success",
                        topics);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception err) {
            log.error("List Topic type={} Failed: {}", type, err.getMessage());
            response = new CommonResponse<>(4220, Status.FAIL, "List Topic Failed: " + err.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    private String parseTopicType(final String type) {
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("Invalid Topic Type");
        }
        switch (type.toLowerCase()) {
            case "pub":
            case "publish":
            case "published":
            case "publishing":
                return "publish";

            case "sub":
            case "subscribe":
            case "subscribed":
            case "subscribing":
            case "subscription":
                return "subscribe";

            default:
                throw new IllegalArgumentException("Unrecognized Topic Type");
        }
    }
}
