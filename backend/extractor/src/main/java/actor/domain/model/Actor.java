package actor.domain.model;

public interface Actor {

    default void reply(Object payload) {
        send(Dispatcher.sender.get(), payload);
    }

    default void sendToMyself(Object payload) {
        send(this.getClass(), payload);
    }

    default void handle(Class<? extends Actor> actor, Object payload) {
        handle(actor.getCanonicalName(), payload);
    }

    default void handle(String actor, Object payload) {
        Dispatcher.dispatcher.get().handle(this.getClass(), actor, payload);
    }

    default void send(String actor, Object payload) {
        Dispatcher.dispatcher.get().send(this.getClass(), actor, payload);
    }

    default void send(Class<? extends Actor> actor, Object payload) {
        send(actor.getCanonicalName(), payload);
    }

}
