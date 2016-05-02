package io.sebi.spasm.rechat;

/**
 * Created by Sebastian Aigner
 */

/**
 * A rechat message is a single message sent by a user. It comes with a lot of metadata, which is partially stored in
 * the attributes of the message.
 */
public class RechatMessage implements Comparable<RechatMessage> {
    public String id;
    public RechatMessageAttributes attributes;

    /**
     * Displays a RechatMessage as a nicely formatted String with a preluding timestamp and content.
     * @return Timestamp and Message of the RechatMessage
     */
    @Override
    public String toString() {
        long hours,
                minutes,
                seconds;
        hours = attributes.relativeTimestamp / 3600;
        minutes = (attributes.relativeTimestamp % 3600) / 60;
        seconds = (attributes.relativeTimestamp % 60);
        return String.format("[%02d:%02d:%02d]", hours, minutes, seconds) + this.attributes.message;
    }

    @Override
    public int compareTo(RechatMessage o) {
        return (int) (this.attributes.timestamp - o.attributes.timestamp);
    }
}
