package gew.nodemanager.common.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@RedisHash(value = ConstantParams.REDIS_HASH_PREFIX)
public class ServiceNodeInfo extends AbstractNodeInfo implements Serializable, Cloneable {

    private Integer port;

    private Boolean useCloudConfig;

    private ConfigFileFormat configFileFormat;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String configFileRowKey;

    private HeartBeatInfo heartBeatInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String healthCheckApi;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String eventPubTopic;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cmdSubTopic;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;


    public ServiceNodeInfo(@NotNull String uuid, @NotNull String name) {
        super(uuid, name);
    }

    public ServiceNodeInfo(@NotNull String uuid, @NotNull String name, Long upTime) {
        super(uuid, name, upTime);
    }

    public BriefNodeInfo toBriefNodeInfo() {
        BriefNodeInfo briefInfo = new BriefNodeInfo(super.uuid, super.name, super.upTime);
        briefInfo.setIp(super.ip);
        briefInfo.setTimeZoneOffset(super.timeZoneOffset);
        if (this.heartBeatInfo != null && this.heartBeatInfo.getInfo() != null) {
            briefInfo.setExtraInfo(this.heartBeatInfo.getInfo());
        }
        return briefInfo;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ServiceNodeInfo serviceNodeInfo = (ServiceNodeInfo) super.clone();
        if (this.heartBeatInfo != null) {
            serviceNodeInfo.setHeartBeatInfo((HeartBeatInfo) this.getHeartBeatInfo().clone());
        }
        return serviceNodeInfo;
    }
}
