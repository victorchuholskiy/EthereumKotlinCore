package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class IncorrectTxValueException extends BaseEthereumException {

    public IncorrectTxValueException(String message) {
        super(message);
    }

    public IncorrectTxValueException(String message, Exception e) {
        super(message, e);
    }

    public IncorrectTxValueException(Exception e) {
        super(e);
    }
}
