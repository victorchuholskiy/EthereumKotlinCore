package biz.cactussoft.ethcore;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import biz.cactussoft.ethcore.exceptions.IncorrectDerivationPathException;

/**
 * Created by viktor.chukholskiy
 * 21/09/17.
 */

public class HDWalletManager {

    private static final String DERIVATION_PATH_REGEX = "^m(/\\d+(')?)+$";
    private static final Pattern DERIVATION_PATH_PATTERN = Pattern.compile(DERIVATION_PATH_REGEX);

    /**
     * Generate new mnemonic phrase (12 words, BIP-39)
     *
     * @return generated mnemonic (list of strings, 12 words)
     */
    public static List<String> generateMnemonic() throws IOException, MnemonicException {
        byte[] randomBytes = new byte[16];
        new Random().nextBytes(randomBytes);
        return entropyToMnemonic(randomBytes);
    }

    /**
     * Mnemonic to entropy
     */
    public static byte[] mnemonicToEntropy(List<String> mnemonic) throws IOException, MnemonicException {
        MnemonicCode mnemonicCode = new MnemonicCode();
        return mnemonicCode.toEntropy(mnemonic);
    }

    /**
     * Mnemonic to entropy
     */
    public static List<String> entropyToMnemonic(byte[] entropy) throws IOException, MnemonicException {
        MnemonicCode mnemonicCode = new MnemonicCode();
        return mnemonicCode.toMnemonic(entropy);
    }

    /**
     * Generation of a seed (64 bytes) based on a mnemonic phrase and an optional passphrase (BIP-39)
     * We use the PBKDF2 function with a mnemonic sentence (in UTF-8 NFKD) used as the passphrase
     * and the string "mnemonic" + passphrase (again in UTF-8 NFKD) used as the salt
     *
     * @param mnemonic - mnemonic phrase (list, 12 words)
     * @param passphrase - optional passphrase used for seed generation.
     * @return seed, 64 bytes
     */
    public static byte[] mnemonicToSeed(List<String> mnemonic, String passphrase) throws IOException, MnemonicException {
        MnemonicCode mnemonicCode = new MnemonicCode();
        mnemonicCode.check(mnemonic);
        return MnemonicCode.toSeed(mnemonic, passphrase);
    }

    /**
     * Generation of a seed (64 bytes) based on a mnemonic phrase and an optional passphrase (BIP-39)
     *
     * @param mnemonic - mnemonic phrase (string, 12 words through a space)
     * @param passphrase - optional passphrase used for seed generation.
     * @return seed, 64 bytes
     */
    public static byte[] mnemonicToSeed(String mnemonic, String passphrase) throws IOException, MnemonicException {
        return mnemonicToSeed(Arrays.asList(mnemonic.toLowerCase().trim().replaceAll(" +", " ").split(" ")), passphrase);
    }

    /**
     * Checking the correctness of the entered mnemonics
     *
     * @param mnemonic - mnemonic phrase (list, 12 words)
     */
    public static void checkWords(List<String> mnemonic) throws IOException , MnemonicException {
        new MnemonicCode().check(mnemonic);
    }

    /**
     * Generate HD wallet master key (BIP-32)
     *
     * @param seed - seed (64 bytes, BIP39)
     * @return generated master key
     */
    public static DeterministicKey generateMasterKey(byte[] seed) {
        return HDKeyDerivation.createMasterPrivateKey(seed);
    }

    /**
     * Build derivation path (string)
     * Basic: m / purpose' / coin_type' / account' / change / address_index
     *
     * @return derivation path
     */
    public static String buildPath(int purpose, int coin, int account, int change) {
        return "m/" + purpose + "'/" + coin + "'/" + account + "'/" + change;
    }

    /**
     * Check is derivation path have right structure
     * Basic: m / purpose' / coin_type' / account' / change / address_index
     *
     * @param path - derivation path
     * @return is path correct
     */
    public static boolean isPathCorrect(String path) {
        return DERIVATION_PATH_PATTERN.matcher(path).matches();
    }

    /**
     * Convert string derivation path to list of ChildNumber
     *
     * @param path - derivation path
     * @return list of indexes
     */
    public static List<ChildNumber> convertPath(String path) throws IncorrectDerivationPathException {
        if (!isPathCorrect(path)) {
            throw new IncorrectDerivationPathException("Incorrect path");
        }
        String[] indexes = path.split("/");
        List<ChildNumber> list = new ArrayList<>();
        for (int i = 1; i < indexes.length; i++) {
            list.add(new ChildNumber(
                    Integer.valueOf(indexes[i].replace("'", "")),
                    indexes[i].contains("'"))
            );
        }
        return list;
    }

    /**
     * Convert string derivation path to list of integer
     *
     * @param path - derivation path
     * @return list of indexes (integers)
     */
    public static List<Integer> convertPathToIntArray(String path) throws IncorrectDerivationPathException {
        if (!isPathCorrect(path)) {
            throw new IncorrectDerivationPathException("Incorrect path");
        }
        String[] indexes = path.split("/");
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < indexes.length; i++) {
            list.add(Integer.valueOf(indexes[i].replace("'", "")));
        }
        return list;
    }

    /**
     * Get child deterministic key generated by master key, derivation path
     * and address index
     *
     * @param masterKey - derivation master key
     * @param path - derivation path (list)
     * @param index - address index
     * @return generated deterministic key
     */
    public static DeterministicKey getChildKey(DeterministicKey masterKey, List<ChildNumber> path, ChildNumber index) {
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);
        return hierarchy.deriveChild(path, true, true, index);
    }

    /**
     * Get child deterministic key generated by master key, derivation path
     * and address index
     *
     * @param masterKey - derivation master key
     * @param path - derivation path (string)
     * @param index - address index
     * @return generated deterministic key
     */
    public static DeterministicKey getChildKey(DeterministicKey masterKey, String path, int index) throws IncorrectDerivationPathException {
        return getChildKey(masterKey, convertPath(path), new ChildNumber(index));
    }

    /**
     * Get array of child deterministic keys generated by master key, derivation path
     * and start address index and count
     *
     * @param masterKey - derivation master key
     * @param path - derivation path (list)
     * @param startIndex - start address index
     * @param count - count of keys
     * @return generated deterministic key
     */
    public static List<DeterministicKey> getChildKeys(DeterministicKey masterKey, List<ChildNumber> path, int startIndex, int count) {
        DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);
        List<DeterministicKey> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(hierarchy.deriveChild(path, true, true, new ChildNumber(startIndex + i)));
        }
        return list;
    }

    /**
     * Get array of child deterministic keys generated by master key, derivation path
     * and start address index and count
     *
     * @param masterKey - derivation master key
     * @param path - derivation path (string)
     * @param startIndex - start address index
     * @param count - count of keys
     * @return generated deterministic key
     */
    public static List<DeterministicKey> getChildKeys(DeterministicKey masterKey, String path, int startIndex, int count) throws IncorrectDerivationPathException {
       return getChildKeys(masterKey, convertPath(path), startIndex, count);
    }
}
