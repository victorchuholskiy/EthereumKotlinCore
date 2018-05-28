package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class IncorrectMnemonicException extends BaseEthereumException {

    public IncorrectMnemonicException(String message) {
        super(message);
    }

    public IncorrectMnemonicException(String message, Exception e) {
        super(message, e);
    }

    public IncorrectMnemonicException(Exception e) {
        super(e);
    }
}
