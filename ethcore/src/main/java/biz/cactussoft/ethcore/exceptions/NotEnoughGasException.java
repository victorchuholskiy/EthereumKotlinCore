package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class NotEnoughGasException extends BaseEthereumException {

    public NotEnoughGasException(String message) {
        super(message);
    }

    public NotEnoughGasException(String message, Exception e) {
        super(message, e);
    }

    public NotEnoughGasException(Exception e) {
        super(e);
    }
}
