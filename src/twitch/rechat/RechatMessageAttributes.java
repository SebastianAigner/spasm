package twitch.rechat;

/**
 * Created by sebi on 26.03.16.
 */

/**
 * The rechat message attributes contain all information about a message sent in twitch rechat as well as the message
 * itself.
 */
public class RechatMessageAttributes {
    public String room;
    public long timestamp;
    public String message;
    public String from;
    public long relativeTimestamp;
}
