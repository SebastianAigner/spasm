import twitch.rechat.RechatMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sebastian Aigner
 */

public class Broadcast {
    private String broadcastID;
    private String title;
    private long startTimestamp;
    private long endTimestamp;
    private String broadcastLink;

    public String getBroadcastLink() {
        return broadcastLink;
    }

    public void setBroadcastLink(String broadcastLink) {
        this.broadcastLink = broadcastLink;
    }

    private Map<String, RechatMessage> chatMessages = new HashMap<>();

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Map<String, RechatMessage> getChatMessages() {
        return chatMessages;
    }

    public void addChatMessage(RechatMessage rechatMessage) {
        chatMessages.put(rechatMessage.id, rechatMessage);
    }

    public long getLength() {
        return endTimestamp - startTimestamp;
    }

    public float getPercentage(long timestamp) {
        return Math.max(0, Math.min(100,timestamp / (float) getLength() * 100));
    }

    public String getBroadcastID() {
        return broadcastID;
    }

    public void setBroadcastID(String broadcastID) {
        this.broadcastID = broadcastID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
