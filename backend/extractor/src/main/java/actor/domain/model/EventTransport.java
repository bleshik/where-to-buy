package actor.domain.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;

public interface EventTransport extends AutoCloseable {
    void send(Class<? extends Actor> senderClass, String actorClass, Object payload);
    default void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        send(senderClass, actorClass.getCanonicalName(), payload);
    }
    default void send(String actorClass, Object payload) {
        send(NoopActor.class, actorClass, payload);
    }

    void listen(Consumer<Event> consumer);
    void close();

    public static class Event implements Serializable {
        public final String senderClass;
        public final String actorClass;
        public final Object payload;

        public Event(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) { 
            this(senderClass, actorClass.getCanonicalName(), payload);
        }

        public Event(Class<? extends Actor> senderClass, String actorClass, Object payload) { 
            this.senderClass = senderClass.getCanonicalName();
            this.actorClass = actorClass;
            this.payload = payload;
        }

        public byte[] toByteArray() {
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(b);
                out.writeObject(this);
                return b.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }

        public static Event fromByteArray(byte[] obj) {
            try {
                ByteArrayInputStream b = new ByteArrayInputStream(obj);
                ObjectInputStream in = new ObjectInputStream(b);
                return (Event) in.readObject();
            } catch(IOException|ClassNotFoundException e) {
                return null;
            }
        }
    }
}
