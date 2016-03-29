package twitch.rechat;

/**
 * Created by sebi on 26.03.16.
 */

/**
 * A rechat message is a single message sent by a user. It comes with a lot of metadata, which is partially stored in
 * the attributes of the message.
 */
public class RechatMessage implements Comparable<RechatMessage> {
    public String id;
    public RechatMessageAttributes attributes;

    @Override
    public String toString() {
        return Long.toString(attributes.relativeTimestamp);
    }

    @Override
    public int compareTo(RechatMessage o) {
        return (int) (this.attributes.timestamp - o.attributes.timestamp);
    }
}
