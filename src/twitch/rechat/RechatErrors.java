package twitch.rechat;

/**
 * Created by sebi on 27.03.16.
 */

/**
 * A rechat error is a JSON formatted error message that is sent by the server when invalid requests are sent.
 */
public class RechatErrors {
    public int status;
    public String detail;
}
