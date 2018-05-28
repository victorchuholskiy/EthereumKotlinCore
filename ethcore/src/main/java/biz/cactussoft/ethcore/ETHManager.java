package biz.cactussoft.ethcore;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import biz.cactussoft.ethcore.exceptions.AccountNotFoundException;
import biz.cactussoft.ethcore.exceptions.EncryptionException;
import biz.cactussoft.ethcore.exceptions.IncorrectDerivationPathException;
import biz.cactussoft.ethcore.exceptions.IncorrectMnemonicException;
import biz.cactussoft.ethcore.exceptions.IncorrectPassException;
import biz.cactussoft.ethcore.models.ETHValue;
import biz.cactussoft.ethcore.models.HDWallet;

/**
 * Created by viktor.chukholskiy
 * 17/07/17.
 */

public class ETHManager {

	public static final String DEFAULT_DERIVATION_PATH = "m/44'/60'/0'/0";

	private static final String TAG = ETHManager.class.getSimpleName();

	private static final String DEFAULT_MNEMONIC_PASSPHRASE = "";
	private static final String ACCOUNT_PREFIX = "0x";
	private static final String ACCOUNT_FIELD_NAME = "address";
	private static final String KEY_FILE_FORMAT = ".json";

	private Web3j sWeb3j;
	private String mKeyStoreDir;

	public ETHManager(String nodeUrl,
					  String keyStoreDir) {
		mKeyStoreDir = keyStoreDir;
		sWeb3j = Web3jFactory.build(new HttpService(nodeUrl));
	}

	/**
	 * Create new account in ethereum network.
	 * Return hex address of the new account.
	 *
	 * @param password for encryption the keystore file (will using for signing transactions)
	 * @return new account address
	 */
	public String createNewAccount(String password) throws NoSuchAlgorithmException, NoSuchProviderException,
			InvalidAlgorithmParameterException, EncryptionException, IOException {
		try {
			final String fileName = WalletUtils.generateLightNewWalletFile(password, new File(mKeyStoreDir));
			return WalletUtils.loadCredentials(password, fileName).getAddress();
		} catch (CipherException e) {
			throw new EncryptionException(e);
		}
	}

	/**
	 * Change account password, recoding key-file
	 *
	 * @param address     - hex account address
	 * @param oldPassword - old password
	 * @param newPassword - new password
	 * @return new account address
	 */
	public String changeAccountPassword(String address,
										String oldPassword,
										String newPassword) throws AccountNotFoundException, IncorrectPassException {
		final File keyFile = getKeyFileByAddress(address);
		if (keyFile == null) {
			throw new AccountNotFoundException("Account " + address + " not found. Perhaps it was not imported.");
		}
		try {
			final Credentials credentials = WalletUtils.loadCredentials(oldPassword, keyFile);
			final WalletFile walletFile = Wallet.createLight(newPassword, credentials.getEcKeyPair());
			final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
			objectMapper.writeValue(keyFile.getAbsoluteFile(), walletFile);
			return credentials.getAddress();
		} catch (Exception e) {
			throw new IncorrectPassException("Incorrect file pass");
		}
	}

	/**
	 * Backup/export existed account.
	 *
	 * @param accountAddress - hex account address
	 * @param password       using for encryption the keystore file
	 * @param newPassword    using for export/import the account
	 */
	public void exportAccount(String accountAddress,
							  String password,
							  String newPassword,
							  String newDir) throws AccountNotFoundException, IOException, EncryptionException {
		final File keyFile = getKeyFileByAddress(accountAddress);
		if (keyFile == null) {
			throw new AccountNotFoundException("Account " + accountAddress + " not found. Perhaps it was not imported.");
		}
		try {
			final Credentials credentials = WalletUtils.loadCredentials(password, keyFile);
			final WalletFile walletFile = Wallet.createLight(newPassword, credentials.getEcKeyPair());
			final File destination = new File(newDir);
			final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
			objectMapper.writeValue(destination, walletFile);
		} catch (CipherException e) {
			throw new EncryptionException(e);
		}
	}

	/**
	 * Import account from json file.
	 *
	 * @param password using for encryption the keystore file
	 * @param file     - original file
	 * @throws IOException, CipherException An error occurred
	 */
	public String importFile(String password,
							 File file) throws IOException, EncryptionException {
		try {
			final Credentials credentials = WalletUtils.loadCredentials(password, file);
			final WalletFile walletFile = Wallet.createLight(password, credentials.getEcKeyPair());
			final File destination = new File(mKeyStoreDir, file.getName());
			final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
			objectMapper.writeValue(destination, walletFile);
			return credentials.getAddress();
		} catch (CipherException e) {
			throw new EncryptionException(e);
		}
	}

	/**
	 * Check is account imported on the device
	 *
	 * @param accountAddress - hex account address
	 */
	public boolean isAccountImported(String accountAddress) {
		return getKeyFileByAddress(accountAddress) != null;
	}

	public List<String> getImportedAccounts() {
		final List<String> addresses = new ArrayList<>();
		final List<File> files = getListFiles(new File(mKeyStoreDir));
		for (File keyFile : files) {
			try {
				final ObjectNode node = new ObjectMapper().readValue(keyFile, ObjectNode.class);
				if (node.has("address")) {
					addresses.add(node.get("address").asText());
				}
			} catch (IOException e) {
				Log.e(TAG, "Incorrect file " + keyFile.getName());
			}
		}
		return addresses;
	}

	/**
	 * Deleting account.
	 *
	 * @param accountAddress - hex account address
	 */
	public boolean deleteKeyFileByAddress(String accountAddress) {
		final File file = getKeyFileByAddress(accountAddress);
		return file != null && file.delete();
	}

	/**
	 * Get current account balance
	 *
	 * @param accountAddress - hex account address
	 */
	public ETHValue getBalance(String accountAddress) throws InterruptedException, ExecutionException {
		final String balance = sWeb3j.ethGetBalance(accountAddress, DefaultBlockParameterName.LATEST).sendAsync().get().getBalance().toString();
		return ETHValue.of(new BigInteger(balance));
	}

	/**
	 * Sending transaction
	 *
	 * @param password using for encryption the keystore file
	 */
	public String sendEthTransaction(String addressFrom,
									 String addressTo,
									 ETHValue value,
									 String password,
									 ETHValue gasPrice,
									 long gasLimit) throws InterruptedException, ExecutionException, IOException, EncryptionException {
		final File keyFile = getKeyFileByAddress(addressFrom);
		if (keyFile != null) {
			try {
				final EthGetTransactionCount ethGetTransactionCount = sWeb3j.ethGetTransactionCount(addressFrom, DefaultBlockParameterName.LATEST).sendAsync().get();

				final BigInteger nonce = ethGetTransactionCount.getTransactionCount();
				final RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice.getValue(),
						new BigInteger(String.valueOf(gasLimit)), addressTo, value.getValue());

				final byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, WalletUtils.loadCredentials(password, keyFile));
				final String hexValue = Numeric.toHexString(signedMessage);

				final EthSendTransaction ethSendTransaction = sWeb3j.ethSendRawTransaction(hexValue).sendAsync().get();
				return ethSendTransaction.getTransactionHash();
			} catch (CipherException e) {
				throw new EncryptionException(e);
			}
		}
		return null;
	}


	public ETHValue getRecommendedGasPrice() throws IOException {
		final EthGasPrice ethGasPrice = sWeb3j.ethGasPrice().send();
		return ETHValue.of(ethGasPrice.getGasPrice());
	}

	/**
	 * Generate new mnemonic phrase (12 words, BIP-39)
	 *
	 * @return generated mnemonic (list of strings, 12 words)
	 */
	public List<String> generateMnemonic() throws IOException, IncorrectMnemonicException {
		try {
			return HDWalletManager.generateMnemonic();
		} catch (MnemonicException e) {
			throw new IncorrectMnemonicException(e);
		}
	}

	/**
	 * Get deterministic key generated by mnemonic, index
	 *
	 * @param mnemonic - mnemonic (12 words)
	 * @param indexes  - address indexes
	 * @return generated deterministic key
	 */
	public List<HDWallet> getHDWallet(List<String> mnemonic,
									  int... indexes) throws IncorrectMnemonicException, IOException,
			IncorrectDerivationPathException {
		return getHDWallet(mnemonic, DEFAULT_MNEMONIC_PASSPHRASE, DEFAULT_DERIVATION_PATH, indexes);
	}

	/**
	 * Get deterministic key generated by mnemonic, passphrase, path and index
	 *
	 * @param mnemonic - mnemonic (12 words)
	 * @param path     - derivation path (string)
	 * @param indexes  - address indexes
	 * @return generated deterministic key
	 */
	public List<HDWallet> getHDWallet(List<String> mnemonic,
									  String passphrase,
									  String path,
									  int... indexes) throws IncorrectMnemonicException, IOException, IncorrectDerivationPathException {
		try {
			final byte[] seed = HDWalletManager.mnemonicToSeed(mnemonic, passphrase);
			final DeterministicKey masterKey = HDWalletManager.generateMasterKey(seed);
			final List<HDWallet> wallets = new ArrayList<>();
			for (int addressIndex : indexes) {
				final DeterministicKey child = HDWalletManager.getChildKey(masterKey, path, addressIndex);
				final Credentials credentials = Credentials.create(child.getPrivateKeyAsHex());
				wallets.add(new HDWallet(child, credentials.getAddress(), addressIndex));
			}
			return wallets;
		} catch (MnemonicException e) {
			throw new IncorrectMnemonicException(e);
		}
	}

	/**
	 * Get deterministic key generated by mnemonic, and index
	 *
	 * @param mnemonic   - mnemonic (12 words)
	 * @param startIndex - start address index
	 * @param count      - count of elements
	 * @return generated deterministic key
	 */
	public List<HDWallet> getConsecutiveHDWallets(List<String> mnemonic,
												  int startIndex,
												  int count) throws IncorrectMnemonicException, IOException,
			IncorrectDerivationPathException {
		return getConsecutiveHDWallets(mnemonic, DEFAULT_MNEMONIC_PASSPHRASE, DEFAULT_DERIVATION_PATH, startIndex, count);
	}

	/**
	 * Get deterministic key generated by mnemonic, passphrase, path and index
	 *
	 * @param mnemonic   - mnemonic (12 words)
	 * @param path       - derivation path (string)
	 * @param startIndex - start address index
	 * @param count      - count of elements
	 * @return generated deterministic key
	 */
	public List<HDWallet> getConsecutiveHDWallets(List<String> mnemonic,
												  String passphrase,
												  String path,
												  int startIndex,
												  int count) throws IncorrectMnemonicException,
			IOException, IncorrectDerivationPathException {
		try {
			final byte[] seed = HDWalletManager.mnemonicToSeed(mnemonic, passphrase);
			final DeterministicKey masterKey = HDWalletManager.generateMasterKey(seed);
			final List<DeterministicKey> childKeys = HDWalletManager.getChildKeys(masterKey, path, startIndex, count);
			final List<HDWallet> wallets = new ArrayList<>();
			for (DeterministicKey key : childKeys) {
				final Credentials credentials = Credentials.create(key.getPrivateKeyAsHex());
				wallets.add(new HDWallet(key, credentials.getAddress(), key.getChildNumber().getI()));
			}
			return wallets;
		} catch (MnemonicException e) {
			throw new IncorrectMnemonicException(e);
		}
	}

	/**
	 * Convert mnemonic words list to string
	 */
	public static String mnemonicToString(List<String> mnemonic) {
		return TextUtils.join(" ", mnemonic);
	}

	/**
	 * Generate hd wallet key-file
	 *
	 * @param hdWallet    - hd wallet
	 * @param password    - password for encryption of the file
	 * @param keyStoreDir - key store directory (file)
	 * @return address of the hd wallet
	 */
	public String saveHDWallet(HDWallet hdWallet,
							   String password,
							   File keyStoreDir) throws EncryptionException,
			IOException {
		final Credentials credentials = Credentials.create(hdWallet.getKey().getPrivateKeyAsHex());
		final File destination = new File(keyStoreDir.getAbsolutePath());
		try {
			WalletUtils.generateWalletFile(password, credentials.getEcKeyPair(), destination, false);
			return credentials.getAddress();
		} catch (CipherException e) {
			throw new EncryptionException(e);
		}
	}

	/**
	 * Checking the correctness of the entered mnemonics
	 *
	 * @param mnemonic - mnemonic phrase (list, 12 words)
	 */
	public static void checkWords(List<String> mnemonic) throws IOException, IncorrectMnemonicException {
		try {
			HDWalletManager.checkWords(mnemonic);
		} catch (MnemonicException e) {
			throw new IncorrectMnemonicException(e);
		}
	}

	private File getKeyFileByAddress(@Nonnull String address) {
		final List<File> files = getListFiles(new File(mKeyStoreDir));
		if (address.startsWith(ACCOUNT_PREFIX)) {
			address = address.substring(ACCOUNT_PREFIX.length());
		}
		for (File keyFile : files) {
			try {
				final ObjectNode node = new ObjectMapper().readValue(keyFile, ObjectNode.class);
				if (node.has(ACCOUNT_FIELD_NAME)) {
					if (address.equalsIgnoreCase(node.get(ACCOUNT_FIELD_NAME).asText())) {
						return keyFile;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "Incorrect file " + keyFile.getName());
			}
		}
		return null;
	}

	private List<File> getListFiles(File parentDir) {
		final ArrayList<File> inFiles = new ArrayList<>();
		final File[] files = parentDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				inFiles.addAll(getListFiles(file));
			} else {
				if (file.getName().endsWith(KEY_FILE_FORMAT)) {
					inFiles.add(file);
				}
			}
		}
		return inFiles;
	}
}