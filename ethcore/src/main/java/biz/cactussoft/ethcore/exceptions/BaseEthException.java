package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class BaseEthException extends Exception {

    public BaseEthException(String message) {
        super(message);
    }

    public BaseEthException(String message, Exception e) {
        super(message, e);
    }

    public BaseEthException(Exception e) {
        super(e);
    }
}
