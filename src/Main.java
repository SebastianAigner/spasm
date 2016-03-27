import com.google.gson.Gson;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sebi on 26.03.16.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        List<RechatMessage> messages = new ArrayList<>();
        String page = "";
        String videoID = "v56758120";
        long timestamp = 1459001711;
        while (true) {
            page = fetchChat(videoID, timestamp);
            Gson gson = new Gson();
            RechatBlock r = gson.fromJson(page, RechatBlock.class);
            long lasttimestamp = 0;
            for (RechatMessage m : r.data) {
                boolean shouldBeAdded = true;
                for (int i = 0; i < 10; ++i) {
                    //Iterate over the last ten messages to make sure there is no redundancy when changing timestamps
                    if (messages.size() > messages.size() - i && messages.size() - i >= 0) {
                        if (messages.get(messages.size() - i).id.equals(m.id)) {
                            shouldBeAdded = false;
                            break;
                        }
                    }
                }
                if (shouldBeAdded) {
                    messages.add(m);
                    lasttimestamp = m.attributes.timestamp;
                    System.out.println(m.attributes.message);
                }
            }
            if (lasttimestamp == 0 || lasttimestamp / 1000 == timestamp) {
                ++timestamp;
            } else {
                timestamp = lasttimestamp / 1000;
            }
        }
    }

    public static String fetchChat(String videoID, long start) throws Exception {
        String page = "";
        URL url = new URL("https://rechat.twitch.tv/rechat-messages?start=" + start + "&video_id=" + videoID);
        Scanner s = new Scanner(url.openStream());
        Gson gson = new Gson();
        while (s.hasNextLine()) {
            page += s.nextLine();
        }
        return page;
    }
}
