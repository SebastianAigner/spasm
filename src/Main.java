import com.google.gson.Gson;
import twitch.kraken.MatchData;
import twitch.rechat.RechatBlock;
import twitch.rechat.RechatErrorRequest;
import twitch.rechat.RechatErrors;
import twitch.rechat.RechatMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.io.InputStream;
import java.util.regex.*;
import javax.swing.*;

/**
 * Created by sebi on 26.03.16.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String link = JOptionPane.showInputDialog("Enter Twitch link");
        if(link == null) {
            return;
        }
        String sanitizedInput = sanitzeInput(link);
        String videoID = "v" + sanitizedInput;
        Match match = new Match();
        match.setMatchID(sanitizedInput);
        match.setTitle(getTitle(videoID));
        createInitialTimestamp(videoID, match);
        String page;
        long timestamp = match.getStartTimestamp();
        while (true) {
            page = fetchChat(videoID, timestamp);
            Gson gson = new Gson();
            RechatBlock r = gson.fromJson(page, RechatBlock.class);
            long lasttimestamp = 0;
            for (RechatMessage rechatMessage : r.data) {
                rechatMessage.attributes.timestamp /= 1000;
                match.addChatMessage(rechatMessage);
                lasttimestamp = rechatMessage.attributes.timestamp;
                rechatMessage.attributes.relativeTimestamp = rechatMessage.attributes.timestamp - match.getStartTimestamp();
                System.out.println("["+ String.format("%.2f",match.getPercentage(rechatMessage.attributes.relativeTimestamp)) + "%] " + rechatMessage.attributes.message);
            }
            if(lasttimestamp >= match.getEndTimestamp()) {
                break;
            }
            if (lasttimestamp == 0 || lasttimestamp == timestamp) {
                ++timestamp;
            } else {
                timestamp = lasttimestamp;
            }
        }
        Gson g = new Gson();
        String jsonified = g.toJson(match, Match.class);
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showDialog(null, "Save Game Report");
        if(choice == JFileChooser.APPROVE_OPTION) {
            System.out.println(chooser.getSelectedFile().getName());
            File file = chooser.getSelectedFile();
            if(!file.exists()) {
                boolean creation = file.createNewFile();
                if(creation) {
                    System.out.println("File created.");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(jsonified.getBytes());
                }
            }
        }
        else {
            System.out.println("Not saving. Dumping to console for backup.");
            System.out.println(jsonified);
        }
    }

    public static String fetchChat(String videoID, long start) throws Exception {
        String page = "";

        URL url = new URL("https://rechat.twitch.tv/rechat-messages?start=" + start + "&video_id=" + videoID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream input;
        if(conn.getResponseCode() == 200) {
            input = conn.getInputStream();
        }
        else {
            input = conn.getErrorStream();
        }
        Scanner s = new Scanner(input);
        while (s.hasNextLine()) {
            page += s.nextLine();
        }
        return page;
    }


    public static void createInitialTimestamp(String videoID, Match match) throws Exception {
        Gson g = new Gson();
        long start;
        long end;
        String errorMessage = fetchChat(videoID, 0);
        RechatErrorRequest r = g.fromJson(errorMessage, RechatErrorRequest.class);
        RechatErrors err = r.errors.get(0);
        if(err.status == 400) {
            System.out.println(err.detail);
            Pattern serverMessagePattern = Pattern.compile("(\\d{4,}) and (\\d{4,})");

            Matcher matcher = serverMessagePattern.matcher(err.detail);
            if(matcher.find()) {
                 start = Long.parseLong(matcher.group(1));
                 end = Long.parseLong(matcher.group(2));
            }
            else {
                throw new Exception("Error message has an unexpected format: " + err.detail);
            }
        }
        else {
            throw new Exception("Server returned status code " + r.errors.get(0).status);
        }
        match.setStartTimestamp(start);
        match.setEndTimestamp(end);
    }

    public static String sanitzeInput(String link) throws Exception {
        Pattern linkPattern = Pattern.compile("\\/v\\/(\\d+)");
        Matcher matcher = linkPattern.matcher(link);
        if(matcher.find()) {
            return matcher.group(1);
        }
        throw new Exception("Invalid link supplied: " + link);
    }

    public static String getTitle(String videoID) throws Exception{
        URL url = new URL("https://api.twitch.tv/kraken/videos/" + videoID + "?on_site=1");
        Scanner s = new Scanner(url.openStream());
        Gson g = new Gson();
        String file = "";
        while(s.hasNextLine()) {
            file += s.nextLine();
        }
        MatchData matchData = g.fromJson(file, MatchData.class);
        System.out.println("Match name:" + matchData.title);
        return matchData.title;
    }

}
