package biz.cactussoft.ethcore;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;
import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Transaction;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import biz.cactussoft.ethcore.exceptions.AccountNotFoundException;
import biz.cactussoft.ethcore.exceptions.EncryptionException;
import biz.cactussoft.ethcore.exceptions.EthClientNotInitException;
import biz.cactussoft.ethcore.exceptions.IncorrectDerivationPathException;
import biz.cactussoft.ethcore.exceptions.IncorrectMnemonicException;
import biz.cactussoft.ethcore.exceptions.IncorrectPassException;
import biz.cactussoft.ethcore.exceptions.IncorrectTxValueException;
import biz.cactussoft.ethcore.exceptions.NotEnoughGasException;
import biz.cactussoft.ethcore.models.ETHValue;
import biz.cactussoft.ethcore.models.HDWallet;
import biz.cactussoft.ethcore.models.TransactionData;

/**
 * Created by viktor.chukholskiy
 * 17/07/17.
 */

public class ETHManager {

    private static final String TAG = ETHManager.class.getSimpleName();

    public static final String DEFAULT_DERIVATION_PATH = "m/44'/60'/0'/0";
    private static final String DEFAULT_MNEMONIC_PASSPHRASE = "";
    private static final String HASH_KEY = "ethCore";

    private String mNodeUrl;
    private int mChainId;

    private EthereumClient sEtherClient;
    private Web3j sWeb3j;

    public ETHManager(String nodeUrl, int chainId) {
        try {
            mNodeUrl = nodeUrl;
            mChainId = chainId;
            sEtherClient = Geth.newEthereumClient(mNodeUrl);
            sWeb3j = Web3jFactory.build(new HttpService(mNodeUrl));
        } catch (Exception e) {
            sEtherClient = null;
            Log.e(TAG, "Error during the client initialization process: " + e.getMessage());
        }
    }

    /**
     * Create new account in ethereum network.
     * Return hex address of the new account.
     *
     * @param password for encryption the keystore file (will using for signing transactions)
     * @param keyStoreDir - dir with keystore files
     * @return new account address
     * @throws Exception An error occurred
     */
    public String createNewAccount(String password, String keyStoreDir) throws Exception {
        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);

        // account creation
        Account newAcc = ks.newAccount(password);
        Log.d(TAG, "New account: " + newAcc.getAddress().getHex());
        return newAcc.getAddress().getHex();
    }

    /**
     * Change account password, recoding key-file
     *
     * @param address - hex account address
     * @param oldPassword - old password
     * @param newPassword - new password
     * @param keyStoreDir - dir with keystore files
     * @return new account address
     * @throws Exception An error occurred
     */
    public String changeAccountPassword(String address, String oldPassword, String newPassword, String keyStoreDir) throws Exception {
        String url = getKeyFileURL(address, keyStoreDir);
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(oldPassword, url);
        } catch (Exception e) {
            throw new IncorrectPassException("Incorrect file pass");
        }

        WalletFile walletFile = Wallet.createLight(newPassword, credentials.getEcKeyPair());
        File destination = new File(url);
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.writeValue(destination, walletFile);
        return credentials.getAddress();
    }

    /**
     * Backup/export existed account.
     *
     * @param accountAddress - hex account address
     * @param password using for encryption the keystore file
     * @param exportPassword using for export/import the account
     * @param keyStoreDir - dir with keystore files
     * @throws Exception An error occurred
     */
    public byte[] exportAccount(String accountAddress, String password, String exportPassword, String keyStoreDir) throws Exception {
        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
        Accounts accounts = ks.getAccounts();

        Account account = null;
        for (long i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAddress().getHex().equals(accountAddress)) {
                account = accounts.get(i);
                break;
            }
        }
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountAddress + " not found. Perhaps it was not imported.");
        }

        // Account exporting
        byte[] jsonAcc = ks.exportKey(account, password, exportPassword);
        Log.d(TAG, "Json (exported account info): " + new String(jsonAcc));
        return jsonAcc;
    }

    /**
     * Import account from json file.
     *
     * @param password using for encryption the keystore file
     * @param file - original file
     * @param keyStoreDir - dir with keystore files
     * @throws IOException, CipherException An error occurred
     */
    public String importFile(String password, File file, File keyStoreDir) throws IOException, EncryptionException {
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(password, file);
        } catch (CipherException e) {
            throw new EncryptionException(e);
        }

        WalletFile walletFile;
        try {
            walletFile = Wallet.createLight(password, credentials.getEcKeyPair());
        } catch (CipherException e) {
            throw new EncryptionException(e);
        }
        File destination = new File(keyStoreDir.getAbsolutePath(), file.getName());
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.writeValue(destination, walletFile);
        return credentials.getAddress();
    }

    /**
     * Check is account imported on the device
     *
     * @param accountAddress - hex account address
     * @throws Exception An error occurred
     */
    public boolean checkIsAccountImported(String accountAddress , String keyStoreDir) throws Exception {
        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
        Accounts accounts = ks.getAccounts();

        Account account = null;
        for (long i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAddress().getHex().equals(accountAddress)) {
                account = accounts.get(i);
                break;
            }
        }
        return account != null;
    }

    public List<String> getImportedAccounts(String keyStoreDir) throws Exception {
        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
        Accounts accounts = ks.getAccounts();
        List<String> addresses = new ArrayList<>();
        for (long i = 0; i < accounts.size(); i++) {
            addresses.add(accounts.get(i).getAddress().getHex());
        }
        return addresses;
    }

    /**
     * Get key-file URL
     *
     * @param accountAddress - hex account address
     * @throws Exception An error occurred
     */
    public String getKeyFileURL(String accountAddress , String keyStoreDir) throws Exception {
        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
        Accounts accounts = ks.getAccounts();

        for (long i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAddress().getHex().equals(accountAddress)) {
                return accounts.get(i).getURL().replace("keystore:", "");
            }
        }
        return "";
    }

    /**
     * Deleting account.
     *
     * @param accountAddress - hex account address
     * @param password using for encryption the keystore file
     * @param keyStoreDir - dir with keystore files
     * @throws Exception An error occurred
     */
    public void deleteAccount(String accountAddress, String password, String keyStoreDir) throws Exception {
        if (sEtherClient == null) {
            throw new EthClientNotInitException("Ethereum client isn't initialized");
        }

        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
        Accounts accounts = ks.getAccounts();

        Account account = null;
        for (long i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAddress().getHex().equals(accountAddress)) {
                account = accounts.get(i);
                break;
            }
        }
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountAddress + " not found. Perhaps it was not imported.");
        }

        ks.deleteAccount(account, password);
    }

    /**
     * Get current account balance
     *
     * @param accountAddress - hex account address
     * @throws Exception An error occurred
     */
    public ETHValue getBalance(String accountAddress) throws Exception {
        if (sEtherClient == null) {
            throw new EthClientNotInitException("Ethereum client isn't initialized");
        }
        BigInt balanceBigInt = sEtherClient.getBalanceAt(Geth.newContext(), new Address(accountAddress), -1);
        BigDecimal bigDecimal = new BigDecimal(balanceBigInt.toString());
        return ETHValue.of(bigDecimal);
    }


    /**
     * Get last block number
     *
     * @throws Exception An error occurred
     */
    public long getLastBlockNumber() throws Exception {
        if (sEtherClient == null) {
            throw new EthClientNotInitException("Ethereum client isn't initialized");
        }
        return sEtherClient.getBlockByNumber(Geth.newContext(), -1).getNumber();
    }

    /**
     * Sending transaction
     *
     * @param info - model consisted addresses from and to and sum of the transaction
     * @param password using for encryption the keystore file
     * @param transactionData - data of the transaction
     * @param keyStoreDir - dir with keystore files
     * @throws Exception An error occurred
     */
    public void sendTransaction(TransactionData info, String password, String transactionData, ETHValue gasPrice, long gasLimit, String keyStoreDir) throws Exception {
        if (sEtherClient == null) {
            throw new EthClientNotInitException("Ethereum client isn't initialized");
        }

        KeyStore ks = new KeyStore(keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
        Accounts accounts = ks.getAccounts();

        Account accountFrom = null;
        for (long i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAddress().getHex().equals(info.getAccountFrom())) {
                accountFrom = accounts.get(i);
                break;
            }
        }

        if (accountFrom == null) {
            throw new AccountNotFoundException("Account " + info.getAccountFrom() + " not found. Perhaps it was not imported.");
        }

        Address addressTo = new Address(info.getAccountTo());

        BigInt value = new BigInt(0);
        value.setString(info.getValue().string(), 10);

        BigInt balance = sEtherClient.getBalanceAt(Geth.newContext(), accountFrom.getAddress(), -1);
        BigDecimal valueBigDecimal = new BigDecimal(value.toString());
        BigDecimal balanceBigDecimal = new BigDecimal(balance.toString());

        if (valueBigDecimal.compareTo(balanceBigDecimal) >= 0) {
            throw new IncorrectTxValueException("Value of the transaction more than current balance.");
        }

        BigInt gasPriceBigInt = new BigInt(0);
        gasPriceBigInt.setString(gasPrice.string(), 10);

        BigDecimal diff = balanceBigDecimal.subtract(valueBigDecimal);
        BigDecimal gasPriceBigDecimal = new BigDecimal(gasPriceBigInt.toString());
        BigDecimal result = diff.divide(gasPriceBigDecimal, 0, RoundingMode.DOWN);

        if (result.compareTo(new BigDecimal(gasLimit)) < 0) {
            throw new NotEnoughGasException("Not enough gas.");
        }

        byte[] data = transactionData.getBytes();
        long nonce = sEtherClient.getPendingNonceAt(new Context(), accountFrom.getAddress());
        BigInt chain = Geth.newBigInt(mChainId);
        Transaction tx = Geth.newTransaction(nonce, addressTo, value, Geth.newBigInt(gasLimit), gasPriceBigInt, data);

        // Sign a transaction with a single authorization
        ks.unlock(accountFrom, password);
        Transaction signed = ks.signTx(accountFrom, tx, chain);
        ks.lock(accountFrom.getAddress());

        sEtherClient.sendTransaction(new Context(), signed);
    }


    public ETHValue getRecommendedGasPrice() throws Exception {
        BigInt gasPrice = sEtherClient.suggestGasPrice(new Context());
        BigDecimal gasPriceBigDecimal = new BigDecimal(gasPrice.toString());
        return ETHValue.of(gasPriceBigDecimal);
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
     * @param indexes - address indexes
     * @return generated deterministic key
     */
    public List<HDWallet> getHDWallet(List<String> mnemonic, int... indexes) throws IncorrectMnemonicException, IOException,
            IncorrectDerivationPathException {
        return getHDWallet(mnemonic, DEFAULT_MNEMONIC_PASSPHRASE, DEFAULT_DERIVATION_PATH, indexes);
    }

    /**
     * Get deterministic key generated by mnemonic, passphrase, path and index
     *
     * @param mnemonic - mnemonic (12 words)
     * @param path - derivation path (string)
     * @param indexes - address indexes
     * @return generated deterministic key
     */
    public List<HDWallet> getHDWallet(List<String> mnemonic, String passphrase, String path, int... indexes) throws IncorrectMnemonicException, IOException, IncorrectDerivationPathException {
        byte[] seed;
        try {
            seed = HDWalletManager.mnemonicToSeed(mnemonic, passphrase);
        } catch (MnemonicException e) {
            throw new IncorrectMnemonicException(e);
        }
        DeterministicKey masterKey = HDWalletManager.generateMasterKey(seed);
        List<HDWallet> wallets = new ArrayList<>();
        for (int addressIndex: indexes) {
            DeterministicKey child = HDWalletManager.getChildKey(masterKey, path, addressIndex);
            Credentials credentials = Credentials.create(child.getPrivateKeyAsHex());
            wallets.add(new HDWallet(child, credentials.getAddress(), addressIndex));
        }
        return wallets;
    }

    /**
     * Get deterministic key generated by mnemonic, and index
     *
     * @param mnemonic - mnemonic (12 words)
     * @param startIndex - start address index
     * @param count - count of elements
     * @return generated deterministic key
     */
    public List<HDWallet> getConsecutiveHDWallets(List<String> mnemonic, int startIndex, int count) throws IncorrectMnemonicException, IOException,
            IncorrectDerivationPathException {
        return getConsecutiveHDWallets(mnemonic, DEFAULT_MNEMONIC_PASSPHRASE, DEFAULT_DERIVATION_PATH, startIndex, count);
    }

    /**
     * Get deterministic key generated by mnemonic, passphrase, path and index
     *
     * @param mnemonic - mnemonic (12 words)
     * @param path - derivation path (string)
     * @param startIndex - start address index
     * @param count - count of elements
     * @return generated deterministic key
     */
    public List<HDWallet> getConsecutiveHDWallets(List<String> mnemonic, String passphrase, String path, int startIndex, int count) throws IncorrectMnemonicException,
            IOException, IncorrectDerivationPathException {
        byte[] seed;
        try {
            seed = HDWalletManager.mnemonicToSeed(mnemonic, passphrase);
        } catch (MnemonicException e) {
            throw new IncorrectMnemonicException(e);
        }
        DeterministicKey masterKey = HDWalletManager.generateMasterKey(seed);
        List<DeterministicKey> childKeys = HDWalletManager.getChildKeys(masterKey, path, startIndex, count);
        List<HDWallet> wallets = new ArrayList<>();
        for (DeterministicKey key: childKeys) {
            Credentials credentials = Credentials.create(key.getPrivateKeyAsHex());
            wallets.add(new HDWallet(key, credentials.getAddress(), key.getChildNumber().getI()));
        }

        return wallets;
    }

    /**
     * Generate hash for string using HMAC-SHA256 algorithm.
     * Generated had will used for grouping wallet.
     */
    public static String generateHash(String source) {
        byte[] resBuf = generateHashBytes(source);
        return new String(Hex.encode(resBuf));
    }

    /**
     * Generate hash byte array for string using HMAC-SHA256 algorithm.
     */
    public static byte[] generateHashBytes(String source) {
        HMac hMac = new HMac(new SHA256Digest());
        byte[] resBuf = new byte[hMac.getMacSize()];

        byte[] k = HASH_KEY.getBytes();
        hMac.init(new KeyParameter(k));

        byte[] m = source.getBytes();
        hMac.update(m, 0, m.length);

        hMac.doFinal(resBuf, 0);
        return resBuf;
    }

    /**
     * Generate hash for string using HMAC-SHA256 algorithm.
     * Generated had will used for grouping wallet.
     */
    public static String generateHash(List<String> mnemonic) {
        return generateHash(mnemonicToString(mnemonic));
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
     * @param hdWallet - hd wallet
     * @param password - password for encryption of the file
     * @param keyStoreDir - key store directory (file)
     * @return address of the hd wallet
     */
    public String saveHDWallet(HDWallet hdWallet, String password, File keyStoreDir) throws EncryptionException,
            IOException {
        Credentials credentials = Credentials.create(hdWallet.getKey().getPrivateKeyAsHex());
        WalletFile walletFile;
        try {
            walletFile = Wallet.createLight(password, credentials.getEcKeyPair());
        } catch (CipherException e) {
            throw new EncryptionException(e);
        }
        File destination = new File(keyStoreDir.getAbsolutePath(), getWalletFileName(walletFile.getAddress()));
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.writeValue(destination, walletFile);
        return credentials.getAddress();
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

    private String getWalletFileName(String address) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'", Locale.getDefault());
        return dateFormat.format(new Date()) + address + ".json";
    }
}