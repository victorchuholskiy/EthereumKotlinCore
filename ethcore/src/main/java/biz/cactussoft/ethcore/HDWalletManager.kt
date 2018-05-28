package biz.cactussoft.ethcore

import biz.cactussoft.ethcore.exceptions.IncorrectDerivationPathException
import org.bitcoinj.crypto.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * Created by viktor.chukholskiy
 * 28/05/18.
 */

class HDWalletManager {
	companion object {
		private val DERIVATION_PATH_REGEX = "^m(/\\d+(')?)+$"
		private val DERIVATION_PATH_PATTERN = Pattern.compile(DERIVATION_PATH_REGEX)

		/**
		 * Generate new mnemonic phrase (12 words, BIP-39)
		 *
		 * @return generated mnemonic (list of strings, 12 words)
		 */
		@Throws(IOException::class, MnemonicException::class)
		fun generateMnemonic(): List<String> {
			val randomBytes = ByteArray(16)
			Random().nextBytes(randomBytes)
			return entropyToMnemonic(randomBytes)
		}

		/**
		 * Mnemonic to entropy
		 */
		@Throws(IOException::class, MnemonicException::class)
		fun mnemonicToEntropy(mnemonic: List<String>): ByteArray {
			val mnemonicCode = MnemonicCode()
			return mnemonicCode.toEntropy(mnemonic)
		}

		/**
		 * Mnemonic to entropy
		 */
		@Throws(IOException::class, MnemonicException::class)
		private fun entropyToMnemonic(entropy: ByteArray): List<String> {
			val mnemonicCode = MnemonicCode()
			return mnemonicCode.toMnemonic(entropy)
		}

		/**
		 * Generation of a seed (64 bytes) based on a mnemonic phrase and an optional passphrase (BIP-39)
		 * We use the PBKDF2 function with a mnemonic sentence (in UTF-8 NFKD) used as the passphrase
		 * and the string "mnemonic" + passphrase (again in UTF-8 NFKD) used as the salt
		 *
		 * @param mnemonic   - mnemonic phrase (list, 12 words)
		 * @param passphrase - optional passphrase used for seed generation.
		 *
		 * @return seed, 64 bytes
		 */
		@Throws(IOException::class, MnemonicException::class)
		fun mnemonicToSeed(mnemonic: List<String>,
						   passphrase: String): ByteArray {
			val mnemonicCode = MnemonicCode()
			mnemonicCode.check(mnemonic)
			return MnemonicCode.toSeed(mnemonic, passphrase)
		}

		/**
		 * Generation of a seed (64 bytes) based on a mnemonic phrase and an optional passphrase (BIP-39)
		 *
		 * @param mnemonic   - mnemonic phrase (string, 12 words through a space)
		 * @param passphrase - optional passphrase used for seed generation.
		 *
		 * @return seed, 64 bytes
		 */
		@Throws(IOException::class, MnemonicException::class)
		fun mnemonicToSeed(mnemonic: String,
						   passphrase: String): ByteArray {
			return mnemonicToSeed(Arrays.asList(*mnemonic.toLowerCase().trim({ it <= ' ' }).replace(" +".toRegex(), " ").split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()), passphrase)
		}

		/**
		 * Checking the correctness of the entered mnemonics
		 *
		 * @param mnemonic - mnemonic phrase (list, 12 words)
		 */
		@Throws(IOException::class, MnemonicException::class)
		fun checkWords(mnemonic: List<String>) {
			MnemonicCode().check(mnemonic)
		}

		/**
		 * Generate HD wallet master key (BIP-32)
		 *
		 * @param seed - seed (64 bytes, BIP39)
		 *
		 * @return generated master key
		 */
		fun generateMasterKey(seed: ByteArray): DeterministicKey {
			return HDKeyDerivation.createMasterPrivateKey(seed)
		}

		/**
		 * Build derivation path (string)
		 * Basic: m / purpose' / coin_type' / account' / change / address_index
		 *
		 * @return derivation path
		 */
		fun buildPath(purpose: Int,
					  coin: Int,
					  account: Int,
					  change: Int): String {
			return "m/$purpose'/$coin'/$account'/$change"
		}

		/**
		 * Check is derivation path have right structure
		 * Basic: m / purpose' / coin_type' / account' / change / address_index
		 *
		 * @param path - derivation path
		 *
		 * @return is path correct
		 */
		private fun isPathCorrect(path: String): Boolean {
			return DERIVATION_PATH_PATTERN.matcher(path).matches()
		}

		/**
		 * Convert string derivation path to list of ChildNumber
		 *
		 * @param path - derivation path
		 *
		 * @return list of indexes
		 */
		@Throws(IncorrectDerivationPathException::class)
		private fun convertPath(path: String): List<ChildNumber> {
			if (!isPathCorrect(path)) {
				throw IncorrectDerivationPathException("Incorrect path")
			}
			val indexes = path.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
			val list = ArrayList<ChildNumber>()
			(1 until indexes.size).forEach {
				list.add(ChildNumber(
						Integer.valueOf(indexes[it].replace("'", ""))!!,
						indexes[it].contains("'"))
				)
			}
			return list
		}

		/**
		 * Convert string derivation path to list of integer
		 *
		 * @param path - derivation path
		 *
		 * @return list of indexes (integers)
		 */
		@Throws(IncorrectDerivationPathException::class)
		fun convertPathToIntArray(path: String): List<Int> {
			if (!isPathCorrect(path)) {
				throw IncorrectDerivationPathException("Incorrect path")
			}
			val indexes = path.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
			val list = ArrayList<Int>()
			(1 until indexes.size).forEach {
				list.add(Integer.valueOf(indexes[it].replace("'", "")))
			}
			return list
		}

		/**
		 * Get child deterministic key generated by master key, derivation path
		 * and address index
		 *
		 * @param masterKey - derivation master key
		 * @param path      - derivation path (list)
		 * @param index     - address index
		 *
		 * @return generated deterministic key
		 */
		private fun getChildKey(masterKey: DeterministicKey,
								path: List<ChildNumber>,
								index: ChildNumber): DeterministicKey {
			val hierarchy = DeterministicHierarchy(masterKey)
			return hierarchy.deriveChild(path, true, true, index)
		}

		/**
		 * Get child deterministic key generated by master key, derivation path
		 * and address index
		 *
		 * @param masterKey - derivation master key
		 * @param path      - derivation path (string)
		 * @param index     - address index
		 *
		 * @return generated deterministic key
		 */
		@Throws(IncorrectDerivationPathException::class)
		fun getChildKey(masterKey: DeterministicKey,
						path: String,
						index: Int): DeterministicKey {
			return getChildKey(masterKey, convertPath(path), ChildNumber(index))
		}

		/**
		 * Get array of child deterministic keys generated by master key, derivation path
		 * and start address index and count
		 *
		 * @param masterKey  - derivation master key
		 * @param path       - derivation path (list)
		 * @param startIndex - start address index
		 * @param count      - count of keys
		 *
		 * @return generated deterministic key
		 */
		private fun getChildKeys(masterKey: DeterministicKey,
								 path: List<ChildNumber>,
								 startIndex: Int,
								 count: Int): List<DeterministicKey> {
			val hierarchy = DeterministicHierarchy(masterKey)
			val list = ArrayList<DeterministicKey>()
			(0 until count).forEach {
				list.add(hierarchy.deriveChild(path, true, true, ChildNumber(startIndex + it)))
			}
			return list
		}

		/**
		 * Get array of child deterministic keys generated by master key, derivation path
		 * and start address index and count
		 *
		 * @param masterKey  - derivation master key
		 * @param path       - derivation path (string)
		 * @param startIndex - start address index
		 * @param count      - count of keys
		 *
		 * @return generated deterministic key
		 */
		@Throws(IncorrectDerivationPathException::class)
		fun getChildKeys(masterKey: DeterministicKey,
						 path: String,
						 startIndex: Int,
						 count: Int): List<DeterministicKey> {
			return getChildKeys(masterKey, convertPath(path), startIndex, count)
		}
	}
}
