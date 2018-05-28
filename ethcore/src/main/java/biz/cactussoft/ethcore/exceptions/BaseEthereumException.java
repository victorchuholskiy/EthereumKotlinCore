package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class BaseEthereumException extends Exception {

    public BaseEthereumException(String message) {
        super(message);
    }

    public BaseEthereumException(String message, Exception e) {
        super(message, e);
    }

    public BaseEthereumException(Exception e) {
        super(e);
    }
}
