package gew.nodemanager.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Data
@Component
@Validated
@Configuration
@ConfigurationProperties(prefix = "node.manager")
public class NodeManagerConfig {

    @NotNull
    private String id;

    private String region;

    @NotNull
    @NotEmpty
    private String nodeListRowKey;

    private Integer nodeListTtl;

    private String heartBeatRowKey;

    private Integer evictNodeAfterSilent;

    private String eventSubTopic;

    private String cmdPubTopic;

}
