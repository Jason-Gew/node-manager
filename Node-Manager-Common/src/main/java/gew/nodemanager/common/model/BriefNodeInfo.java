package gew.nodemanager.common.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jason/GeW
 * @since 2019-03-24
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BriefNodeInfo extends AbstractNodeInfo {

    private Map<String, Object> extraInfo;

    public BriefNodeInfo() {
        super();
        this.extraInfo = new ConcurrentHashMap<>();
    }

    public BriefNodeInfo(@NotNull String uuid, String name) {
        super(uuid, name);
        this.extraInfo = new ConcurrentHashMap<>();
    }

    public BriefNodeInfo(@NotNull String uuid, @NotNull String name, Long upTime) {
        super(uuid, name, upTime);
        this.extraInfo = new ConcurrentHashMap<>();
    }



    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void addExtraInfo(final String key, final Object value) {
        this.extraInfo.put(key, value);
    }

    public void addExtraInfo(final Map<String, Object> values) {
        if (values != null && !values.isEmpty()) {
            values.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .forEach(entry -> this.extraInfo.put(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
