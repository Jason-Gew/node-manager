package gew.nodemanager.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;

/**
 * Common Format for an unified HTTP Response.
 * @author Jason/GeW
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResponse<T> implements Serializable {

    private Integer code;

    private Status status;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pagination pagination;


    public CommonResponse() {

    }

    public CommonResponse(T result) {
        this.code = 200;
        this.status = Status.SUCCESS;
        this.message = "Operation Success";
        this.result = result;
        this.timestamp = Instant.now().toString();
    }

    public CommonResponse(Integer code, Status status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    public CommonResponse(Integer code, Status status, String message, T result) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.result = result;
        this.timestamp = Instant.now().toString();
    }

    public CommonResponse(Integer code, Status status, String message, T result, String timestamp) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.result = result;
        this.timestamp = timestamp;
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }

    public Pagination getPagination() {
        return pagination;
    }
    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        return "CommonResponse{" +
                "code=" + code +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", result=" + result +
                ", pagination=" + pagination +
                '}';
    }
}
