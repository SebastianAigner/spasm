package io.sebi.spasm.rechat;

/**
 * Created by Sebastian Aigner
 */

/**
 * A rechat error is a JSON formatted error message that is sent by the server when invalid requests are sent.
 */
public class RechatErrors {
    public int status;
    public String detail;
}
