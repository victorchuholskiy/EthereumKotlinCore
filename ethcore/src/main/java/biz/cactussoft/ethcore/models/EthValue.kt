package biz.cactussoft.ethcore.models

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by viktor.chukholskiy
 * 28/05/18.
 */
class EthValue {

	var value: BigInteger? = null
		private set

	fun inWei(): Long {
		return value!!.toLong()
	}

	fun inKWei(digitsNumber: Int): Double {
		return BigDecimal(value).divide(KWEI, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inGWei(digitsNumber: Int): Double {
		return BigDecimal(value).divide(GWEI, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inMicroEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(MICROETHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inMilliEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(MILLIETHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(ETHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inKEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(KETHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inMEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(METHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inGEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(GETHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	fun inTEther(digitsNumber: Int): Double {
		return BigDecimal(value).divide(TETHER, digitsNumber, BigDecimal.ROUND_DOWN).toDouble()
	}

	private constructor(value: BigInteger) {
		this.value = value
	}

	private constructor(value: Long) {
		this.value = BigInteger.valueOf(value)
	}

	companion object {

		private val KWEI = BigDecimal.TEN.pow(3)
		private val GWEI = BigDecimal.TEN.pow(9)
		private val MICROETHER = BigDecimal.TEN.pow(12)
		private val MILLIETHER = BigDecimal.TEN.pow(15)
		private val ETHER = BigDecimal.TEN.pow(18)
		private val KETHER = BigDecimal.TEN.pow(21)
		private val METHER = BigDecimal.TEN.pow(24)
		private val GETHER = BigDecimal.TEN.pow(27)
		private val TETHER = BigDecimal.TEN.pow(30)

		fun of(value: Long): EthValue {
			return EthValue(value)
		}

		fun of(value: BigInteger): EthValue {
			return EthValue(value)
		}

		fun ofWei(value: Long): EthValue {
			return EthValue(BigInteger.valueOf(value))
		}

		fun ofKWei(value: Long): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(KWEI).toBigInteger())
		}

		fun ofGWei(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(GWEI).toBigInteger())
		}

		fun ofMicroEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(MICROETHER).toBigInteger())
		}

		fun ofMilliEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(MILLIETHER).toBigInteger())
		}

		fun ofEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(ETHER).toBigInteger())
		}

		fun ofKEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(KETHER).toBigInteger())
		}

		fun ofMEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(METHER).toBigInteger())
		}

		fun ofGEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(GETHER).toBigInteger())
		}

		fun ofTEther(value: Double): EthValue {
			return EthValue(BigDecimal.valueOf(value).multiply(TETHER).toBigInteger())
		}
	}
}
