package actor.domain.model;

import java.util.function.Consumer;

public interface EventTransport {
    void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload);
    void listen(Consumer<Event> consumer);

    public static class Event {
        public final Class<? extends Actor> senderClass;
        public final Class<? extends Actor> actorClass;
        public final Object payload;
        public Event(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) { 
            this.senderClass = senderClass;
            this.actorClass = actorClass;
            this.payload = payload;
        }
    }
}
