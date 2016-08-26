package actor.domain.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;

public interface EventTransport extends AutoCloseable {
    void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload);
    void listen(Consumer<Event> consumer);
    void close();

    public static class Event implements Serializable {
        public final Class<? extends Actor> senderClass;
        public final Class<? extends Actor> actorClass;
        public final Object payload;
        public Event(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) { 
            this.senderClass = senderClass;
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
