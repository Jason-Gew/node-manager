package gew.nodemanager.common.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gew.nodemanager.common.util.TimeIntervalHelper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartBeatInfo implements Cloneable {

    private Long ttl;

    private String timeUnit;

    private String key;

    private Map<String, Object> info;


    @JsonIgnore
    public boolean isValid() {
        if (ttl == null || ttl < 1) {
            return false;
        }
        try {
            TimeIntervalHelper.toTimeUnit(timeUnit);
        } catch (IllegalArgumentException iae) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        HeartBeatInfo heartBeatInfo;
        heartBeatInfo = (HeartBeatInfo) super.clone();
        if (this.info != null && !this.info.isEmpty()) {
            heartBeatInfo.setInfo(new HashMap<>(this.info));
        }
        return heartBeatInfo;
    }
}
