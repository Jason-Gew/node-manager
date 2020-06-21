package gew.nodemanager.common.service;

import gew.nodemanager.common.model.DistributedLock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public interface DistributedLockService {

    /**
     * Lock a key and get lock value if success
     * @param key   lock key
     * @return      DistributedLock Details
     */
    DistributedLock lock(final String key);

    /**
     * Lock a key, set valid duration and get lock value if success
     * @param key       lock key
     * @param duration  lock key valid duration
     * @return          DistributedLock Details
     */
    DistributedLock lock(final String key, final Duration duration);

    /**
     * Try to lock a key within certain timeout and get lock value if success
     * @param key       lock key
     * @param timeout   try lock timeout value
     * @param timeUnit  try lock timeout unit
     * @return          DistributedLock Details
     * @throws InterruptedException
     */
    DistributedLock tryLock(final String key, final long timeout, final TimeUnit timeUnit)
            throws InterruptedException;

    /**
     * Try to lock a key within certain timeout, set duration and get lock value if success
     * @param key           lock key
     * @param duration      lock key valid duration
     * @param timeout       try lock timeout value
     * @param timeUnit      try lock timeout unit
     * @return              DistributedLock Details
     * @throws InterruptedException
     */
    DistributedLock tryLock(final String key, final Duration duration, final long timeout, final TimeUnit timeUnit)
            throws InterruptedException;

    /**
     * Force to unlock key without checking lock value
     * @param key   lock key
     * @return      DistributedLock Details
     */
    DistributedLock unlock(final String key);

    /**
     * Check lock value and then unlock key
     * @param key   lock key
     * @param value lock value
     * @return      DistributedLock Details
     */
    DistributedLock unlock(final String key, final String value);

    /**
     * Check lock key existence
     * @param key   lock key
     * @return true / false / null
     */
    Boolean locked(final String key);

}
