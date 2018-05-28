package biz.cactussoft.ethcore

import android.text.TextUtils
import android.util.Log
import biz.cactussoft.ethcore.exceptions.*
import biz.cactussoft.ethcore.models.EthValue
import biz.cactussoft.ethcore.models.HDWallet
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.bitcoinj.crypto.MnemonicException
import org.web3j.crypto.*
import org.web3j.protocol.ObjectMapperFactory
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.util.ArrayList
import java.util.concurrent.ExecutionException

/**
 * Created by viktor.chukholskiy
 * 28/05/18.
 */
class EthManager(nodeUrl: String,
				 private val mKeyStoreDir: String) {

	private val sWeb3j: Web3j = Web3jFactory.build(HttpService(nodeUrl))

	/**
	 * Create new account in ethereum network. Default algorithm using random generation.
	 * Return hex address of the new account.
	 *
	 * @param password for encryption the keystore file (will using for signing transactions)
	 *
	 * @return new account address
	 */
	@Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, EncryptionException::class, IOException::class)
	fun createNewAccount(password: String): String {
		try {
			val fileName = WalletUtils.generateLightNewWalletFile(password, File(mKeyStoreDir))
			return WalletUtils.loadCredentials(password, fileName).address
		} catch (e: CipherException) {
			throw EncryptionException(e)
		}

	}

	/**
	 * Import account from json file (check data and copy to keystore folder)
	 *
	 * @param password using for encryption the keystore file
	 * @param file     is original file
	 *
	 * @return address of the wallet
	 */
	@Throws(IOException::class, EncryptionException::class)
	fun importFile(password: String,
				   file: File): String {
		try {
			val credentials = WalletUtils.loadCredentials(password, file)
			val walletFile = Wallet.createLight(password, credentials.ecKeyPair)
			val destination = File(mKeyStoreDir, file.name)
			val objectMapper = ObjectMapperFactory.getObjectMapper()
			objectMapper.writeValue(destination, walletFile)
			return credentials.address
		} catch (e: CipherException) {
			throw EncryptionException(e)
		}

	}

	/**
	 * Check is account imported on the device
	 *
	 * @param accountAddress - hex account address
	 *
	 * @return true if a key-file is found (imported)
	 */
	fun isAccountImported(accountAddress: String): Boolean {
		return getKeyFileByAddress(accountAddress) != null
	}

	/**
	 * Check is account imported on the device
	 *
	 * @return the list of addresses of imported accounts
	 */
	val importedAccounts: List<String>
		get() {
			val addresses = ArrayList<String>()
			val files = getListFiles(File(mKeyStoreDir))
			for (keyFile in files) {
				try {
					val node = ObjectMapper().readValue(keyFile, ObjectNode::class.java)
					if (node.has("address")) {
						addresses.add(node.get("address").asText())
					}
				} catch (e: IOException) {
					Log.e(TAG, "Incorrect file " + keyFile.name)
				}

			}
			return addresses
		}

	/**
	 * Change account password, recoding key-file
	 *
	 * @param address     - hex account address
	 * @param oldPassword - old password
	 * @param newPassword - new password
	 *
	 * @return new account address
	 */
	@Throws(AccountNotFoundException::class, IncorrectPassException::class)
	fun changeAccountPassword(address: String,
							  oldPassword: String,
							  newPassword: String): String {
		val keyFile = getKeyFileByAddress(address) ?: throw AccountNotFoundException("Account $address not found. Perhaps it was not imported.")
		try {
			val credentials = WalletUtils.loadCredentials(oldPassword, keyFile)
			val walletFile = Wallet.createLight(newPassword, credentials.ecKeyPair)
			val objectMapper = ObjectMapperFactory.getObjectMapper()
			objectMapper.writeValue(keyFile.absoluteFile, walletFile)
			return credentials.address
		} catch (e: Exception) {
			throw IncorrectPassException("Incorrect file pass")
		}

	}

	/**
	 * Export existed keystore file
	 *
	 * @param accountAddress - hex account address
	 * @param password       using for encryption the keystore file
	 * @param newPassword    using for re-encryption of the keystore file (you can change the password during the wallet export)
	 * @param newDir         - destination folder
	 */
	@Throws(AccountNotFoundException::class, IOException::class, EncryptionException::class)
	fun exportAccount(accountAddress: String,
					  password: String,
					  newPassword: String,
					  newDir: String) {
		val keyFile = getKeyFileByAddress(accountAddress) ?: throw AccountNotFoundException("Account $accountAddress not found. Perhaps it was not imported.")
		try {
			val credentials = WalletUtils.loadCredentials(password, keyFile)
			val walletFile = Wallet.createLight(newPassword, credentials.ecKeyPair)
			val destination = File(newDir)
			val objectMapper = ObjectMapperFactory.getObjectMapper()
			objectMapper.writeValue(destination, walletFile)
		} catch (e: CipherException) {
			throw EncryptionException(e)
		}

	}

	/**
	 * Deleting account (key-file from key-store folder).
	 *
	 * @param accountAddress - hex account address
	 *
	 * @return true if deleting success
	 */
	fun deleteKeyFileByAddress(accountAddress: String): Boolean {
		val file = getKeyFileByAddress(accountAddress)
		return file != null && file.delete()
	}

	/**
	 * Get current account balance
	 *
	 * @param accountAddress - hex account address
	 *
	 * @return balance as ETHValue
	 */
	@Throws(InterruptedException::class, ExecutionException::class)
	fun getBalance(accountAddress: String): EthValue {
		val balance = sWeb3j.ethGetBalance(accountAddress, DefaultBlockParameterName.LATEST).sendAsync().get().balance.toString()
		return EthValue.of(BigInteger(balance))
	}

	/**
	 * Sending transaction (simple eth transaction)
	 *
	 * @param password using for encryption the keystore file
	 *
	 * @return transaction hex
	 */
	@Throws(InterruptedException::class, ExecutionException::class, IOException::class, EncryptionException::class)
	fun sendEthTransaction(addressFrom: String,
						   addressTo: String,
						   value: EthValue,
						   password: String,
						   gasPrice: EthValue,
						   gasLimit: Long): String? {
		val keyFile = getKeyFileByAddress(addressFrom)
		if (keyFile != null) {
			try {
				val ethGetTransactionCount = sWeb3j.ethGetTransactionCount(addressFrom, DefaultBlockParameterName.LATEST).sendAsync().get()

				val nonce = ethGetTransactionCount.transactionCount
				val rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice.value,
						BigInteger(gasLimit.toString()), addressTo, value.value)

				val signedMessage = TransactionEncoder.signMessage(rawTransaction, WalletUtils.loadCredentials(password, keyFile))
				val hexValue = Numeric.toHexString(signedMessage)

				val ethSendTransaction = sWeb3j.ethSendRawTransaction(hexValue).sendAsync().get()
				return ethSendTransaction.transactionHash
			} catch (e: CipherException) {
				throw EncryptionException(e)
			}

		}
		return null
	}

	/**
	 * Get recommended gas price
	 *
	 * @return recommended gas price as ETHValue
	 */
	val recommendedGasPrice: EthValue
		@Throws(IOException::class)
		get() {
			val ethGasPrice = sWeb3j.ethGasPrice().send()
			return EthValue.of(ethGasPrice.gasPrice)
		}

	/**
	 * Generate new mnemonic phrase (12 words, BIP-39)
	 *
	 * @return generated mnemonic (list of strings, 12 words)
	 */
	@Throws(IOException::class, IncorrectMnemonicException::class)
	fun generateMnemonic(): List<String> {
		try {
			return HDWalletManager.generateMnemonic()
		} catch (e: MnemonicException) {
			throw IncorrectMnemonicException(e)
		}

	}

	/**
	 * Get deterministic key generated by mnemonic, index
	 *
	 * @param mnemonic - mnemonic (12 words)
	 * @param indexes  - address indexes
	 *
	 * @return generated deterministic key
	 */
	@Throws(IncorrectMnemonicException::class, IOException::class, IncorrectDerivationPathException::class)
	fun getHDWallet(mnemonic: List<String>,
					vararg indexes: Int): List<HDWallet> {
		return getHDWallet(mnemonic, DEFAULT_MNEMONIC_PASSPHRASE, DEFAULT_DERIVATION_PATH, *indexes)
	}

	/**
	 * Get deterministic key generated by mnemonic, passphrase, path and index
	 *
	 * @param mnemonic   - mnemonic (12 words)
	 * @param passphrase - optional password using for greater safety (empty by default)
	 * @param path       - derivation path (string)
	 * @param indexes    - address indexes
	 *
	 * @return generated deterministic key
	 */
	@Throws(IncorrectMnemonicException::class, IOException::class, IncorrectDerivationPathException::class)
	fun getHDWallet(mnemonic: List<String>,
					passphrase: String,
					path: String,
					vararg indexes: Int): List<HDWallet> {
		try {
			val seed = HDWalletManager.mnemonicToSeed(mnemonic, passphrase)
			val masterKey = HDWalletManager.generateMasterKey(seed)
			val wallets = ArrayList<HDWallet>()
			for (addressIndex in indexes) {
				val child = HDWalletManager.getChildKey(masterKey, path, addressIndex)
				val credentials = Credentials.create(child.privateKeyAsHex)
				wallets.add(HDWallet(child, credentials.address, addressIndex))
			}
			return wallets
		} catch (e: MnemonicException) {
			throw IncorrectMnemonicException(e)
		}

	}

	/**
	 * Get deterministic key generated by mnemonic, and index
	 *
	 * @param mnemonic   - mnemonic (12 words)
	 * @param startIndex - start address index
	 * @param count      - count of elements
	 *
	 * @return generated deterministic keys (list of hd wallets)
	 */
	@Throws(IncorrectMnemonicException::class, IOException::class, IncorrectDerivationPathException::class)
	fun getConsecutiveHDWallets(mnemonic: List<String>,
								startIndex: Int,
								count: Int): List<HDWallet> {
		return getConsecutiveHDWallets(mnemonic, DEFAULT_MNEMONIC_PASSPHRASE, DEFAULT_DERIVATION_PATH, startIndex, count)
	}

	/**
	 * Get deterministic key generated by mnemonic, passphrase, path and index
	 *
	 * @param mnemonic   - mnemonic (12 words)
	 * @param path       - derivation path (string)
	 * @param startIndex - start address index
	 * @param count      - count of elements
	 *
	 * @return generated deterministic key
	 */
	@Throws(IncorrectMnemonicException::class, IOException::class, IncorrectDerivationPathException::class)
	fun getConsecutiveHDWallets(mnemonic: List<String>,
								passphrase: String,
								path: String,
								startIndex: Int,
								count: Int): List<HDWallet> {
		try {
			val seed = HDWalletManager.mnemonicToSeed(mnemonic, passphrase)
			val masterKey = HDWalletManager.generateMasterKey(seed)
			val childKeys = HDWalletManager.getChildKeys(masterKey, path, startIndex, count)
			val wallets = ArrayList<HDWallet>()
			for (key in childKeys) {
				val credentials = Credentials.create(key.privateKeyAsHex)
				wallets.add(HDWallet(key, credentials.address, key.childNumber.i))
			}
			return wallets
		} catch (e: MnemonicException) {
			throw IncorrectMnemonicException(e)
		}

	}

	/**
	 * Generate hd wallet key-file
	 *
	 * @param hdWallet    - hd wallet
	 * @param password    - password for encryption of the file
	 * @param keyStoreDir - key store directory (file)
	 *
	 * @return address of the hd wallet
	 */
	@Throws(EncryptionException::class, IOException::class)
	fun saveHDWallet(hdWallet: HDWallet,
					 password: String,
					 keyStoreDir: File): String {
		val credentials = Credentials.create(hdWallet.key!!.privateKeyAsHex)
		val destination = File(keyStoreDir.absolutePath)
		try {
			WalletUtils.generateWalletFile(password, credentials.ecKeyPair, destination, false)
			return credentials.address
		} catch (e: CipherException) {
			throw EncryptionException(e)
		}

	}

	/**
	 * Tries to find the key-file in the keystore folder by the address
	 *
	 * @param accountAddress - hex address of the account
	 *
	 * @return key-file if it is found (otherwise null)
	 */
	private fun getKeyFileByAddress(accountAddress: String): File? {
		var address = accountAddress
		val files = getListFiles(File(mKeyStoreDir))
		if (address.startsWith(ACCOUNT_PREFIX)) {
			address = address.substring(ACCOUNT_PREFIX.length)
		}
		for (keyFile in files) {
			try {
				val node = ObjectMapper().readValue(keyFile, ObjectNode::class.java)
				if (node.has(ACCOUNT_FIELD_NAME)) {
					if (address.equals(node.get(ACCOUNT_FIELD_NAME).asText(), ignoreCase = true)) {
						return keyFile
					}
				}
			} catch (e: IOException) {
				Log.e(TAG, "Incorrect file " + keyFile.name)
			}

		}
		return null
	}

	/**
	 * Provides a list of all json files in the keystore folder
	 *
	 * @param parentDir - keystore folder
	 *
	 * @return list of json files
	 */
	private fun getListFiles(parentDir: File): List<File> {
		val inFiles = ArrayList<File>()
		val files = parentDir.listFiles()
		for (file in files) {
			if (file.isDirectory) {
				inFiles.addAll(getListFiles(file))
			} else {
				if (file.name.endsWith(KEY_FILE_FORMAT)) {
					inFiles.add(file)
				}
			}
		}
		return inFiles
	}

	companion object {

		val DEFAULT_DERIVATION_PATH = "m/44'/60'/0'/0"

		private val TAG = EthManager::class.java.simpleName

		private val DEFAULT_MNEMONIC_PASSPHRASE = ""
		private val ACCOUNT_PREFIX = "0x"
		private val ACCOUNT_FIELD_NAME = "address"
		private val KEY_FILE_FORMAT = ".json"

		/**
		 * Convert mnemonic words list to string
		 */
		fun mnemonicToString(mnemonic: List<String>): String {
			return TextUtils.join(" ", mnemonic)
		}

		/**
		 * Checking the correctness of the entered mnemonics
		 *
		 * @param mnemonic - mnemonic phrase (list, 12 words)
		 */
		@Throws(IOException::class, IncorrectMnemonicException::class)
		fun checkMnemonic(mnemonic: List<String>) {
			try {
				HDWalletManager.checkWords(mnemonic)
			} catch (e: MnemonicException) {
				throw IncorrectMnemonicException(e)
			}

		}
	}
}