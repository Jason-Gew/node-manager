package gew.nodemanager.common.model;

import java.time.Instant;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class DistributedLock {

    private boolean success;

    private Action action;

    private String key;

    private String value;

    private Instant timestamp;

    private String msg;


    public enum Action {
        LOCK,

        UNLOCK,

        EXTEND_LOCK,
    }

    public DistributedLock() {
        // Default Constructor
    }

    public DistributedLock(boolean success, Action action, String key) {
        this.success = success;
        this.action = action;
        this.key = key;
    }

    public DistributedLock(boolean success, Action action, String key, String msg) {
        this.success = success;
        this.action = action;
        this.key = key;
        this.msg = msg;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
