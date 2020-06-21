package gew.nodemanager.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BriefTopicInfo {

    private String topic;

    private String operator;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant gmt;

    public BriefTopicInfo() {

    }

    public BriefTopicInfo(String topic, String operator, Instant gmt) {
        this.topic = topic;
        this.operator = operator;
        this.gmt = gmt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BriefTopicInfo that = (BriefTopicInfo) o;
        return StringUtils.equalsIgnoreCase(topic, that.topic) &&
                StringUtils.equalsIgnoreCase(operator, that.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, operator);
    }
}
