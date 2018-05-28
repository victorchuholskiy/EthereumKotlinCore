package biz.cactussoft.ethcore.models;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class ETHValue {
    private static final BigDecimal KWEI = BigDecimal.TEN.pow(3);
    private static final BigDecimal GWEI = BigDecimal.TEN.pow(9);
    private static final BigDecimal MICROETHER = BigDecimal.TEN.pow(12);
    private static final BigDecimal MILLIETHER = BigDecimal.TEN.pow(15);
    private static final BigDecimal ETHER = BigDecimal.TEN.pow(18);
    private static final BigDecimal KETHER = BigDecimal.TEN.pow(21);
    private static final BigDecimal METHER = BigDecimal.TEN.pow(24);
    private static final BigDecimal GETHER = BigDecimal.TEN.pow(27);
    private static final BigDecimal TETHER = BigDecimal.TEN.pow(30);

    BigDecimal value;

    ETHValue(BigDecimal value) {
        this.value = value;
    }

    ETHValue(long value) {
        this.value = BigDecimal.valueOf(value);
    }

    public static ETHValue of(long value) {
        return new ETHValue(value);
    }

    public static ETHValue of(BigDecimal value) {
        return new ETHValue(value);
    }

    public static ETHValue ofWei(long value) {
        return new ETHValue(BigDecimal.valueOf(value));
    }

    public static ETHValue ofKWei(long value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(KWEI));
    }

    public static ETHValue ofGWei(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(GWEI));
    }

    public static ETHValue ofMicroEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(MICROETHER));
    }

    public static ETHValue ofMilliEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(MILLIETHER));
    }

    public static ETHValue ofEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(ETHER));
    }

    public static ETHValue ofKEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(KETHER));
    }

    public static ETHValue ofMEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(METHER));
    }

    public static ETHValue ofGEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(GETHER));
    }

    public static ETHValue ofTEther(double value) {
        return new ETHValue(BigDecimal.valueOf(value).multiply(TETHER));
    }

    public long inWei() {
        return this.value.longValue();
    }

    public double inKWei(int digitsNumber) {
        return this.value.divide(KWEI, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inGWei(int digitsNumber) {
        return this.value.divide(GWEI, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inMicroEther(int digitsNumber) {
        return this.value.divide(MICROETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inMilliEther(int digitsNumber) {
        return this.value.divide(MILLIETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inEther(int digitsNumber) {
        return this.value.divide(ETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inKEther(int digitsNumber) {
        return this.value.divide(KETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inMEther(int digitsNumber) {
        return this.value.divide(METHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inGEther(int digitsNumber) {
        return this.value.divide(GETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public double inTEther(int digitsNumber) {
        return this.value.divide(TETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public BigInteger getBigInteger() {
        String decimalValue = value.toString();
        if (decimalValue.contains(".")) {
            decimalValue = decimalValue.substring(0, decimalValue.indexOf("."));
        }
        return new BigInteger(decimalValue);
    }

    public String string() {
        return this.value.toString();
    }
}
