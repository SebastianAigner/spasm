import com.google.gson.Gson;
import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by sebi on 29.03.16.
 */
public class Analytics {
    private Match match;
    private List<RechatMessage> chatMessages;

    public void openGameReport() {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showDialog(null, "Open Game Report");
        if (choice == JFileChooser.CANCEL_OPTION) {
            return;
        }
        Gson g = new Gson();
        String file = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(chooser.getSelectedFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner s = new Scanner(fis);
        while (s.hasNextLine()) {
            file += s.nextLine();
        }
        match = g.fromJson(file, Match.class);
        chatMessages = new ArrayList<>(match.getChatMessages().values());
        Collections.sort(chatMessages);
        System.out.println(chatMessages);
    }

    public List<RechatMessage> getMessageList() {
        return chatMessages;
    }

    public String getFileOpenStatus() {
        if (match == null) {
            return "No file open";
        }
        if (match.getChatMessages() == null) {
            return "No chat messages!";
        }
        return "Opened successfully.";
    }

    public List<RechatMessage> findMessageText(String text) {
        ArrayList<RechatMessage> results = new ArrayList<>();
        for (RechatMessage rechatMessage : chatMessages) {
            if (rechatMessage.attributes.message.equals(text)) {
                results.add(rechatMessage);
            }
        }
        return results;
    }

    public List<RechatMessage> findMesasgeTextContains(String text, boolean ignoreDelimiters, boolean caseSensitive) {
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

    public String getLinkForChatMessage(RechatMessage rechatMessage) {
        return match.getMatchLink() + "?t=" + rechatMessage.attributes.relativeTimestamp / 60 + "m" + rechatMessage.attributes.relativeTimestamp % 60 + "s";
    }

    public String getLinkForTimestamp(long timestamp) {
        return match.getMatchLink() + "?t=" + timestamp / 60 + "m" + timestamp % 60 + "s";
    }

    public String getLinkForPercentage(double percentage) {
        return getLinkForTimestamp((int) (match.getLength() * percentage));
    }

    public Match getMatch() {
        return match;
    }

    public List<String> getMostMessagedWords() {
        LinkedHashMap<String, Integer> wordbank = new LinkedHashMap<>();
        for (RechatMessage rechatMessage : chatMessages) {
            String[] words = rechatMessage.attributes.message.split(" ");
            for (int i = 0; i < words.length; ++i) {
                String currentWord = words[i].toLowerCase();
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


    public List<Integer> getChatmessageDistribution(List<RechatMessage> messages, int resolution) {
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

    public long getTimestampForPercentage(double percentage) {
        return (long) (match.getLength() * percentage);
    }
}
