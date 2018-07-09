package biz.cactussoft.ethcontracts.models

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by viktor.chukholskiy
 * 29/05/18.
 */
class TokenValue {

	var value: BigInteger
		private set

	var decimal: Int
		private set

	fun inTokens(digitsNumber: Int): Double {
		return BigDecimal(value).divide(BigDecimal.TEN.pow(decimal), digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun string(): String {
		return value.toString()
	}

	constructor(value: BigInteger, decimal: Int) {
		this.value = value
		this.decimal = decimal
	}

	constructor(value: Long, decimal: Int) {
		this.value = BigInteger.valueOf(value)
		this.decimal = decimal
	}

	companion object {

		fun of(value: Long, decimal: Int): TokenValue {
			return TokenValue(value, decimal)
		}

		fun of(value: BigInteger, decimal: Int): TokenValue {
			return TokenValue(value, decimal)
		}

		fun ofTokens(value: Double, decimal: Int): TokenValue {
			return TokenValue(BigDecimal.valueOf(value).multiply(BigDecimal.TEN.pow(decimal)).toBigInteger(), decimal)
		}
	}
}