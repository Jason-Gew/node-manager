package gew.nodemanager.common.service;

import gew.nodemanager.common.model.DistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Distributed Lock Service Common Implementation based on DistributedLockServiceRedisImpl
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class DistributedLockCommonImpl extends DistributedLockServiceRedisImpl implements Lock {

    private String lockKey;

    private String lockValue;

    private Duration lockDuration;

    private AtomicBoolean lockProcess;

    private AtomicBoolean unlockProcess;


    public DistributedLockCommonImpl(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
        this.lockProcess = new AtomicBoolean(false);
        this.unlockProcess = new AtomicBoolean(false);
    }

    public DistributedLockCommonImpl(RedisTemplate<String, Object> redisTemplate, String lockKey,
                                     Duration lockDuration) {
        super(redisTemplate);
        this.lockKey = lockKey;
        this.lockDuration = lockDuration;
        this.lockProcess = new AtomicBoolean(false);
        this.unlockProcess = new AtomicBoolean(false);
    }

    public DistributedLockCommonImpl(RedisTemplate<String, Object> redisTemplate, String lockKey, String lockValue) {
        super(redisTemplate);
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.lockProcess = new AtomicBoolean(false);
        this.unlockProcess = new AtomicBoolean(false);
    }

    @Override
    public void lock() {
        if (StringUtils.isBlank(this.lockKey)) {
            throw new IllegalArgumentException("Invalid Lock Key");
        }
        boolean check = this.lockProcess.compareAndSet(false, true);
        if (check) {
            throw new IllegalStateException("Lock is Still Processing");
        }
        DistributedLock lock = super.lock(this.lockKey, this.lockDuration);
        this.lockProcess.set(false);
        if (lock.isSuccess()) {
            this.lockValue = lock.getValue();
        } else {
            throw new IllegalStateException(lock.getMsg());
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (this.lockProcess.get()) {
            throw new InterruptedException("Lock is Still Processing");
        } else if (this.unlockProcess.get()) {
            throw new InterruptedException("Unlock is Still Processing");
        }
    }

    @Override
    public boolean tryLock() {
        throw new RuntimeException("Not support tryLock with no expiration time");
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (StringUtils.isBlank(this.lockKey)) {
            throw new IllegalArgumentException("Invalid Lock Key");
        } else if (time < 0 || unit == null) {
            throw new  IllegalArgumentException("Invalid Expire Time or Time Unit");
        }
        boolean check = this.lockProcess.compareAndSet(false, true);
        if (check) {
            throw new IllegalStateException("Lock is Still Processing");
        }
        DistributedLock lock = super.tryLock(this.lockKey, this.lockDuration, time, unit);
        this.lockProcess.set(false);
        if (lock.isSuccess()) {
            this.lockValue = lock.getValue();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void unlock() {
        if (StringUtils.isBlank(this.lockKey)) {
            throw new IllegalArgumentException("Invalid Lock Key");
        } else if (StringUtils.isBlank(this.lockValue)) {
            throw new IllegalArgumentException("Invalid Lock Value");
        }
        boolean check = this.unlockProcess.compareAndSet(false, true);
        if (check) {
            throw new IllegalStateException("Unlock is Still Processing");
        }
        DistributedLock unlock = super.unlock(this.lockKey, this.lockValue);
        this.unlockProcess.set(false);
        if (!unlock.isSuccess()) {
            throw new IllegalStateException(unlock.getMsg());
        }
    }

    @Override
    public Condition newCondition() {
        // Currently Not Support
        return null;
    }

    public String getLockKey() {
        return this.lockKey;
    }

    public String getLockValue() {
        return this.lockValue;
    }

    public Duration getLockDuration() {
        return this.lockDuration;
    }
}
