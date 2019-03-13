package global;

import org.pivxj.core.Address;
import org.pivxj.core.InsufficientMoneyException;
import org.pivxj.core.Peer;
import org.pivxj.core.Sha256Hash;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutput;
import org.pivxj.crypto.DeterministicKey;
import org.pivxj.crypto.MnemonicException;
import org.pivxj.wallet.DeterministicKeyChain;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import global.exceptions.CantSweepBalanceException;
import global.exceptions.ContactAlreadyExistException;
import global.exceptions.NoPeerConnectedException;
import global.exceptions.UpgradeException;
import global.wrappers.InputWrapper;
import wallet.Coin;
import wallet.WalletManager;
import wallet.exceptions.CantRestoreEncryptedWallet;
import wallet.exceptions.InsufficientInputsException;
import wallet.exceptions.TxNotFoundException;

/**
 * Created by mati on 18/04/17.
 */

public interface UloModule {

    /**
     * Initialize the module
     */
    void start() throws IOException;

    /**
     * ...
     */
    void createWallet() throws WalletManager.WalletErrorException;

    void savePinCode(String pinCode);

    void addNode(String ip, int port) throws WalletManager.WalletErrorException;

    void upDatePinCode(String oldpinCode,String newpinCode) throws WalletManager.WalletErrorException;
    boolean backupWallet() ;

    /**
     *
     *
     * @param backupFile
     */
    void restoreWallet(File backupFile) throws IOException;

    void restoreWalletFromEncrypted(File file, String password) throws CantRestoreEncryptedWallet, IOException;

    void restoreWallet(List<String> mnemonic) throws IOException, MnemonicException;

    /**
     * If the wallet already exist
     * @return
     */
    boolean isWalletCreated();

    /**
     * Return a new address.
     */
    String getReceiveAddress();

    Address getFreshNewAddress();

    boolean isAddressUsed(Address address);

    long getAvailableBalance();

    Coin getAvailableBalanceCoin();

    Coin getALLBalanceCoin();
    Coin getUnnavailableBalanceCoin();

    boolean isWalletWatchOnly();

    BigDecimal getAvailableBalanceLocale();

    /******    Address Label          ******/

    List<AddressLabel> getContacts();

    AddressLabel getAddressLabel(String address);

    List<AddressLabel> getMyAddresses();

    void saveContact(AddressLabel addressLabel) throws ContactAlreadyExistException;

    void saveContactIfNotExist(AddressLabel addressLabel);

    void deleteAddressLabel(AddressLabel data);


    /******   End Address Label          ******/


    boolean chechAddress(String addressBase58);

//    Transaction buildSendTx(String addressBase58, Coin amount, String memo, Address changeAddress) throws InsufficientMoneyException;
//    Transaction buildSendTx(String addressBase58, Coin amount, Coin feePerKb, String memo, Address changeAddress) throws InsufficientMoneyException;

    WalletConfiguration getConf();

    List<wallet.transaction.Transaction> listTx();

//    Coin getValueSentFromMe(Transaction transaction, boolean excludeChangeAddress);

     boolean checkTransactionChanged();
    void commitTx(Transaction transaction);

    List<Peer> listConnectedPeers();

    long getChainHeight();

    PivxRate getRate(String selectedRateCoin);

    List<InputWrapper> listUnspentWrappers();

    Set<InputWrapper> convertFrom(List<TransactionInput> list) throws TxNotFoundException;

    Transaction getTx(Sha256Hash txId);

    List<String> getMnemonic();

    String getWatchingPubKey();
    DeterministicKey getWatchingKey();

    DeterministicKey getKeyPairForAddress(Address address);

    TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) throws TxNotFoundException;

    List<TransactionOutput> getRandomUnspentNotInListToFullCoins(List<TransactionInput> inputs, Coin amount) throws InsufficientInputsException;

//    Transaction completeTx(Transaction transaction, Address changeAddress, Coin fee) throws InsufficientMoneyException;
//    Transaction completeTx(Transaction transaction) throws InsufficientMoneyException;
//
//    Transaction completeTxWithCustomFee(Transaction transaction, Coin fee) throws InsufficientMoneyException;

//    Coin getUnspentValue(Sha256Hash parentTransactionHash, int index);

    boolean isAnyPeerConnected();

    long getConnectedPeerHeight();

    int getProtocolVersion();

    void checkMnemonic(List<String> mnemonic) throws MnemonicException;

    boolean isSyncWithNode() throws NoPeerConnectedException;

    void watchOnlyMode(String xpub, DeterministicKeyChain.KeyChainType keyChainType) throws IOException;

    boolean isBip32Wallet();

    boolean sweepBalanceToNewSchema() throws InsufficientMoneyException, CantSweepBalanceException;

    boolean upgradeWallet(String upgradeCode) throws UpgradeException;

    List<PivxRate> listRates();

    List<String> getAvailableMnemonicWordsList();

//    /**
//     * Encrypt the wallet
//     * @param password
//     * @return
//     */
//    boolean encrypt(String password) throws UnsupportedEncodingException;
//    boolean decrypt(String password) throws UnsupportedEncodingException;
//    boolean isWalletLocked();
    void saveRate(PivxRate pivxRate);


    long getConnectStatus();
    String createTransaction(long amount,String addr, String des);

    wallet.Wallet getWallet();

    void resetBlockChain() throws WalletManager.WalletErrorException;
}
