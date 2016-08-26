package actor.port.adapter.local;

import actor.domain.model.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LocalEventTransport implements EventTransport {

    private Consumer<Event> consumer;

    @Override
    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        if (consumer != null) {
            consumer.accept(new Event(senderClass, actorClass, payload));
        }
    }

    @Override
    public void listen(Consumer<Event> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void close() {}

}
