/**
 * Created by sebi on 29.03.16.
 */
public class TimestampHelper {
    public static String timestampToString(long timestamp) {
        long hours = (int) timestamp / 3600;
        long minutes = (timestamp % 3600) / 60;
        long seconds = (timestamp % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
