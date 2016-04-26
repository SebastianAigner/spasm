/**
 * Created by Sebastian Aigner
 */

/**
 * The timestamp helper provides a way to cope with the timestamps sent by the API of twitch.
 */
public class TimestampHelper {
    /**
     * Converts a timestamp with whole-second precision into a nicely human-readable representation: ##:##:##
     * @param timestamp
     * @return timestamp in human readable form
     */
    public static String secondPrecisionTimestampToString(long timestamp) {
        long hours = (int) timestamp / 3600;
        long minutes = (timestamp % 3600) / 60;
        long seconds = (timestamp % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
