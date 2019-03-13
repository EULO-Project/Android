package wallet;

import android.util.Log;

import com.google.common.base.Charsets;

import org.pivxj.core.Address;
import org.pivxj.core.BlockChain;
import org.pivxj.core.InsufficientMoneyException;
import org.pivxj.core.PeerGroup;
import org.pivxj.core.Sha256Hash;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutput;
import org.pivxj.core.Utils;
import org.pivxj.core.listeners.TransactionConfidenceEventListener;
import org.pivxj.crypto.DeterministicKey;
import org.pivxj.crypto.LinuxSecureRandom;
import org.pivxj.crypto.MnemonicCode;
import org.pivxj.crypto.MnemonicException;
import org.pivxj.wallet.DeterministicKeyChain;
import org.pivxj.wallet.DeterministicSeed;
import org.pivxj.wallet.Protos;
import org.pivxj.wallet.SendRequest;
import org.pivxj.wallet.UnreadableWalletException;
import org.pivxj.wallet.WalletFiles;
import org.pivxj.wallet.WalletProtobufSerializer;
import org.pivxj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import global.ContextWrapper;
import global.WalletConfiguration;
import global.utils.GsonUtils;
import global.utils.Io;
import global.utils.LogHelper;
import global.utils.StringUtils;
import wallet.exceptions.InsufficientInputsException;
import wallet.exceptions.TxNotFoundException;


/**
 * Created by furszy on 6/4/17.
 */

public class WalletManager {

    static {
        System.loadLibrary("monerujo");
    }


    private static final Logger logger = LoggerFactory.getLogger(WalletManager.class);
    /**
     * Minimum entropy
     */
    private static final int SEED_ENTROPY_EXTRA = 256;
    private static final int ENTROPY_SIZE_DEBUG = -1;


    private org.pivxj.wallet.Wallet wallet;
    Wallet walletEulo;
    private File walletFile;

    private WalletConfiguration conf;
    private ContextWrapper contextWrapper;

    public WalletManager(ContextWrapper contextWrapper, WalletConfiguration conf) {
        this.conf = conf;
        this.contextWrapper = contextWrapper;
    }

    // methods

    public Address newFreshReceiveAddress() {
        return wallet.freshReceiveAddress();
    }

    /**
     * Get the last address active which not appear on a tx.
     *
     * @return
     */
    public String getCurrentAddress() {
        return GetAddress(walletEulo);
    }

    public List<Address> getIssuedReceiveAddresses() {
        return wallet.getIssuedReceiveAddresses();
    }

    /**
     * Method to know if an address is already used for receive coins.
     *
     * @return
     */
    public boolean isMarkedAddress(Address address) {
        return false;
    }

    public boolean isWatchingAddress(Address address) {
        return wallet.isAddressWatched(address);
    }

    public void completeSend(SendRequest sendRequest) throws InsufficientMoneyException {
        wallet.completeTx(sendRequest);
    }

    // init

    public void init() throws IOException {
        // init mnemonic code first..
        initMnemonicCode();

        restoreOrCreateWallet();

//        Log.v("test",test("test test")) ;
    }

    private void initMnemonicCode() {
        try {
            if (MnemonicCode.INSTANCE == null) {

                InputStream inputStream = contextWrapper.openAssestsStream(conf.getMnemonicFilename());
                MnemonicCode.INSTANCE = new MnemonicCode(inputStream, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreOrCreateWallet() throws IOException {
        walletFile = contextWrapper.getFileStreamPath(conf.getWalletProtobufFilename());
        loadWalletFromProtobuf(walletFile);
    }


    private File getWalletFile() {
        return walletFile;
    }

    //    private void loadWalletFromProtobuf(File walletFile) throws IOException {
//        if (walletFile.exists()) {
//            FileInputStream walletStream = null;
//            try {
//                walletStream = new FileInputStream(walletFile);
//                wallet = new WalletProtobufSerializer().readWallet(walletStream);
//
//                if (!wallet.getParams().equals(conf.getNetworkParams()))
//                    throw new UnreadableWalletException("bad wallet network parameters: " + wallet.getParams().getId());
//
//            } catch (UnreadableWalletException e) {
//                logger.error("problem loading wallet", e);
//                wallet = restoreWalletFromBackup();
//            } catch (FileNotFoundException e) {
//                logger.error("problem loading wallet", e);
//                //context.toast(e.getClass().getName());
//                wallet = restoreWalletFromBackup();
//            } finally {
//                if (walletStream != null)
//                    try {
//                        walletStream.close();
//                    } catch (IOException e) {
//                        //nothing
//                    }
//            }
//            if (!wallet.isConsistent()) {
//                //contextWrapper.toast("inconsistent wallet: " + walletFile);
//                logger.error("inconsistent wallet " + walletFile);
//                wallet = restoreWalletFromBackup();
//            }
//            if (!wallet.getParams().equals(conf.getNetworkParams()))
//                throw new Error("bad wallet network parameters: " + wallet.getParams().getId());
//
//            afterLoadWallet();
//
//        } else {
//
//            // generate wallet from random mnemonic
//            wallet = generateRandomWallet();
//
//            saveWallet();
//            backupWallet();
//
////            config.armBackupReminder();
//            logger.info("new wallet created");
//        }
//
//        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
//            @Override
//            public void onCoinsReceived(org.pivxj.wallet.Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
//                org.pivxj.core.Context.propagate(conf.getWalletContext());
//                saveWallet();
//            }
//        });
//    }
    private void loadWalletFromProtobuf(File walletFile) throws IOException {
        if (walletFile.exists() && contextWrapper.getPinCode() != null) {
            try {
                walletEulo = openWallet(walletFile.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();

                logger.info("openWallet error " + e.getMessage());
            }
        } else {
            logger.info("new wallet created");
        }

        if (walletEulo == null) {
            walletEulo = Wallet.NEW_WALLET(this);
            walletEulo.setSeed(generateMnemonic(SEED_ENTROPY_EXTRA));
            walletEulo.setPath(walletFile.getAbsolutePath());
            walletEulo.setPassword(contextWrapper.getPinCode());
            walletFile.mkdirs();
        }

    }

    public org.pivxj.wallet.Wallet generateRandomWallet() {
        if (Utils.isAndroidRuntime()) {
            new LinuxSecureRandom();
        }
        List<String> words = generateMnemonic(SEED_ENTROPY_EXTRA);
        DeterministicSeed seed = new DeterministicSeed(words, null, "", System.currentTimeMillis());
        return org.pivxj.wallet.Wallet.fromSeed(conf.getNetworkParams(), seed, DeterministicKeyChain.KeyChainType.BIP44_PIVX_ONLY);
    }

    public static List<String> generateMnemonic(int entropyBitsSize) {
        byte[] entropy;
        if (ENTROPY_SIZE_DEBUG > 0) {
            entropy = new byte[ENTROPY_SIZE_DEBUG];
        } else {
            entropy = new byte[entropyBitsSize / 8];
        }
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(entropy);
        List<String> list = bytesToMnemonic(entropy);
        list.subList(0, 12);
        return list.subList(0, 12);
    }

    public static List<String> bytesToMnemonic(byte[] bytes) {
        List<String> mnemonic;
        try {
            mnemonic = MnemonicCode.INSTANCE.toMnemonic(bytes);
        } catch (MnemonicException.MnemonicLengthException e) {
            throw new RuntimeException(e); // should not happen, we have 16 bytes of entropy
        }
        return mnemonic;
    }


    /**
     * Restore wallet from backup
     *
     * @return
     */
    private org.pivxj.wallet.Wallet restoreWalletFromBackup() {

        InputStream is = null;
        try {
            is = contextWrapper.openFileInput(conf.getKeyBackupProtobuf());
            final org.pivxj.wallet.Wallet wallet = new WalletProtobufSerializer().readWallet(is, true, null);
            if (!wallet.isConsistent())
                throw new Error("Inconsistent backup");
            // todo: acá tengo que resetear la wallet
            //resetBlockchain();
            //context.toast("Your wallet was reset!\\\\nIt will take some time to recover.");
            logger.info("wallet restored from backup: '" + conf.getKeyBackupProtobuf() + "'");
            return wallet;
        } catch (final IOException e) {
            throw new Error("cannot read backup", e);
        } catch (UnreadableWalletException e) {
            throw new Error("cannot read backup", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    public void restoreWalletFrom(List<String> mnemonic) throws IOException, MnemonicException {

        List<String> list = getAvailableMnemonicWordsList();
        for (String str : mnemonic) {
            if (!list.contains(str)) {
                LogHelper.v("MnemonicException = " + str);
                throw new MnemonicException();
            }
        }
//        MnemonicCode.INSTANCE.check(mnemonic);
//        wallet = org.pivxj.wallet.Wallet.fromSeed(
//                conf.getNetworkParams(),
//                new DeterministicSeed(mnemonic, null, "", timestamp),
//                bip44 ? DeterministicKeyChain.KeyChainType.BIP44_PIVX_ONLY : DeterministicKeyChain.KeyChainType.BIP32
//        );
//        restoreWallet(wallet);
        Io.delete(walletFile);
        walletFile.mkdirs();
        walletEulo = Wallet.NEW_WALLET(this);
        walletEulo.setSeed(mnemonic);
        walletEulo.setPath(walletFile.getAbsolutePath());
        walletEulo.setPassword(contextWrapper.getPinCode());
    }

    /**
     * Este metodo puede tener varias implementaciones de guardado distintas.
     */
    public void saveWallet() {
//        try {
//            protobufSerializeWallet(wallet);
//        } catch (final IOException x) {
//            throw new RuntimeException(x);
//        }
    }

    /**
     * Save wallet file
     *
     * @param wallet
     * @throws IOException
     */
    private void protobufSerializeWallet(final org.pivxj.wallet.Wallet wallet) throws IOException {
        logger.info("trying to serialize: " + walletFile.getAbsolutePath());
        wallet.saveToFile(walletFile);
        // make wallets world accessible in test mode
        //if (conf.isTest())
        //    Io.chmod(walletFile, 0777);

        logger.info("wallet saved to: '{}', took {}", walletFile);
    }


    public List<Address> getWatchedAddresses() {
        return wallet.getWatchedAddresses();
    }

    public void reset() {
        wallet.reset();
    }

    public long getEarliestKeyCreationTime() {
        return wallet.getEarliestKeyCreationTime();
    }

    public void addWalletFrom(PeerGroup peerGroup) {
        peerGroup.addWallet(wallet);
    }

    public void addWalletFrom(BlockChain blockChain) {
        blockChain.addWallet(wallet);
    }

    public void removeWalletFrom(PeerGroup peerGroup) {
        peerGroup.removeWallet(wallet);
    }

    public int getLastBlockSeenHeight() {
        return wallet.getLastBlockSeenHeight();
    }

    public Transaction getTransaction(Sha256Hash hash) {
        return wallet.getTransaction(hash);
    }

    public void addCoinsReceivedEventListener(WalletCoinsReceivedEventListener coinReceiverListener) {
        wallet.addCoinsReceivedEventListener(coinReceiverListener);
    }

    public void removeCoinsReceivedEventListener(WalletCoinsReceivedEventListener coinReceiverListener) {
        wallet.removeCoinsReceivedEventListener(coinReceiverListener);
    }

    public Coin getAvailableBalance() {

        Long value = GetAvailiable(walletEulo);
        logger.debug("getAvailableBalance = " + value);
        return Coin.valueOf(value);
    }

    public Coin getALLBalance() {

        Long value = GetBalance(walletEulo);
        return Coin.valueOf(value);
    }
//    public Coin getValueSentFromMe(wallet.transaction.Transaction transaction) {
//
//        transaction.getSpend().
//        return transaction.getValueSentFromMe(wallet);
//    }
//
//    public Coin getValueSentToMe(Transaction transaction) {
//        return transaction.getValueSentToMe(wallet);
//    }


    public void restoreWalletFromProtobuf(final File file) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            restoreWallet(WalletUtils.restoreWalletFromProtobuf(is, conf.getNetworkParams()));
            logger.info("successfully restored unencrypted wallet: {}", file);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException x2) {
                    // swallow
                }
            }
        }
    }

    private void restoreWallet(final org.pivxj.wallet.Wallet wallet) throws IOException {

        replaceWallet(wallet);

        //config.disarmBackupReminder();
        // en vez de hacer esto acá hacerlo en el module..
        /*if (listener!=null)
            listener.onWalletRestored();*/

    }

    public void replaceWallet(final org.pivxj.wallet.Wallet newWallet) throws IOException {
        resetBlockchain();

        try {
            wallet.shutdownAutosaveAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
        wallet = newWallet;
        //conf.maybeIncrementBestChainHeightEver(newWallet.getLastBlockSeenHeight());
//        afterLoadWallet();

        // todo: Nadie estaba escuchando esto.. Tengo que ver que deberia hacer despues
//        final IntentWrapper intentWrapper = new IntentWrapperAndroid(WalletConstants.ACTION_WALLET_REFERENCE_CHANGED);
//        intentWrapper.setPackage(context.getPackageName());
//        context.sendLocalBroadcast(intentWrapper);
    }

    private void resetBlockchain() {
        contextWrapper.stopBlockchain();
    }

    public void restoreWalletFromEncrypted(File file, String password) throws IOException {
        final BufferedReader cipherIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        final StringBuilder cipherText = new StringBuilder();
        Io.copy(cipherIn, cipherText, conf.getBackupMaxChars());
        cipherIn.close();

        final byte[] plainText = Crypto.decryptBytes(cipherText.toString(), password.toCharArray());
        final InputStream is = new ByteArrayInputStream(plainText);

        restoreWallet(WalletUtils.restoreWalletFromProtobufOrBase58(is, conf.getNetworkParams(), conf.getBackupMaxChars()));

        logger.info("successfully restored encrypted wallet: {}", file);
    }

    /**
     * Backup wallet
     */
    private void backupWallet() {

        final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(wallet).toBuilder();

        // strip redundant
        builder.clearTransaction();
        builder.clearLastSeenBlockHash();
        builder.setLastSeenBlockHeight(-1);
        builder.clearLastSeenBlockTimeSecs();
        final Protos.Wallet walletProto = builder.build();

        OutputStream os = null;

        try {
            os = contextWrapper.openFileOutputPrivateMode(conf.getKeyBackupProtobuf());
            walletProto.writeTo(os);
        } catch (FileNotFoundException e) {
            logger.error("problem writing wallet backup", e);
        } catch (IOException e) {
            logger.error("problem writing wallet backup", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    /**
     * Backup wallet file with a given password
     *
     * @param file
     * @param password
     * @throws IOException
     */

    public boolean backupWallet(File file, final String password) throws IOException {

        return backupWallet(wallet, file, password);
    }

    /**
     * Backup wallet file with a given password
     *
     * @param file
     * @param password
     * @throws IOException
     */
    public boolean backupWallet(org.pivxj.wallet.Wallet wallet, File file, final String password) throws IOException {

//        final Protos.Wallet walletProto = new WalletProtobufSerializer().walletToProto(wallet);
//
//        Writer cipherOut = null;
//
//        try {
//            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            walletProto.writeTo(baos);
//            baos.close();
//            final byte[] plainBytes = baos.toByteArray();
//
//            cipherOut = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
//            cipherOut.write(Crypto.encrypt(plainBytes, password.toCharArray()));
//            cipherOut.flush();
//
//            logger.info("backed up wallet to: '" + file + "'");
//
//            return true;
//        } finally {
//            if (cipherOut != null) {
//                try {
//                    cipherOut.close();
//                } catch (final IOException x) {
//                    // swallow
//                }
//            }
//        }
        return true;

    }

    public boolean backUpWallet() {
        int code = 0;
        if (walletFile != null && walletFile.exists()) {
            code = BackupWallet(walletFile.getAbsolutePath(), walletEulo);
        }
        if (code == 1) {
            return true;
        }
        return false;
    }

    /**
     * Restart the wallet and re create it in a watch only mode.
     *
     * @param xpub
     */
    public void watchOnlyMode(String xpub, DeterministicKeyChain.KeyChainType keyChainType) throws IOException {
        org.pivxj.wallet.Wallet wallet = org.pivxj.wallet.Wallet.fromWatchingKeyB58(conf.getNetworkParams(), xpub, 0, keyChainType);
        restoreWallet(wallet);
    }

    public List<wallet.transaction.Transaction> listTransactions() {

        List<wallet.transaction.Transaction> list = new ArrayList<>();
        try {
            String result = getTransaction();

            LogHelper.v("listTransactions  = \n" + result);
            list = GsonUtils.jsonStringConvertToList(result, wallet.transaction.Transaction[].class);

            Collections.sort(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }


    /**
     * Return true is this wallet instance built the transaction
     *
     * @param transaction
     */
    public boolean isMine(wallet.transaction.Transaction transaction) {
        return transaction.getIsSpend();
    }

    public void commitTx(Transaction transaction) {
        wallet.maybeCommitTx(transaction);
    }

    public Coin getUnspensableBalance() {

        return getALLBalance().minus(getAvailableBalance());
    }

    public boolean isAddressMine(Address address) {
        return wallet.isPubKeyHashMine(address.getHash160());
    }

    public void addOnTransactionsConfidenceChange(TransactionConfidenceEventListener transactionConfidenceEventListener) {
        wallet.addTransactionConfidenceEventListener(transactionConfidenceEventListener);
    }

    public void removeTransactionConfidenceChange(TransactionConfidenceEventListener transactionConfidenceEventListener) {
        wallet.removeTransactionConfidenceEventListener(transactionConfidenceEventListener);
    }

    /**
     * Don't use this, it's just for the restoreWallet.
     *
     * @return
     */
    @Deprecated
    public wallet.Wallet getWallet() {
        return walletEulo;
    }

    public List<TransactionOutput> listUnspent() {
        return wallet.getUnspents();
    }

    public List<String> getMnemonic() {

        return walletEulo.getSeed();
//        return wallet.getActiveKeyChain().getMnemonicCode();
    }

    public DeterministicKey getKeyPairForAddress(Address address) {
        DeterministicKey deterministicKey = wallet.getActiveKeyChain().findKeyFromPubHash(address.getHash160());
        logger.info("Key pub: " + deterministicKey.getPublicKeyAsHex());
        return deterministicKey;
    }

    /**
     * If the wallet doesn't contain any private key.
     *
     * @return
     */
    public boolean isWatchOnly() {
        return false;
    }

    public TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) throws TxNotFoundException {
        Transaction tx = wallet.getTransaction(parentTxHash);
        if (tx == null)
            throw new TxNotFoundException("tx " + parentTxHash.toString() + " not found");
        return tx.getOutput(index);
    }

    public List<TransactionOutput> getRandomListUnspentNotInListToFullCoins(List<TransactionInput> inputs, Coin amount) throws InsufficientInputsException {
        List<TransactionOutput> list = new ArrayList<>();
        Coin total = Coin.ZERO;
        for (TransactionOutput transactionOutput : wallet.getUnspents()) {
            boolean found = false;
            if (inputs != null) {
                for (TransactionInput input : inputs) {
                    if (input.getConnectedOutput().getParentTransactionHash().equals(transactionOutput.getParentTransactionHash())
                            &&
                            input.getConnectedOutput().getIndex() == transactionOutput.getIndex()) {
                        found = true;
                    }
                }
            }
            if (!found) {
                if (total.isLessThan(amount)) {
                    list.add(transactionOutput);
//                    total = total.add(transactionOutput.getValue());
                }
                if (total.isGreaterThan(amount)) {
                    return list;
                }
            }
        }
        throw new InsufficientInputsException("No unspent available", null);
    }

//    public Coin getUnspentValue(Sha256Hash parentTransactionHash, int index) {
//        Transaction tx = wallet.getTransaction(parentTransactionHash);
//        if (tx == null) return null;
//        return tx.getOutput(index).getValue();
//    }

    public void checkMnemonic(List<String> mnemonic) throws MnemonicException {
        MnemonicCode.INSTANCE.check(mnemonic);
    }

    public DeterministicKey getWatchingPubKey() {
        return wallet.getWatchingKey();
    }

    public String getExtPubKey() {
        return wallet.getWatchingKey().serializePubB58(conf.getNetworkParams());
    }

    public boolean isBip32Wallet() {
        return wallet.getActiveKeyChain().getKeyChainType() == DeterministicKeyChain.KeyChainType.BIP32;
    }

    /**
     * Create a clean transaction from the wallet balance to the sweep address
     *
     * @param sweepAddress
     * @return
     */
    public Transaction createCleanWalletTx(Address sweepAddress) throws InsufficientMoneyException {
        SendRequest sendRequest = SendRequest.emptyWallet(sweepAddress);
        wallet.completeTx(sendRequest);
        return sendRequest.tx;
    }

    public List<String> getAvailableMnemonicWordsList() {
        return MnemonicCode.INSTANCE.getWordList();
    }

    public boolean checkAddress(String addressBase58) {
        if (StringUtils.isEmpty(addressBase58)) {
            return false;
        }
        return true;
    }

    private static final class WalletAutosaveEventListener implements WalletFiles.Listener {

        WalletConfiguration conf;

        public WalletAutosaveEventListener(WalletConfiguration walletConfiguration) {
            conf = walletConfiguration;
        }

        @Override
        public void onBeforeAutoSave(final File file) {
        }

        @Override
        public void onAfterAutoSave(final File file) {
            // make wallets world accessible in test mode
            //if (conf.isTest())
            //    Io.chmod(file, 0777);
        }
    }

    public void savePinCode(String pincode) {
        walletEulo.setPassword(pincode);
    }

    public Wallet createWallet() throws WalletErrorException {


        if (!walletEulo.isNewWallet()) {
            return walletEulo;
        }
        String salt = "ulosalt";
        try {

            Log.v("wallet", "walletEulo.getPath() =" + walletEulo.getPath());
            Log.v("wallet", "walletEulo.getPassword() =" + walletEulo.getPassword());
            long holder = CreateWallet(walletEulo.getPath(), "myWallet", walletEulo.getPassword(), WalletUtils.listToString(walletEulo.getSeed()), salt);
//        long holder=test("wallet");

            Log.v("wallet", "createWallet holder = " + holder);

            walletEulo = Wallet.HANDlE(holder, this);

            if (holder == Wallet.ERROR_CODE) {
                throw new WalletErrorException(getError());
            }
//            Log.v("wallet","error = "+getError());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String addr=GetAddress(wallet);
//        Log.v("wallet","createWallet addr = "+addr);
        return walletEulo;
    }

    public String getError() {
        if (walletEulo == null) {
            walletEulo = Wallet.HANDlE(Wallet.ERROR_CODE, this);
        }
        return GetErrString(walletEulo);
    }


    public long getConnectStatus() {
        if (walletEulo.isEnable()) {

            return GetConnectStatus(walletEulo);
        }
        return Wallet.CONNECT_STATUS_DISCONNECTED;
    }

    public String createTransaction(long amount, String addr, String des) {


        Log.v("wallet", "CreateTransaction ");
        Log.v("wallet", "amount =  " + amount);
        Log.v("wallet", "addr =  " + addr);
        Log.v("wallet", "des =  " + des);
        return CreateTransaction(walletEulo, amount, addr, des);
    }

    public Wallet openWallet(String path) throws WalletErrorException {
        long handle = OpenWallet(path, "myWallet", contextWrapper.getPinCode());
        if (handle == Wallet.ERROR_CODE) {
            throw new WalletErrorException(getError());
        }
        Wallet wallet = handleToWallet(handle);
        return wallet;
    }

    public boolean ifsSyncFinished() {
        if (ifSyncFinished(walletEulo) == 1) {
            return true;
        }
        return false;
    }

    public long getBlockHeight() throws WalletErrorException {
        long height = GetBlockHeight(walletEulo);
        if (height == 0) {
//            throw new WalletErrorException(getError());
        }


        LogHelper.v("handle = "+walletEulo.getHandle());
        LogHelper.v("getBlockHeight = "+height);
        return height;
    }

    public long getLastBlockHeight() {
        long height = GetLastBlockHeight(walletEulo);

        LogHelper.v("handle = "+walletEulo.getHandle());
        LogHelper.v("getLastBlockHeight = "+height);
        return height;
    }

    public String getTransaction() {
        return GetTransaction(walletEulo);
    }

    private Wallet handleToWallet(long handle) {
        if (handle == Wallet.ERROR_CODE) {
            return null;
        }
        return new Wallet(handle, this);
    }



    public Wallet createWallet(String path, String account, String password, String seed) {
        return new Wallet(CreateWallet(path, account, password, seed, "ulosalt"), this);
    }

    public boolean backupWallet(String path, Wallet wallet) {

        int result = BackupWallet(path, wallet);
        if (result == 0) {
            return false;
        }
        return true;
    }

    public void updatePassword(String oldpwd, String password) throws WalletErrorException {
        int res = UpdatePassword(walletEulo, oldpwd, password);
        if (res != 0) {
            throw new WalletErrorException(getError());
        }

    }

    public void addNode(String ip, int port) throws WalletErrorException {
        int res = AddNode(walletEulo, ip, port);
        if (res != 0) {
            throw new WalletErrorException(getError());
        }
    }

    public void resetBlockChain() throws WalletErrorException {
        int res = ResetBlockChain(walletEulo);

        LogHelper.v("handle = "+walletEulo.getHandle());
        LogHelper.v("ResetBlockChain = "+res);
        if (res != 0) {
            throw new WalletErrorException(getError());
        }
    }


    protected long getBalance(Wallet wallet) {
        return GetBalance(wallet);
    }

    private native int AddNode(Wallet wallet, String ip, int port);


    private native long CreateWallet(String path, String account, String password, String seed, String salt);


    private native long OpenWallet(String path, String account, String password);


    private native int BackupWallet(String path, Wallet wallet);

    private native int UpdatePassword(Wallet wallet, String oldpwd, String password);


    private native int ResetBlockChain(Wallet wallet);


    private native long ifSyncFinished(Wallet wallet);
//int ConnectedPeerCount(Wallet wallet);

//0 disconnected, 1 connectting, 2 connected
    private native long GetConnectStatus(Wallet wallet);

    private native long GetBlockHeight(Wallet wallet);

    private native long GetLastBlockHeight(Wallet wallet);

    private native String GetAddress(wallet.Wallet wallet);

    private native String GetSeed(Wallet wallet);

    public native String GetSalt(Wallet wallet);

    private native long GetBalance(Wallet wallet);

    private native long GetAvailiable(Wallet wallet);

    private native String GetTransaction(Wallet wallet);


    private native String aesEncrypt(String key, String pt);

    private native String aesDecrypt(String key, String ct);

    private native String CreateTransaction(Wallet wallet, long amount, String addr, String des);

    private native String GetErrString(Wallet wallet);


    public class WalletErrorException extends Exception {
        public WalletErrorException(String error) {
            super(error);
        }
    }

}
