package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class SendingTxTimeoutException extends BaseEthereumException {

    public SendingTxTimeoutException(String message) {
        super(message);
    }

    public SendingTxTimeoutException(String message, Exception e) {
        super(message, e);
    }

    public SendingTxTimeoutException(Exception e) {
        super(e);
    }
}
