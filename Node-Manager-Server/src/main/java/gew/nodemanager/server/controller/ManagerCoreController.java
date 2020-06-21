package gew.nodemanager.server.controller;

import gew.nodemanager.common.model.AbstractNodeInfo;
import gew.nodemanager.common.model.CommonResponse;
import gew.nodemanager.common.model.ConstantParams;
import gew.nodemanager.common.model.Pagination;
import gew.nodemanager.common.model.ServiceNodeInfo;
import gew.nodemanager.common.model.Status;
import gew.nodemanager.server.service.NodeInfoService;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/manager")
@Api(value="manager", tags="Node Manager")
public class ManagerCoreController {

    @Autowired
    private NodeInfoService nodeInfoService;


    private ResponseEntity<CommonResponse<ServiceNodeInfo>> createOrUpdateNodeInfo(ServiceNodeInfo nodeInfo,
                                                                                   boolean create) {
        CommonResponse<ServiceNodeInfo> response;
        if (StringUtils.isBlank(nodeInfo.getName())) {
            response = new CommonResponse<>(4001, Status.FAIL, "Invalid Node Name");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (StringUtils.isBlank(nodeInfo.getUuid())) {
            response = new CommonResponse<>(4002, Status.FAIL, "Invalid Node UUID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            String uuid = nodeInfoService.addOrUpdateNode(nodeInfo, create);
            log.info("{} Node [Name={}, UUID={}] Success", create ? "Register" : "Update",
                    nodeInfo.getName(), uuid);
            if (nodeInfo.getHeartBeatInfo() != null && nodeInfo.getHeartBeatInfo().getTtl() != null
                    && StringUtils.isNotBlank(nodeInfo.getHeartBeatInfo().getKey())) {
                String heartBeatKey = nodeInfoService.refreshHeartBeat(nodeInfo, nodeInfo.getHeartBeatInfo());
                log.info("{} Node [Name={}, UUID={}] And Refresh HeartBeat [{}] Success",
                        create ? "Register" : "Update", nodeInfo.getName(), uuid, heartBeatKey);
            }
            response = new CommonResponse<>(200, Status.SUCCESS,
                    String.format("%s Node and Refresh Heart Beat Success", create ? "Register" : "Update"),
                    nodeInfo);
        } catch (Exception err) {
            log.error("{} Node [{}] Failed: {}",  create ? "Register" : "Update",
                    nodeInfo.getUuid(), err.getMessage());
            response = new CommonResponse<>(4006, Status.FAIL,
                    String.format("%s Node and Refresh Heart Beat Failed: ", create ? "Register" : "Update") +
                            err.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/register.json", produces = DEFAULT_JSON_TYPE, consumes = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<ServiceNodeInfo>> register(@RequestBody ServiceNodeInfo nodeInfo,
                                                                    HttpServletRequest httpRequest) {
        log.info("Received Register Node Request From [{}]: {}", httpRequest.getRemoteAddr(), nodeInfo.toString());

        return createOrUpdateNodeInfo(nodeInfo, true);
    }


    @PutMapping(value = "/update.json", produces = DEFAULT_JSON_TYPE, consumes = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<ServiceNodeInfo>> update(@RequestBody ServiceNodeInfo nodeInfo,
                                                                  HttpServletRequest httpRequest) {
        log.info("Received Update Node Request From [{}]: {}", httpRequest.getRemoteAddr(), nodeInfo.toString());

        return createOrUpdateNodeInfo(nodeInfo, false);
    }


    @GetMapping(value = "/listNodes.json", produces = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<List<ServiceNodeInfo>>> listNodes(HttpServletRequest httpRequest,
            @RequestParam(required = false, defaultValue = "0")    Integer pageNumber,
            @RequestParam(required = false, defaultValue = "20")   Integer pageSize) {
        log.info("Received List Nodes Request From [{}]: Page={}, Size={}", httpRequest.getRemoteAddr(),
                pageNumber, pageSize);
        CommonResponse<List<ServiceNodeInfo>> response;
        if (pageSize < 0 || pageSize > ConstantParams.DEFAULT_PAGE_SIZE) {
            pageSize = ConstantParams.DEFAULT_PAGE_SIZE;
            log.warn("Request Page Size is too Large, System Set to Default: {}", pageSize);
        } else if (pageNumber < 0) {
            pageNumber = 0;
            log.warn("Invalid Page Number, System Set to Default Start Page 1");
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.ASC, "upTime");
        try {
            Page<ServiceNodeInfo> nodes = nodeInfoService.listNodeInfo(pageable);
            response = new CommonResponse<>(200, Status.SUCCESS,
                    "List Service Nodes Info Success", nodes.toList());
            Pagination pagination = new Pagination(pageNumber, pageSize, Pagination.SortOrder.ASC, "upTime");
            pagination.setTotal(nodes.getTotalElements());

            response.setPagination(pagination);
        } catch (Exception err) {
            log.error("List Nodes Failed: {}", err.getMessage());
            response = new CommonResponse<>(4010, Status.FAIL, "List Nodes Failed: " + err.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/node.json", produces = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<AbstractNodeInfo>> getNodeInfo(@RequestParam final String uuid,
                                                                        HttpServletRequest httpRequest) {
        log.info("Received Get Node Info Request From [{}]: uuid={}", httpRequest.getRemoteAddr(), uuid);
        CommonResponse<AbstractNodeInfo> response;
        if (StringUtils.isBlank(uuid)) {
            response = new CommonResponse<>(4002, Status.FAIL, "Invalid Node UUID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            Optional<ServiceNodeInfo> nodeInfo = nodeInfoService.getNodeInfoByUuid(uuid);
            response = nodeInfo.<CommonResponse<AbstractNodeInfo>>map(CommonResponse::new)
                    .orElseGet(() -> new CommonResponse<>(4003, Status.FAIL, "Node Does Not Exist"));
        } catch (Exception err) {
            log.error("Get Node Info By uuid={} Failed: {}", uuid, err.getMessage());
            response = new CommonResponse<>(4004, Status.FAIL, "Get Node Info Failed: " + err.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "delete.json", produces = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<AbstractNodeInfo>> deleteNode(@RequestParam final String uuid,
                                                                       @RequestParam final String name,
                                                                       HttpServletRequest httpRequest) {
        log.info("Received Get Node Info Request From [{}]: uuid={}, name={}",
                httpRequest.getRemoteAddr(), uuid, name);

        CommonResponse<AbstractNodeInfo> response;
        if (StringUtils.isAnyBlank(uuid, name)) {
            response = new CommonResponse<>(4000, Status.FAIL, "Invalid Request Parameter(s)");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            Optional<ServiceNodeInfo> nodeInfo = nodeInfoService.getNodeInfoByUuid(uuid);
            if (nodeInfo.isPresent()) {
                if (nodeInfo.get().getName().equalsIgnoreCase(name)) {
                    boolean status = nodeInfoService.removeNode(uuid);
                    log.info("Remove Node [uuid={}, name={}] Request Has Been Processed: {}", uuid, name,
                            status ? "Success" : "Failed");
                    response = new CommonResponse<>(nodeInfo.get().toBriefNodeInfo());
                } else {
                    response = new CommonResponse<>(4030, Status.FAIL, "Node Name Does Not Match");
                }
            } else {
                response = new CommonResponse<>(4003, Status.FAIL, "Node Does Not Exist");
            }
        } catch (Exception err) {
            log.error("Remove Node [uuid={}, name={}] Exception: {}", uuid, name, err.getMessage());
            response = new CommonResponse<>(4031, Status.FAIL, "Remove Node Failed: " + err.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
