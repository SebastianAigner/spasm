package twitch.rechat;

/**
 * Created by Sebastian Aigner
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This error request is used to determine start and end of the broadcast. In general, it contains a list of RechatErrors
 */
public class RechatErrorRequest {
    public List<RechatErrors> errors = new ArrayList<>();
}
