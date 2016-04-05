import com.google.gson.Gson;
import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Sebastian Aigner
 */


/**
 * The analytics class provides helpful methods to analyze a game report.
 */
public class Analytics {
    private Broadcast broadcast;
    private List<RechatMessage> chatMessages;

    public enum FileOpenStatus {
        SUCCESS,
        CANCEL,
        EMPTY
    }

    /**
     * Opens a game report for the current analysis session. Presents the user with a file dialog to open the file.
     */
    public void openGameReport() {
        //Consider moving this part into the actual GUI and only pass a FileInputStream here
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showDialog(null, "Open Game Report");
        if (choice == JFileChooser.CANCEL_OPTION) {
            return;
        }
        Gson g = new Gson();
        String file = "";
        FileInputStream fis;
        try {
            fis = new FileInputStream(chooser.getSelectedFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Scanner s = new Scanner(fis);
        while (s.hasNextLine()) {
            file += s.nextLine();
        }
        broadcast = g.fromJson(file, Broadcast.class);
        chatMessages = new ArrayList<>(broadcast.getChatMessages().values());
        Collections.sort(chatMessages);
        System.out.println(chatMessages);
    }

    /**
     * Get list of messages in the current analysis session (search filters do not apply)
     * @return list of messages
     */
    public List<RechatMessage> getMessageList() {
        return chatMessages;
    }

    public FileOpenStatus getFileOpenStatus() {
        if (broadcast == null) {
            return FileOpenStatus.CANCEL;
        }
        if (broadcast.getChatMessages() == null) {
            return FileOpenStatus.EMPTY;
        }
        return FileOpenStatus.SUCCESS;
    }

    /**
     * Finds a specific message within the current analysis session
     * @param text search text
     * @return result RechatMessages
     */
    public List<RechatMessage> findMessageText(String text) {
        ArrayList<RechatMessage> results = new ArrayList<>();
        for (RechatMessage rechatMessage : chatMessages) {
            if (rechatMessage.attributes.message.equals(text)) {
                results.add(rechatMessage);
            }
        }
        return results;
    }

    /**
     * Find messages that contain one or multiple comma-seperated strings in the current analysis session.
     * @param text search text (can contain comma-delimiters)
     * @param ignoreDelimiters whether delimiters should be ignored (search for the "raw" string)
     * @param caseSensitive whether the search should be case sensitive
     * @return list of search results. Null if the chat messages for the current analytics session are also null.
     */
    public List<RechatMessage> findMesasgeTextContains(String text, boolean ignoreDelimiters, boolean caseSensitive) {
        if(chatMessages == null) {
            return null;
        }
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        List<String> searchTerms = new ArrayList<>();
        if (ignoreDelimiters) {
            searchTerms.add(text);
        } else {
            searchTerms = Arrays.asList(text.split(","));
        }
        List<RechatMessage> results = new ArrayList<>();
        for (RechatMessage rechatMessage : chatMessages) {
            String rechatMessageText = rechatMessage.attributes.message;
            if (!caseSensitive) {
                rechatMessageText = rechatMessageText.toLowerCase();
            }
            for (String searchTerm : searchTerms) {
                if (rechatMessageText.contains(searchTerm)) {
                    results.add(rechatMessage);
                }
            }
        }
        return results;
    }

    /**
     * Creates a URL for the replay of the chat message passed in
     * @param rechatMessage message which should be used to determine the timestamp for the URL
     * @return timestamped twitch video URL
     */
    public String getLinkForChatMessage(RechatMessage rechatMessage) {
        if(rechatMessage == null) {
            return null;
        }
        return broadcast.getBroadcastLink() + "?t=" + rechatMessage.attributes.relativeTimestamp / 60 + "m" + rechatMessage.attributes.relativeTimestamp % 60 + "s";
    }

    /**
     * Creates a URL based on a twitch timestamp relative to the broadcast start
     * @param timestamp timestamp relative to the broadcast start
     * @return timestamped twitch video URL
     */
    public String getLinkForTimestamp(long timestamp) {
        return broadcast.getBroadcastLink() + "?t=" + timestamp / 60 + "m" + timestamp % 60 + "s";
    }

    /**
     * Creates a URL based on percentage in the broadcast.
     * @param percentage percentage to use as timestamp
     * @return timestamped twitch video URL
     */
    public String getLinkForPercentage(double percentage) {
        return getLinkForTimestamp((int) (broadcast.getLength() * percentage));
    }

    public Broadcast getBroadcast() {
        return broadcast;
    }

    /**
     * Get the words that have been used the most throughout the broadcast
     * @return list of most used words in a broadcast
     */
    public List<String> getMostMessagedWords() {
        LinkedHashMap<String, Integer> wordbank = new LinkedHashMap<>();
        for (RechatMessage rechatMessage : chatMessages) {
            String[] words = rechatMessage.attributes.message.split(" ");
            for (String word : words) {
                String currentWord = word.toLowerCase();
                if (wordbank.containsKey(currentWord)) {
                    int currentWordCount = wordbank.get(currentWord);
                    ++currentWordCount;
                    wordbank.put(currentWord, currentWordCount);
                } else {
                    wordbank.put(currentWord, 1);
                }
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(wordbank.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue()) * -1;
            }
        });
        List<String> mostUsed = new ArrayList<>();
        for (Map.Entry mapEntry : list) {
            mostUsed.add((String) mapEntry.getKey());
        }
        mostUsed = mostUsed.subList(0, 50);
        return mostUsed;
    }


    /**
     * Does a frequency analysis for the passed Rechat messages. The frequency analysis is done by grouping the messages
     * by their timestamp in the given resolution and counting the occurrences within these groups.
     * @param messages messages of which the frequency will be determined
     * @param resolution amount of values to produce
     * @return
     */
    public List<Integer> getChatmessageDistribution(List<RechatMessage> messages, int resolution) {
        if(messages == null) {
            return null;
        }
        int currentBlock = 0;
        int listcounter = 0;
        ArrayList<Integer> distribution = new ArrayList<>();
        while (listcounter < messages.size()) {
            RechatMessage currentMessage = messages.get(listcounter);
            if (currentMessage.attributes.relativeTimestamp <= currentBlock * resolution + resolution) {
                if (currentBlock < distribution.size()) {
                    distribution.set(currentBlock, distribution.get(currentBlock) + 1);
                } else {
                    distribution.add(1); //open next
                }
            } else {
                if (currentBlock >= distribution.size()) {
                    distribution.add(0); //empty
                }
                currentBlock++;
                continue;
            }
            ++listcounter;
        }
        return distribution;
    }

    /**
     * Creates a timestamp from a percentage in a broadcast
     * @param percentage percentage at which the timestamp should be
     * @return timestamp
     */
    public long getTimestampForPercentage(double percentage) {
        return (long) (broadcast.getLength() * percentage);
    }
}
