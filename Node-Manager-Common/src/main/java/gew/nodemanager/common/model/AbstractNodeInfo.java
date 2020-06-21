package gew.nodemanager.common.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public abstract class AbstractNodeInfo {

    @Id
    @NotNull
    protected String uuid;

    @NotNull
    protected String name;

    protected Long upTime;

    protected String timeZoneOffset;

    protected String ip;


    public AbstractNodeInfo() {
        // Default Constructor
    }

    public AbstractNodeInfo(@NotNull String uuid,  @NotNull String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public AbstractNodeInfo(@NotNull String uuid, @NotNull String name, Long upTime) {
        this.uuid = uuid;
        this.name = name;
        this.upTime = upTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUpTime() {
        return upTime;
    }

    public void setUpTime(Long upTime) {
        this.upTime = upTime;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNodeInfo that = (AbstractNodeInfo) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(upTime, that.upTime) &&
                Objects.equals(timeZoneOffset, that.timeZoneOffset) &&
                Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, upTime, timeZoneOffset, ip);
    }
}
