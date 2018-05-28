package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class IncorrectPassException extends Exception {

    public IncorrectPassException(String message) {
        super(message);
    }

    public IncorrectPassException(String message, Exception e) {
        super(message, e);
    }

    public IncorrectPassException(Exception e) {
        super(e);
    }
}
