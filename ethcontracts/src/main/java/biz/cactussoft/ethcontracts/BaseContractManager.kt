package biz.cactussoft.ethcontracts

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.TransactionManager
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.util.ArrayList

/**
 * Created by viktor.chukholskiy
 * 29/05/18.
 */
open class BaseContractManager(nodeUrl: String,
						  private val mKeyStoreDir: String) {

	protected val sWeb3j: Web3j = Web3jFactory.build(HttpService(nodeUrl))
	protected val sEmptyTransactionManager =
			object : TransactionManager(sWeb3j, "") {
				@Throws(IOException::class)
				override fun sendTransaction(gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String, value: BigInteger): EthSendTransaction? {
					return null
				}

				override fun getFromAddress(): String? {
					return null
				}
			}
	/**
	 * Tries to find the key-file in the keystore folder by the address
	 *
	 * @param accountAddress - hex address of the account
	 *
	 * @return key-file if it is found (otherwise null)
	 */
	protected fun getKeyFileByAddress(accountAddress: String): File? {
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
	protected fun getListFiles(parentDir: File): List<File> {
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
		private const val ACCOUNT_PREFIX = "0x"
		private const val ACCOUNT_FIELD_NAME = "address"
		private const val KEY_FILE_FORMAT = ".json"

		private val TAG = BaseContractManager::class.java.simpleName
	}
}