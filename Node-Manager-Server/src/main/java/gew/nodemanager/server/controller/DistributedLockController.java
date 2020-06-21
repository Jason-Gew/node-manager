package gew.nodemanager.server.controller;

import gew.nodemanager.common.model.CommonResponse;
import gew.nodemanager.common.model.DistributedLock;
import gew.nodemanager.common.model.Status;
import gew.nodemanager.common.service.DistributedLockService;
import gew.nodemanager.common.service.DistributedLockServiceRedisImpl;
import gew.nodemanager.common.util.TimeIntervalHelper;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static gew.nodemanager.common.model.ConstantParams.DEFAULT_JSON_TYPE;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
@RestController
@RequestMapping("/distributed-lock")
@Api(value="distributed-lock", tags="Distributed Lock Service")
public class DistributedLockController implements InitializingBean {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private DistributedLockService distributedLockService;

    private static final Integer MAX_LOCK_KEY_LENGTH = 2048;

    @Override
    public void afterPropertiesSet() throws Exception {
        distributedLockService = new DistributedLockServiceRedisImpl(redisTemplate);
        log.info("Initializing Distributed Lock Service Based on Redis");
    }


    @PostMapping(value = "/lock", produces = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<DistributedLock>> lock(@RequestParam final String lockKey,
                                                                @RequestParam(required = false) Long duration,
                                                                @RequestParam(required = false, defaultValue = "sec")
                                                                              String timeUnit,
                                                                HttpServletRequest httpRequest) {
        log.info("Received Distributed-Lock Request From [{}]: Key={} Duration={}-{}",
                httpRequest.getRemoteAddr(), lockKey, duration, timeUnit);
        HttpStatus httpStatus;
        CommonResponse<DistributedLock> response;
        if (StringUtils.isBlank(lockKey)) {
            response = new CommonResponse<>(4101, Status.FAIL, "Invalid Lock Key");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (lockKey.length() > MAX_LOCK_KEY_LENGTH) {
            response = new CommonResponse<>(4102, Status.FAIL,
                    "Lock Key Length is too Large: Max=" + MAX_LOCK_KEY_LENGTH);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            TemporalUnit unit;

            if (StringUtils.isNotBlank(timeUnit)) {
                unit = TimeIntervalHelper.toTemporalUnit(timeUnit);
            } else {
                unit = ChronoUnit.SECONDS;
            }
            if (duration != null && duration > 0 && unit != null) {
                DistributedLock distributedLock = distributedLockService.lock(lockKey, Duration.of(duration, unit));
                response = new CommonResponse<>(200, distributedLock.isSuccess() ? Status.SUCCESS : Status.FAIL,
                        "Operation Proceed", distributedLock);
                httpStatus = HttpStatus.OK;

            } else if ((duration != null && duration < 1) || unit == null) {
                response = new CommonResponse<>(4103, Status.FAIL, "Invalid Duration");
                httpStatus = HttpStatus.BAD_REQUEST;

            } else {
                DistributedLock distributedLock = distributedLockService.lock(lockKey);
                response = new CommonResponse<>(200, distributedLock.isSuccess() ? Status.SUCCESS : Status.FAIL,
                        "Operation Proceed", distributedLock);
                httpStatus = HttpStatus.OK;
            }
        } catch (Exception err) {
            log.error("Set Distributed Lock on [{}] Failed: {}", lockKey, err.getMessage());
            response = new CommonResponse<>(4104, Status.FAIL, "Lock Key Failed: " + err.getMessage());
            httpStatus = HttpStatus.OK;
        }
        return new ResponseEntity<>(response, httpStatus);
    }


    @PostMapping(value = "/unlock", produces = DEFAULT_JSON_TYPE)
    public ResponseEntity<CommonResponse<DistributedLock>> unlock(@RequestParam final String lockKey,
                                                                  @RequestParam final String lockValue,
                                                                  HttpServletRequest httpRequest) {
        log.info("Received Distributed-Lock Request From [{}]: Key={} Value={}",
                httpRequest.getRemoteAddr(), lockKey, lockValue);
        HttpStatus httpStatus;
        CommonResponse<DistributedLock> response;
        if (StringUtils.isBlank(lockKey)) {
            response = new CommonResponse<>(4101, Status.FAIL, "Invalid Lock Key");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (lockKey.length() > MAX_LOCK_KEY_LENGTH) {
            response = new CommonResponse<>(4102, Status.FAIL,
                    "Lock Key Length is too Large: Max=" + MAX_LOCK_KEY_LENGTH);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            DistributedLock unlock = distributedLockService.unlock(lockKey, lockValue);
            response = new CommonResponse<>(200, unlock.isSuccess() ? Status.SUCCESS : Status.FAIL,
                    "Operation Proceed", unlock);
            httpStatus = HttpStatus.OK;
        } catch (Exception err) {
            log.error("Release Distributed Lock on Key=[{}] with Value=[{}] Failed: {}",
                    lockKey, lockValue, err.getMessage());
            response = new CommonResponse<>(4106, Status.FAIL, "Unlock Key Failed: " + err.getMessage());
            httpStatus = HttpStatus.OK;
        }
        return new ResponseEntity<>(response, httpStatus);
    }
}
