import java.util.HashMap;

/**
 * Created by sebi on 29.03.16.
 */
public class Match {
    String matchID;
    long startTimestamp;
    long endTimestamp;
    HashMap<String, RechatMessage> chatMessages = new HashMap<>();

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

    public HashMap<String, RechatMessage> getChatMessages() {
        return chatMessages;
    }

    public void addChatMessage(RechatMessage rechatMessage) {
        chatMessages.put(rechatMessage.id, rechatMessage);
    }

    public long getLength() {
        return endTimestamp-startTimestamp;
    }

    public float getPercentage(long timestamp) {
        float percentage = timestamp / (float)getLength() * 100;
        return percentage;
    }
}
