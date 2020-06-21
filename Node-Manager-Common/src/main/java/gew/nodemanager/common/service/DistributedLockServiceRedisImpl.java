package gew.nodemanager.common.service;

import gew.nodemanager.common.model.DistributedLock;
import gew.nodemanager.common.util.UidGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static gew.nodemanager.common.model.ConstantParams.DELETE_IF_EQUAL;
import static gew.nodemanager.common.model.ConstantParams.GET_AND_DELETE;
import static gew.nodemanager.common.model.ConstantParams.INTEGER_DT_PREFIX;

/**
 * Lock Service for Distributed System based on Redis
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class DistributedLockServiceRedisImpl implements DistributedLockService {

    private RedisTemplate<String, Object> redisTemplate;

    // Default 1 Second Retry Period
    private Long retryPeriod = 1L;

    // Default Stop Retry after 120 Seconds
    private Long maxRetryTimeout = 60L;

    private static RedisScript<String> GET_AND_DELETE_LUA = new DefaultRedisScript<>(GET_AND_DELETE, String.class);

    private static RedisScript<Boolean> DELETE_IF_EQUAL_LUA = new DefaultRedisScript<>(DELETE_IF_EQUAL, Boolean.class);



    // Constructor with RedisTemplate as Argument
    public DistributedLockServiceRedisImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public DistributedLock lock(final String key) {
        return lock(key, null);
    }

    @Override
    public DistributedLock lock(final String key, final Duration duration) {
        checkState(key);
        DistributedLock lock;
        try {
            Boolean result;
            String lockValue = generateLockValue(key);
            if (duration == null) {
                result = redisTemplate.opsForValue().setIfAbsent(key, lockValue);
            } else {
                if (TimeoutUtils.hasMillis(duration)) {
                    result =  redisTemplate.opsForValue().setIfAbsent(key, lockValue,
                            duration.toMillis(), TimeUnit.MILLISECONDS);
                } else {
                    result = redisTemplate.opsForValue().setIfAbsent(key, lockValue,
                            duration.getSeconds(), TimeUnit.SECONDS);
                }
            }
            if (result != null && result) {
                lock = new DistributedLock(true, DistributedLock.Action.LOCK, key,
                        "Lock Success");
                lock.setValue(lockValue);
            } else {
                lock = new DistributedLock(false, DistributedLock.Action.LOCK, key,
                        "Lock Failed: Lock Key Already Exist");
            }
        } catch (Exception err) {
            lock = new DistributedLock(false, DistributedLock.Action.LOCK, key,
                    "Lock Failed: " + err.getMessage());
        }
        lock.setTimestamp(Instant.now());
        return lock;
    }

    @Override
    public synchronized DistributedLock tryLock(String key, long timeout, TimeUnit timeUnit)
                        throws InterruptedException {
        return tryLock(key, null, timeout, timeUnit);
    }

    @Override
    public synchronized DistributedLock tryLock(String key, Duration duration, long timeout, TimeUnit timeUnit)
                        throws InterruptedException {
        checkState(key);
        if (timeout < 1 || timeUnit == null) {
            throw new IllegalArgumentException("Invalid Timeout Settings");
        }
        if (timeUnit.toSeconds(timeout) > maxRetryTimeout) {
            throw new IllegalArgumentException("Timeout Value Larger Than Max Retry Limitation");
        }
        long epoch = System.currentTimeMillis();
        while (timeUnit.toMillis(timeout) + epoch < System.currentTimeMillis()) {
            DistributedLock lock = lock(key, duration);
            if (lock.isSuccess()) {
                return lock;
            } else {
                TimeUnit.SECONDS.sleep(retryPeriod);
            }
        }
        return new DistributedLock(false, DistributedLock.Action.LOCK, key, "Try Lock Failed: Timeout");
    }

    @Override
    public DistributedLock unlock(String key) {
        checkState(key);
        DistributedLock lock;
        try {
            String result = redisTemplate.execute(GET_AND_DELETE_LUA, Collections.singletonList(key));
            if (StringUtils.isBlank(result)) {
                lock = new DistributedLock(false, DistributedLock.Action.UNLOCK, key,
                        "Unlock Failed: Lock Key Does Not Exist");
            } else {
                lock = new DistributedLock(true, DistributedLock.Action.UNLOCK, key, "Unlock Success");
                lock.setValue(result);
            }
        } catch (Exception err) {
            lock = new DistributedLock(false, DistributedLock.Action.UNLOCK, key,
                    "Unlock Failed: " + err.getMessage());
        }
        lock.setTimestamp(Instant.now());
        return lock;
    }

    @Override
    public DistributedLock unlock(String key, String value) {
        checkState(key, value);
        DistributedLock distributedLock;
        try {
            Boolean result = redisTemplate.hasKey(key);
            if (result == null || !result) {
                distributedLock = new DistributedLock(false, DistributedLock.Action.UNLOCK, key);
                distributedLock.setValue(value);
                distributedLock.setMsg("Unlock Failed: Lock Key Does Not Exist");
            } else {
                Boolean unlock = redisTemplate.execute(DELETE_IF_EQUAL_LUA, Collections.singletonList(key), value);
                if (unlock == null || !unlock) {
                    distributedLock = new DistributedLock(false, DistributedLock.Action.UNLOCK, key);
                    distributedLock.setValue(value);
                    distributedLock.setMsg("Unlock Failed: Lock Value Does Not Match");
                } else {
                    distributedLock = new DistributedLock(true, DistributedLock.Action.UNLOCK, key,
                            "Unlock Success");
                    distributedLock.setValue(value);
                }
            }
        } catch (Exception err) {
            distributedLock = new DistributedLock(false, DistributedLock.Action.UNLOCK, key);
            distributedLock.setValue(value);
            distributedLock.setMsg("Unlock Failed: " + err.getMessage());
        }
        distributedLock.setTimestamp(Instant.now());
        return distributedLock;
    }

    @Override
    public Boolean locked(String key) {
        checkState(key);
        return redisTemplate.hasKey(key);
    }

    private void checkState(String... values) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate is Null");
        }
        if (StringUtils.isAnyBlank(values)) {
            throw new IllegalArgumentException("Invalid Parameter(s)");
        }
    }

    private String generateLockValue(final String key) {
        String prefix = INTEGER_DT_PREFIX.format(LocalDate.now(ZoneId.of("UTC")));
        String uuid = UidGenerator.generate(prefix, 32);
        if (StringUtils.isNotBlank(key)) {
            uuid = uuid + "-" + key.hashCode();
        }
        return uuid;
    }

    public Long getRetryPeriod() {
        return retryPeriod;
    }

    public void setRetryPeriod(Long retryPeriod) {
        this.retryPeriod = retryPeriod;
    }

    public Long getMaxRetryTimeout() {
        return maxRetryTimeout;
    }

    public void setMaxRetryTimeout(Long maxRetryTimeout) {
        this.maxRetryTimeout = maxRetryTimeout;
    }
}
