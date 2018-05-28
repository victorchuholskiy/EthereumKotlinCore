package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class EncryptionException extends BaseEthereumException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Exception e) {
        super(message, e);
    }

    public EncryptionException(Exception e) {
        super(e);
    }
}
