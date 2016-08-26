package actor.port.adapter.local;

import actor.domain.model.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilteringEventTransport implements EventTransport {
        
    private final EventTransport wrapped;
    private final Collection<Predicate<EventTransport.Event>> accepts;

    public FilteringEventTransport(EventTransport wrapped, Predicate<EventTransport.Event>... accepts) {
        this.wrapped = wrapped;
        this.accepts  = Arrays.asList(accepts);
    }

    @Override
    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        if (accepts.stream().allMatch(a -> a.test(new EventTransport.Event(senderClass, actorClass, payload)))) {
            wrapped.send(senderClass, actorClass, payload);
        }
    }

    @Override
    public void listen(Consumer<EventTransport.Event> consumer) {
        wrapped.listen(consumer);
    }

    @Override
    public void close() {
        wrapped.close();
    }

}
