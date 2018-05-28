package biz.cactussoft.ethcore.models;

import org.bitcoinj.crypto.DeterministicKey;

/**
 * Created by viktor.chukholskiy
 * 21/09/17.
 */

public class HDWallet {

    private DeterministicKey key;

    private String address;

    private int index;

    public HDWallet(DeterministicKey key, String address, int index) {
        this.key = key;
        this.address = address;
        this.index = index;
    }

    public DeterministicKey getKey() {
        return key;
    }

    public void setKey(DeterministicKey key) {
        this.key = key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HDWallet hdWallet = (HDWallet) o;

        return address != null ? address.equals(hdWallet.address) : hdWallet.address == null;

    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }
}
