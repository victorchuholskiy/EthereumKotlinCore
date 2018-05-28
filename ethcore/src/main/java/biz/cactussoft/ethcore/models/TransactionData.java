package biz.cactussoft.ethcore.models;


/**
 * Created by viktor.chukholskiy
 * 07/07/17.
 */

public class TransactionData {

    private String accountFrom;
    private String accountTo;
    private ETHValue value;

    public TransactionData(String accountFrom, String accountTo, ETHValue value) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.value = value;
    }

    public String getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(String accountFrom) {
        this.accountFrom = accountFrom;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(String accountTo) {
        this.accountTo = accountTo;
    }

    public ETHValue getValue() {
        return value;
    }

    public void setValue(ETHValue value) {
        this.value = value;
    }
}
