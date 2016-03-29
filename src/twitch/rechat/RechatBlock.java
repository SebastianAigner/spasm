package twitch.rechat; /**
 * Created by sebi on 26.03.16.
 */

import java.util.List;

/**
 * RechatBlock represents the result of a query call to the Twitch Chat Replay API. It usually contains about 10-15
 * chat messages in a "data" list.
 */
public class RechatBlock {
    public List<RechatMessage> data;
}
