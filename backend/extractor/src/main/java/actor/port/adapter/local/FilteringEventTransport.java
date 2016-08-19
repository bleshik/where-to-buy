package actor.port.adapter.local;

import actor.domain.model.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilteringEventTransport implements EventTransport {
        
    private final EventTransport wrapped;
    private final Predicate<EventTransport.Event> accept;

    public FilteringEventTransport(EventTransport wrapped, Predicate<EventTransport.Event> accept) {
        this.wrapped = wrapped;
        this.accept  = accept;
    }

    @Override
    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        if (accept.test(new EventTransport.Event(senderClass, actorClass, payload))) {
            wrapped.send(senderClass, actorClass, payload);
        }
    }

    @Override
    public void listen(Consumer<EventTransport.Event> consumer) {
        wrapped.listen(consumer);
    }
}
