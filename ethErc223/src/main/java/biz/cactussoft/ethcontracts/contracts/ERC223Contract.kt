package biz.cactussoft.ethcontracts.contracts

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import java.io.IOException
import java.math.BigInteger
import java.util.*

/**
 * Created by viktor.chukholskiy
 * 29/05/18.
 */
class ERC223Contract : Contract {

	private constructor(contractBinary: String,
						contractAddress: String,
						web3j: Web3j,
						credentials: Credentials,
						gasPrice: BigInteger,
						gasLimit: BigInteger) : super(contractBinary, contractAddress, web3j, credentials, gasPrice, gasLimit)

	private constructor(contractBinary: String,
						contractAddress: String,
						web3j: Web3j,
						transactionManager: TransactionManager,
						gasPrice: BigInteger,
						gasLimit: BigInteger) : super(contractBinary, contractAddress, web3j, transactionManager, gasPrice, gasLimit)

	@Throws(IOException::class)
	fun totalSupply(): Uint256 {
		val name = "totalSupply"
		val input = emptyList<Type<Any>>()
		val output = listOf(object : TypeReference<Uint256>() {})
		return executeCallSingleValueReturn(Function(name, input, output))
	}

	@Throws(IOException::class)
	fun name(): Utf8String {
		val name = "name"
		val input = emptyList<Type<Any>>()
		val output = listOf(object : TypeReference<Utf8String>() {})
		return executeCallSingleValueReturn(Function(name, input, output))
	}

	@Throws(IOException::class)
	fun symbol(): Utf8String {
		val name = "symbol"
		val input = emptyList<Type<Any>>()
		val output = listOf(object : TypeReference<Utf8String>() {})
		return executeCallSingleValueReturn(Function(name, input, output))
	}

	@Throws(IOException::class)
	fun decimals(): Uint8 {
		val name = "decimals"
		val input = emptyList<Type<Any>>()
		val output = listOf(object : TypeReference<Uint8>() {})
		return executeCallSingleValueReturn(Function(name, input, output))
	}

	@Throws(IOException::class)
	fun balanceOf(owner: String): Uint256 {
		val name = "balanceOf"
		val input = Arrays.asList(Address(owner))
		val output = listOf(object : TypeReference<Uint256>() {})
		return executeCallSingleValueReturn(Function(name, input as List<Type<String>>, output))
	}

	@Throws(InterruptedException::class, IOException::class)
	fun transfer(to: String, value: BigInteger): TransactionReceipt {
		val name = "transfer"
		val input = Arrays.asList(Address(to), Uint256(value))
		val output = emptyList<TypeReference<out Type<Any>>>()
		return executeTransaction(Function(name, input, output))
	}

	companion object {

		fun load(contractBinary: String, contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): ERC223Contract {
			return ERC223Contract(contractBinary, contractAddress, web3j, credentials, gasPrice, gasLimit)
		}

		fun load(contractBinary: String, contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): ERC223Contract {
			return ERC223Contract(contractBinary, contractAddress, web3j, transactionManager, gasPrice, gasLimit)
		}
	}
}