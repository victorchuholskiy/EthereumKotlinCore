package biz.cactussoft.ethcore.models;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by viktor.chukholskiy
 * 27/07/17.
 */

public class EthValue {

	private static final BigDecimal KWEI = BigDecimal.TEN.pow(3);
	private static final BigDecimal GWEI = BigDecimal.TEN.pow(9);
	private static final BigDecimal MICROETHER = BigDecimal.TEN.pow(12);
	private static final BigDecimal MILLIETHER = BigDecimal.TEN.pow(15);
	private static final BigDecimal ETHER = BigDecimal.TEN.pow(18);
	private static final BigDecimal KETHER = BigDecimal.TEN.pow(21);
	private static final BigDecimal METHER = BigDecimal.TEN.pow(24);
	private static final BigDecimal GETHER = BigDecimal.TEN.pow(27);
	private static final BigDecimal TETHER = BigDecimal.TEN.pow(30);

	private BigInteger value;

	public static EthValue of(long value) {
		return new EthValue(value);
	}

	public static EthValue of(BigInteger value) {
		return new EthValue(value);
	}

	public static EthValue ofWei(long value) {
		return new EthValue(BigInteger.valueOf(value));
	}

	public static EthValue ofKWei(long value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(KWEI).toBigInteger());
	}

	public static EthValue ofGWei(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(GWEI).toBigInteger());
	}

	public static EthValue ofMicroEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(MICROETHER).toBigInteger());
	}

	public static EthValue ofMilliEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(MILLIETHER).toBigInteger());
	}

	public static EthValue ofEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(ETHER).toBigInteger());
	}

	public static EthValue ofKEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(KETHER).toBigInteger());
	}

	public static EthValue ofMEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(METHER).toBigInteger());
	}

	public static EthValue ofGEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(GETHER).toBigInteger());
	}

	public static EthValue ofTEther(double value) {
		return new EthValue(BigDecimal.valueOf(value).multiply(TETHER).toBigInteger());
	}

	public long inWei() {
		return value.longValue();
	}

	public double inKWei(int digitsNumber) {
		return new BigDecimal(value).divide(KWEI, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	private EthValue(BigInteger value) {
		this.value = value;
	}

	private EthValue(long value) {
		this.value = BigInteger.valueOf(value);
	}

	public double inGWei(int digitsNumber) {
		return new BigDecimal(value).divide(GWEI, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inMicroEther(int digitsNumber) {
		return new BigDecimal(value).divide(MICROETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inMilliEther(int digitsNumber) {
		return new BigDecimal(value).divide(MILLIETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inEther(int digitsNumber) {
		return new BigDecimal(value).divide(ETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inKEther(int digitsNumber) {
		return new BigDecimal(value).divide(KETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inMEther(int digitsNumber) {
		return new BigDecimal(value).divide(METHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inGEther(int digitsNumber) {
		return new BigDecimal(value).divide(GETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public double inTEther(int digitsNumber) {
		return new BigDecimal(value).divide(TETHER, digitsNumber, BigDecimal.ROUND_DOWN).doubleValue();
	}

	public BigInteger getValue() {
		return value;
	}
}
