package gew.nodemanager.common.model;

/**
 * @author Jason/GeW
 * @since 2019-03-24
 */
public class NodeCrudException extends RuntimeException {

    public NodeCrudException(String message) {
        super(message);
    }

    public NodeCrudException(String message, Throwable cause) {
        super(message, cause);
    }
}
