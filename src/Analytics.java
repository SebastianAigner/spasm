import com.google.gson.Gson;
import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.List;

/**
 * Created by sebi on 29.03.16.
 */
public class Analytics {
    Match match;
    List<RechatMessage> chatMessages;
    public void openGameReport() {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showDialog(null,"Open Game Report");
        if(choice == JFileChooser.CANCEL_OPTION) {
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
        while(s.hasNextLine()) {
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
        if(match == null) {
            return "No file open";
        }
        if(match.getChatMessages() == null) {
            return "No chat messages!";
        }
        return "Opened successfully.";
    }

    public List<RechatMessage> findMessageText(String text) {
        ArrayList<RechatMessage> results = new ArrayList<>();
        for(RechatMessage rechatMessage: chatMessages) {
            if(rechatMessage.attributes.message.equals(text)) {
                results.add(rechatMessage);
            }
        }
        return results;
    }
}
