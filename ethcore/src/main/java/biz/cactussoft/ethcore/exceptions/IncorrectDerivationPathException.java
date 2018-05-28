package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class IncorrectDerivationPathException extends Exception {

    public IncorrectDerivationPathException(String message) {
        super(message);
    }

    public IncorrectDerivationPathException(String message, Exception e) {
        super(message, e);
    }

    public IncorrectDerivationPathException(Exception e) {
        super(e);
    }
}
