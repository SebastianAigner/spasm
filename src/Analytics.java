import com.google.gson.Gson;
import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by sebi on 29.03.16.
 */
public class Analytics {
    public static void main(String[] args) throws Exception {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showDialog(null,"Open Game Report");
        if(choice == JFileChooser.CANCEL_OPTION) {
            return;
        }

        Gson g = new Gson();
        String file = "";
        FileInputStream fis = new FileInputStream(chooser.getSelectedFile());
        Scanner s = new Scanner(fis);
        while(s.hasNextLine()) {
            file += s.nextLine();
        }
        Match match = g.fromJson(file, Match.class);
        ArrayList<RechatMessage> chatMessages = new ArrayList<RechatMessage>(match.getChatMessages().values());
        Collections.sort(chatMessages);
        System.out.println(chatMessages);
    }
}
