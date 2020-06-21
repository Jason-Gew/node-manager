package gew.nodemanager.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gew.nodemanager.common.model.PubSubMsgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "topic_subscription", indexes = {@Index(name = "idx_node_uuid", columnList = "node_uuid"),
        @Index(name = "idx_topic", columnList = "topic")})
public class TopicSubscription implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(name = "node_uuid", nullable = false, length = 128)
    private String nodeUuid;

    @NotNull
    @NotEmpty
    @Column(nullable = false, length = 512)
    private String topic;

    @Column(name = "msg_type", length = 128)
    private String msgType;

    private Boolean subscribing;

    @Column(name = "create_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Column(nullable = false)
    private String operator;

    private static final long serialVersionUID = 20200324L;


    @JsonIgnore
    public PubSubMsgType getPubSubMsgType() {
        return PubSubMsgType.toType(this.msgType);
    }

}
