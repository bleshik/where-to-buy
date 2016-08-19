package actor.domain.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.*;

public final class Dispatcher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Function<Class<? extends Actor>, Actor> actors;
    private final EventTransport eventTransport;
    static ThreadLocal<Class<? extends Actor>> sender = new ThreadLocal<>();
    static ThreadLocal<Dispatcher> dispatcher = new ThreadLocal<>();

    static ClassValue<Map<Class, Method>> handlingMethods =
        new ClassValue<Map<Class, Method>>() {
        @Override
        protected Map<Class, Method> computeValue(Class<?> type) {
            Stream<Method> whenMethods = Stream.empty();
            Class<?> curClass = type;
            while(curClass != Object.class) {
                whenMethods = Stream.concat(
                    whenMethods,
                    Arrays.asList(
                        curClass.getDeclaredMethods()
                    ).stream().filter(m ->
                        m.getName().equals("when") && (m.getParameterTypes().length == 1 || m.getParameterTypes().length == 2)
                    ).map(m -> {
                        m.setAccessible(true);
                        return m;
                    })
                );
                curClass = curClass.getSuperclass();
            }
            return whenMethods.collect(
                groupingBy((m) -> (Class) m.getParameterTypes()[0],
                    collectingAndThen(toList(), (list) -> list.get(0))
                )
            );
        }
    };

    public Dispatcher(EventTransport eventTransport) {
        this.eventTransport = eventTransport;
        this.actors = (Class<? extends Actor> c) -> {
            try {
                return (Actor) c.getConstructor().newInstance();
            } catch (Exception ex) {
                throw new ActorException("Failed to create an actor " + c.getCanonicalName(), ex);
            }
        };
        this.eventTransport.listen((event) -> handle(event.senderClass, event.actorClass, event.payload));
    }

    public Dispatcher(EventTransport eventTransport, Function<Class<? extends Actor>, Actor> actors) {
        this.eventTransport = eventTransport;
        this.actors = actors;
        this.eventTransport.listen((event) -> handle(event.senderClass, event.actorClass, event.payload));
    }


    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        pool.execute(() -> {
            try {
                sender.set(senderClass);
                dispatcher.set(this);
                eventTransport.send(senderClass, actorClass, payload);
            } catch (Exception ex) {
                logger.error("Actor failed", ex);
            }
        });
    }

    public void send(Class<? extends Actor> actorClass, Object payload) {
        send(NoopActor.class, actorClass, payload);
    }

    public void handle(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        try {
            Optional<Method> when = handlingMethods.get(actorClass)
                .entrySet()
                .stream()
                .filter((e) -> e.getKey().isInstance(payload))
                .findAny()
                .map((w) -> w.getValue());
            if (!when.isPresent()) {
                throw new IllegalArgumentException(
                        actorClass.getCanonicalName() +
                        " does not know how to process " +
                        payload.getClass().getCanonicalName()
                );
            }
            if (when.get().getParameterTypes().length == 1) {
                when.get().invoke(actors.apply(actorClass), payload);
            } else {
                when.get().invoke(actors.apply(actorClass), payload, sender);
            }
        } catch (IllegalAccessException|InvocationTargetException ex) {
            throw new ActorException("Actor handle failed", ex);
        }
    }

    public void awaitTermination() {
        try {
            pool.shutdown();
            while (!pool.awaitTermination(10, TimeUnit.SECONDS)) {}
        } catch (InterruptedException e) { }
    }

}
