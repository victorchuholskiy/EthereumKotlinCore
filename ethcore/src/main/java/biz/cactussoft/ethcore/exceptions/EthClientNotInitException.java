package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class EthClientNotInitException extends BaseEthereumException {

    public EthClientNotInitException(String message) {
        super(message);
    }

    public EthClientNotInitException(String message, Exception e) {
        super(message, e);
    }

    public EthClientNotInitException(Exception e) {
        super(e);
    }
}
