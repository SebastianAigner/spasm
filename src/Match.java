import twitch.rechat.RechatMessage;

import java.util.HashMap;

/**
 * Created by sebi on 29.03.16.
 */
public class Match {
    private String matchID;
    private String title;
    private long startTimestamp;
    private long endTimestamp;
    private String matchLink;

    public String getMatchLink() {
        return matchLink;
    }

    public void setMatchLink(String matchLink) {
        this.matchLink = matchLink;
    }

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
        return endTimestamp - startTimestamp;
    }

    public float getPercentage(long timestamp) {
        return timestamp / (float) getLength() * 100;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
