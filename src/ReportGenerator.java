import com.google.gson.Gson;
import twitch.kraken.BroadcastData;
import twitch.rechat.RechatBlock;
import twitch.rechat.RechatErrorRequest;
import twitch.rechat.RechatErrors;
import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sebastian Aigner
 */

/**
 * This (originally seperate) tool allows to download twitch chat protocols. It saves them in JSON, which in return can
 * be read by the main analytics application
 */
public class ReportGenerator extends SwingWorker<Void, Void> {

    private String videoURL;
    private String lastMessage; // used for previewing messages in the UI

    public String getLastMessage() {
        return lastMessage;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public ReportGenerator(String videoURL) {
        this.videoURL = videoURL;
    }

    /**
     * Creates a report based on a twitch video URL pasted by the user in the background.
     * Once the creation of the report is done, prompts the user for a file to save to.
     * @throws Exception
     */
    @Override
    public Void doInBackground() throws Exception {
        if (videoURL == null) {
            return null;
        }
        String sanitizedInput = sanitzeInput(videoURL);
        String videoID = "v" + sanitizedInput;
        Broadcast broadcast = new Broadcast();
        broadcast.setBroadcastID(sanitizedInput);
        broadcast.setTitle(getTitle(videoID));
        createInitialTimestamp(videoID, broadcast);
        broadcast.setBroadcastLink(videoURL);
        String page;
        long timestamp = broadcast.getStartTimestamp();
        while (!isCancelled()) {
            page = fetchChat(videoID, timestamp);
            long lasttimestamp = 0;
            Gson gson = new Gson();
            RechatBlock r;
            try {
                r = gson.fromJson(page, RechatBlock.class);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.err.println("The error occurred while fetching " + videoID + " at " + TimestampHelper.secondPrecisionTimestampToString(timestamp));
                ++lasttimestamp;
                continue;
            }
            if (r.data != null) {
                for (RechatMessage rechatMessage : r.data) {
                    rechatMessage.attributes.timestamp /= 1000;
                    if (rechatMessage.attributes.message.equals("")) {
                        rechatMessage.attributes.message = "<message removed>";
                    }
                    broadcast.addChatMessage(rechatMessage);
                    lasttimestamp = rechatMessage.attributes.timestamp;
                    rechatMessage.attributes.relativeTimestamp = rechatMessage.attributes.timestamp - broadcast.getStartTimestamp();
                    this.lastMessage = rechatMessage.attributes.message;
                }
            }
            setProgress((int)broadcast.getPercentage(lasttimestamp-broadcast.getStartTimestamp()));
            if (lasttimestamp >= broadcast.getEndTimestamp()) {
                break;
            }
            if (lasttimestamp == 0 || lasttimestamp == timestamp) {
                ++timestamp;
            } else {
                timestamp = lasttimestamp;
            }
        }
        Gson g = new Gson();
        String resultJsonFormatted = g.toJson(broadcast, Broadcast.class);
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showDialog(null, "Save Game Report");
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.exists()) {
                boolean creation = file.createNewFile();
                if (creation) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(resultJsonFormatted.getBytes());
                }
            }
        } else {
            System.out.println("Not saving. Dumping to console for backup.");
            System.out.println(resultJsonFormatted);
        }
        return null;
    }

    /**
     * Fetches a segment of chat for a given videoID at a given timestamp
     * @param videoID videoID to fetch the segment from
     * @param start timestamp from which on the messages will be fetched
     * @return messages as delivered by the twitch API
     * @throws Exception
     */
    public static String fetchChat(String videoID, long start) throws Exception {
        String page = "";

        URL url = new URL("https://rechat.twitch.tv/rechat-messages?start=" + start + "&video_id=" + videoID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream input;
        if (conn.getResponseCode() == 200) {
            input = conn.getInputStream();
        } else {
            input = conn.getErrorStream();
        }
        Scanner s = new Scanner(input);
        while (s.hasNextLine()) {
            page += s.nextLine();
        }
        return page;
    }

    /**
     * Creates the initial timestamp for a broadcast by provoking an error message in the twitch API. The error message
     * contains both start and end timestamp for the broadcast.
     * @param videoID
     * @param broadcast
     * @throws Exception
     */
    public static void createInitialTimestamp(String videoID, Broadcast broadcast) throws Exception {
        Gson g = new Gson();
        long start;
        long end;
        String errorMessage;
        try {
             errorMessage = fetchChat(videoID, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        RechatErrorRequest r = g.fromJson(errorMessage, RechatErrorRequest.class);
        RechatErrors err = r.errors.get(0);
        if (err.status == 400) {
            /*
                This pattern matches on the Twitch API error message
                "X is not between TIME_START and TIME_END"
             */
            Pattern serverMessagePattern = Pattern.compile("(\\d{4,}) and (\\d{4,})");
            Matcher matcher = serverMessagePattern.matcher(err.detail);
            if (matcher.find()) {
                start = Long.parseLong(matcher.group(1));
                end = Long.parseLong(matcher.group(2));
            } else {
                throw new Exception("Error message has an unexpected format: " + err.detail);
            }
        } else {
            throw new Exception("Server returned status code " + r.errors.get(0).status);
        }
        broadcast.setStartTimestamp(start);
        broadcast.setEndTimestamp(end);
    }

    /**
     * Takes a twitch past broadcast link and filters out the "/v/ID_GOES_HERE" part of the URL.
     * @param link link to be filtered for ID
     * @return video ID (without preluding /v/)
     * @throws Exception
     */
    public static String sanitzeInput(String link) throws Exception {
        Pattern linkPattern = Pattern.compile("\\/v\\/(\\d+)");
        Matcher matcher = linkPattern.matcher(link);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new Exception("Invalid link supplied: " + link);
    }

    /**
     * Gets the title of a broadcast from the Twitch Kraken API
     * @param videoID videoID of which to fetch the title
     * @return Title of the broadcast
     */
    public static String getTitle(String videoID) {
        URL url;
        try {
             url = new URL("https://api.twitch.tv/kraken/videos/" + videoID + "?on_site=1");
        }
        catch (MalformedURLException mex) {
            System.err.println("Could not get broadcast title from the Twitch API!");
            mex.printStackTrace();
            return "Title not available!";
        }
        Scanner s;
        try {
            s = new Scanner(url.openStream());
        }
        catch (IOException ioex) {
            ioex.printStackTrace();
            return "Title not available";
        }
        Gson g = new Gson();
        String file = "";
        while (s.hasNextLine()) {
            file += s.nextLine();
        }
        BroadcastData broadcastData = g.fromJson(file, BroadcastData.class);
        return broadcastData.title;
    }

}
