package actor.port.adapter.local;

import actor.domain.model.*;
import java.util.function.Consumer;

public class LocalEventTransport implements EventTransport {
        
    private Consumer<Event> consumer;

    @Override
    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        if (this.consumer != null) {
            this.consumer.accept(new Event(senderClass, actorClass, payload));
        }
    }

    @Override
    public void listen(Consumer<Event> consumer) {
        this.consumer = consumer;
    }
}
