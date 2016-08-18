package actor.domain.model;

public class ActorException extends RuntimeException {
    public ActorException(String msg, Exception ex) {
        super(msg, ex);
    }
}
