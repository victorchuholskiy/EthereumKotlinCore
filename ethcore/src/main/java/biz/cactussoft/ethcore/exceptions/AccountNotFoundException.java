package biz.cactussoft.ethcore.exceptions;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class AccountNotFoundException extends Exception {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Exception e) {
        super(message, e);
    }

    public AccountNotFoundException(Exception e) {
        super(e);
    }
}
