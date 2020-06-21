package gew.nodemanager.common.model;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class TopicCrudException extends RuntimeException {

    public TopicCrudException(String message) {
        super(message);
    }

    public TopicCrudException(String message, Throwable cause) {
        super(message, cause);
    }
}
