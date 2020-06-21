package gew.nodemanager.common.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.OffsetTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import gew.nodemanager.common.util.UidGenerator;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

import static gew.nodemanager.common.model.ConstantParams.INTEGER_DT_PREFIX;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class BasicNodeMsg<T extends Serializable> implements Serializable, Comparable<BasicNodeMsg<T>> {

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetTimeDeserializer.class)
    private OffsetDateTime datetime;

    private String uuid;

    private int priority;

    @NotNull
    private String topic;

    @NotNull
    private T payload;

    @NotNull
    private String signature;


    public BasicNodeMsg() {
        this.datetime = OffsetDateTime.now();
        this.uuid = UidGenerator.generate(INTEGER_DT_PREFIX.format(datetime), 32);
    }

    public BasicNodeMsg(final String topic) {
        this.topic = topic;
        this.datetime = OffsetDateTime.now();
        this.uuid = UidGenerator.generate(INTEGER_DT_PREFIX.format(datetime), 32);
    }

    public BasicNodeMsg(String topic, T payload) {
        this.topic = topic;
        this.payload = payload;
        this.datetime = OffsetDateTime.now();
        this.uuid = UidGenerator.generate(INTEGER_DT_PREFIX.format(datetime), 32);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(BasicNodeMsg msg) {
        if (msg == null) {
            return -1;
        } else {
            return Integer.compare(this.priority, msg.priority);
        }
    }

    public OffsetDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(OffsetDateTime datetime) {
        this.datetime = datetime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicNodeMsg<?> that = (BasicNodeMsg<?>) o;
        return priority == that.priority &&
                Objects.equals(datetime, that.datetime) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime, uuid, priority, topic, payload, signature);
    }

    @Override
    public String toString() {
        return "BasicNodeMsg{" +
                "datetime=" + datetime +
                ", uuid='" + uuid + '\'' +
                ", priority=" + priority +
                ", topic='" + topic + '\'' +
                ", payload=" + payload +
                ", signature='" + signature + '\'' +
                '}';
    }
}
