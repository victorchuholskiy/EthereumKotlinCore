package biz.cactussoft.ethcore.models

import org.bitcoinj.crypto.DeterministicKey

/**
 * Created by viktor.chukholskiy
 * 28/05/18.
 */
class HDWallet(var key: DeterministicKey?,
			   var address: String?,
			   var index: Int) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null) return false
		if (this::class != other::class) return false

		other as HDWallet

		if (key != other.key) return false
		if (address != other.address) return false
		if (index != other.index) return false

		return true
	}

	override fun hashCode(): Int {
		var result = key?.hashCode() ?: 0
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + index
		return result
	}
}