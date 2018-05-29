package biz.cactussoft.ethcontracts

import biz.cactussoft.ethcontracts.contracts.ERC223Currency
import biz.cactussoft.ethcontracts.models.TokenValue
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.exceptions.TransactionException
import org.web3j.tx.Contract
import java.io.IOException
import java.math.BigInteger
import java.util.concurrent.ExecutionException

/**
 * Created by viktor.chukholskiy
 * 29/05/18.
 */
class CurrencyContractManager (nodeUrl: String, keyStoreDir: String, contractAddress: String) : BaseContractManager(nodeUrl, keyStoreDir) {

	private val mDefContract: ERC223Currency = ERC223Currency.load("", contractAddress, sWeb3j, sEmptyTransactionManager, Contract.GAS_PRICE, Contract.GAS_LIMIT)

	/**
	 * Get token name
	 */
	@Throws(IOException::class, InterruptedException::class)
	fun getTokenNameInfo(): String {
		try {
			return mDefContract.name().value
		} catch (e: ExecutionException) {
		}
		return ""
	}

	/**
	 * Get token symbol
	 */
	@Throws(IOException::class, InterruptedException::class)
	fun getTokenSymbolInfo(): String {
		try {
			return mDefContract.symbol().value
		} catch (e: ExecutionException) {
		}
		return ""
	}

	/**
	 * Get token decimals
	 */
	@Throws(IOException::class, InterruptedException::class)
	fun getTokenDecimalsInfo(): Int {
		try {
			return mDefContract.decimals().value.toInt()
		} catch (e: ExecutionException) {
		}
		return 0
	}

	/**
	 * Updating wallet tokens balance
	 *
	 * @param contractAddress - contract address
	 * @param ownerAddress - wallet/contract owner address
	 *
	 * @return tokens balance value
	 */
	@Throws(IOException::class, InterruptedException::class, ExecutionException::class)
	fun getTokenBalance(ownerAddress: String, decimal: Int): TokenValue {
		val value = mDefContract.balanceOf(ownerAddress).value
		return TokenValue.of(value, decimal)
	}

	/**
	 * Generation of erc20 contract object, connection to real contract.
	 * Can be used for checking for validation of parameters.
	 *
	 * @param walletAddress - wallet address
	 * @param password - password for encryption of the key-file
	 * @param contractAddress - erc20 contract address
	 */
	@Throws(Exception::class)
	fun prepareERC20Contract(walletAddress: String, password: String, contractAddress: String,
							 gasPrice: BigInteger, gasLimit: Long): ERC223Currency {
		val credentials = WalletUtils.loadCredentials(password, getKeyFileByAddress(walletAddress))
		val gasLimitBigInteger = BigInteger(gasLimit.toString())
		return ERC223Currency.load("", contractAddress, sWeb3j, credentials, gasPrice, gasLimitBigInteger)
	}

	/**
	 * Sending tokens from wallet without waiting feedback
	 *
	 * @param contract - contract
	 * @param addressTo - wallet/contract address of the recipient
	 * @param value - value of tokens
	 */
	@Throws(IOException::class, InterruptedException::class, ExecutionException::class)
	fun sendTokens(contract: ERC223Currency, addressTo: String, value: TokenValue) {
		try {
			contract.transfer(addressTo, value.value)
		} catch (e: Exception) {
			throw TransactionException(e)
		}
	}
}