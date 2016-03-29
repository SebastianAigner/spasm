import com.google.gson.Gson;
import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by sebi on 29.03.16.
 */
public class Analytics {
    Match match;
    ArrayList<RechatMessage> chatMessages;
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

    public ArrayList<String> getMessageList() {
        ArrayList<String> clearChatMessages = new ArrayList<>();
        for(RechatMessage message: chatMessages) {
            clearChatMessages.add(message.attributes.from + ": " + message.attributes.message);
        }
        return clearChatMessages;
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
}
