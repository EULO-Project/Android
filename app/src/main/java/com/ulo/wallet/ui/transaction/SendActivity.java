package com.ulo.wallet.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseActivity;
import com.ulo.wallet.base.BaseDrawerActivity;
import com.ulo.wallet.base.dialog.SimpleTextDialog;
import com.ulo.wallet.base.dialog.SimpleTwoButtonsDialog;
import com.ulo.wallet.ui.contacts.ContactsActivity;
import com.ulo.wallet.ui.pincode.PincodeActivity;
import com.ulo.wallet.ui.transaction.custom.ChangeAddressActivity;
import com.ulo.wallet.ui.transaction.custom.CustomFeeActivity;
import com.ulo.wallet.ui.transaction.custom.CustomFeeFragment;
import com.ulo.wallet.utils.DialogsUtil;
import com.ulo.wallet.utils.NavigationUtils;
import com.ulo.wallet.utils.TxUtils;
import com.ulo.wallet.utils.scanner.ScanActivity;


import global.utils.GsonUtils;
import global.utils.LogHelper;
import wallet.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import global.AddressLabel;
import global.PivxRate;
import global.exceptions.NoPeerConnectedException;
import global.wrappers.InputWrapper;
import wallet.transaction.ErrorBean;
import wallet.transaction.Transaction;

import static android.Manifest.permission_group.CAMERA;
import static com.ulo.wallet.ui.transaction.custom.ChangeAddressActivity.INTENT_EXTRA_CHANGE_ADDRESS;
import static com.ulo.wallet.ui.transaction.custom.ChangeAddressActivity.INTENT_EXTRA_CHANGE_SEND_ORIGIN;
import static com.ulo.wallet.ui.transaction.custom.CustomFeeFragment.INTENT_EXTRA_FEE;
import static com.ulo.wallet.ui.transaction.custom.CustomFeeFragment.INTENT_EXTRA_IS_FEE_PER_KB;
import static com.ulo.wallet.ui.transaction.custom.CustomFeeFragment.INTENT_EXTRA_IS_MINIMUM_FEE;
import static com.ulo.wallet.ui.transaction.custom.CustomFeeFragment.INTENT_EXTRA_IS_TOTAL_FEE;
import static com.ulo.wallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/4/17.
 */

public class SendActivity extends BaseActivity implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger(SendActivity.class);

    public static final String INTENT_EXTRA_TOTAL_AMOUNT = "total_amount";
    public static final String INTENT_ADDRESS = "intent_address";
    public static final String INTENT_MEMO = "intent_memo";

    private static final int PIN_RESULT = 121;
    private static final int SCANNER_RESULT = 122;
    private static final int CUSTOM_FEE_RESULT = 123;
    private static final int MULTIPLE_ADDRESSES_SEND_RESULT = 124;
    private static final int CUSTOM_INPUTS = 125;
    private static final int SEND_DETAIL = 126;
    private static final int CUSTOM_CHANGE_ADDRESS = 127;

    private View root;
    private Button buttonSend, addAllPiv;
    private AutoCompleteTextView edit_address;
    private TextView txt_local_currency, txt_coin_selection, txt_custom_fee, txt_change_address, txtShowPiv;
    private TextView txt_multiple_outputs, txt_currency_amount;
    private View container_address;
    private EditText edit_amount, editCurrency;
    private EditText edit_memo;
    private MyFilterableAdapter filterableAdapter;
    private String addressStr;
    private PivxRate pivxRate;
    private ImageButton btnSwap;
    private ViewFlipper amountSwap;

    private boolean inPivs = true;
    private Transaction transaction;
    /**
     * Several outputs
     */
    private List<OutputWrapper> outputWrappers;
    /**
     * Custom inputs
     */
    private Set<InputWrapper> unspent;
    /**
     * Custom fee selector
     */
    private CustomFeeFragment.FeeSelector customFee;
    /**
     * Clean wallet flag
     */
    private boolean cleanWallet;
    /**
     * Is multi send
     */
    private boolean isMultiSend;
    /**
     * Change address
     */
    private boolean changeToOrigin;


     Coin amount;
    String memo;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_transaction_send, container);
        setTitle(R.string.btn_send);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        edit_address = (AutoCompleteTextView) findViewById(R.id.edit_address);
        edit_amount = (EditText) findViewById(R.id.edit_amount);
        edit_memo = (EditText) findViewById(R.id.edit_memo);
        container_address = root.findViewById(R.id.container_address);
        txt_local_currency = (TextView) findViewById(R.id.txt_local_currency);
        txt_multiple_outputs = (TextView) root.findViewById(R.id.txt_multiple_outputs);
        txt_multiple_outputs.setOnClickListener(this);
        txt_coin_selection = (TextView) root.findViewById(R.id.txt_coin_selection);
        txt_coin_selection.setOnClickListener(this);
        txt_custom_fee = (TextView) root.findViewById(R.id.txt_custom_fee);
        txt_custom_fee.setOnClickListener(this);
//        txt_change_address = (TextView) root.findViewById(R.id.txt_change_address);
//        txt_change_address.setOnClickListener(this);
        findViewById(R.id.button_qr).setOnClickListener(this);
        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSend.setOnClickListener(this);

        //Swap type of ammounts
        amountSwap = (ViewFlipper) findViewById(R.id.viewFlipper);
        amountSwap.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left));
        amountSwap.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right));
        btnSwap = (ImageButton) findViewById(R.id.btn_swap);
        btnSwap.setOnClickListener(this);

        //Sending amount currency
        editCurrency = (EditText) findViewById(R.id.edit_amount_currency);
        txt_currency_amount = (TextView) root.findViewById(R.id.txt_currency_amount);
        txtShowPiv = (TextView) findViewById(R.id.txt_show_piv);

        //Sending amount piv
        addAllPiv = (Button) findViewById(R.id.btn_add_all);
        addAllPiv.setOnClickListener(this);
        pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());

        if(pivxRate!=null){

            txt_local_currency.setText("0 " + pivxRate.getCode());
        }

        editCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pivxRate != null) {
                    if (s.length() > 0) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        BigDecimal result = new BigDecimal(valueStr).divide(pivxRate.getRate(), 6, BigDecimal.ROUND_DOWN);
                        txtShowPiv.setText(result.toPlainString() + " ULO");
                    } else {
                        txtShowPiv.setText("0 ULO");
                    }
                } else {
                    txtShowPiv.setText(R.string.no_rate);
                }
                cleanWallet = false;
            }
        });

        edit_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (pivxRate != null) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        Coin coin = Coin.parseCoin(valueStr);
                        txt_local_currency.setText(
                                pivxApplication.getCentralFormats().format(
                                        new BigDecimal(coin.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                                )
                                        + " " + pivxRate.getCode()
                        );
                    } else {
                        // rate null -> no connection.
                        txt_local_currency.setText(R.string.no_rate);
                    }
                } else {
                    if (pivxRate != null)
                        txt_local_currency.setText("0 " + pivxRate.getCode());
                    else
                        txt_local_currency.setText(R.string.no_rate);
                }
                cleanWallet = false;

            }
        });

        // Load data if exists
        Intent intent = getIntent();

        if(intent.hasExtra(INTENT_ADDRESS)){

            String address=intent.getStringExtra(INTENT_ADDRESS);
            edit_address.setText(address);
        }
        if(intent.hasExtra(INTENT_EXTRA_TOTAL_AMOUNT)){

            Coin amount = (Coin) intent.getSerializableExtra(INTENT_EXTRA_TOTAL_AMOUNT);

            edit_amount.setText(amount.toPlainString());
        }
        if(intent.hasExtra(INTENT_MEMO)){

            String memo = intent.getStringExtra(INTENT_MEMO);
            edit_memo.setText(memo);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogHelper.v("sendact onNewIntent");
        if(intent.hasExtra(INTENT_ADDRESS)){
            String address=intent.getStringExtra(INTENT_ADDRESS);
            edit_address.setText(address);
        }
        if(intent.hasExtra(INTENT_EXTRA_TOTAL_AMOUNT)){

            Coin amount = (Coin) intent.getSerializableExtra(INTENT_EXTRA_TOTAL_AMOUNT);

            edit_amount.setText(amount.toPlainString());
        }
        if(intent.hasExtra(INTENT_MEMO)){

            String memo = intent.getStringExtra(INTENT_MEMO);
            edit_memo.setText(memo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.option_fee) {
//            startCustomFeeActivity(customFee);
//            return true;
//        } else if (id == R.id.option_multiple_addresses) {
//            startMultiAddressSendActivity(outputWrappers);
//            return true;
//        } else if (id == R.id.option_select_inputs) {
//            startCoinControlActivity(unspent);
//        } else if (id == R.id.option_change_address) {
//            startChangeAddressActivity(changeAddress, changeToOrigin);
//        }

        if(id==R.id.option_address_book){
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    private void startCustomFeeActivity(CustomFeeFragment.FeeSelector customFee) {
        Intent intent = new Intent(this, CustomFeeActivity.class);
        if (customFee != null) {
            intent.putExtra(INTENT_EXTRA_IS_FEE_PER_KB, customFee.isFeePerKbSelected());
            intent.putExtra(INTENT_EXTRA_IS_TOTAL_FEE, !customFee.isFeePerKbSelected());
            intent.putExtra(INTENT_EXTRA_IS_MINIMUM_FEE, customFee.isPayMinimum());
            intent.putExtra(INTENT_EXTRA_FEE, customFee.getAmount());
        }
        startActivityForResult(intent, CUSTOM_FEE_RESULT);
    }

    private void startMultiAddressSendActivity(List<OutputWrapper> outputWrappers) {
//        Intent intent = new Intent(this, OutputsActivity.class);
//        Bundle bundle = new Bundle();
//        if (outputWrappers != null)
//            bundle.putSerializable(INTENT_EXTRA_OUTPUTS_WRAPPERS, (Serializable) outputWrappers);
//        intent.putExtras(bundle);
//        startActivityForResult(intent, MULTIPLE_ADDRESSES_SEND_RESULT);
    }

    private void startCoinControlActivity(Set<InputWrapper> unspent) {
//        String amountStr = getAmountStr();
//        if (amountStr.length() > 0) {
//            Intent intent = new Intent(this, InputsActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString(INTENT_EXTRA_TOTAL_AMOUNT, amountStr);
//            if (unspent != null)
//                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, (Serializable) unspent);
//            intent.putExtras(bundle);
//            startActivityForResult(intent, CUSTOM_INPUTS);
//        } else {
//            Toast.makeText(this, R.string.send_amount_input_error, Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if (transaction != null)
//            outState.putSerializable(TX, transaction.unsafeBitcoinSerialize());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // todo: test this roting the screen..
//        if (savedInstanceState.containsKey(TX)) {
//            transaction = new Transaction(pivxModule.getConf().getNetworkParams(), savedInstanceState.getByteArray(TX));
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationUtils.goBackToHome(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        // todo: This is not updating the filter..
//        if (filterableAdapter == null) {
//            if (pivxModule.getContacts() != null && pivxModule.getContacts().size() > 0) {
//                List<AddressLabel> list = new ArrayList<>(pivxModule.getContacts());
//                filterableAdapter = new MyFilterableAdapter(this, list);
//                edit_address.setAdapter(filterableAdapter);
//            }
//        }

        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSend) {
            try {
                if (checkConnectivity()) {
                    send();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            }
        } else if (id == R.id.button_qr) {
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            startActivityForResult(new Intent(this, ScanActivity.class), SCANNER_RESULT);
        } else if (id == R.id.btn_add_all) {
            if (!isMultiSend) {
                cleanWallet = true;
                Coin coin = pivxModule.getAvailableBalanceCoin();
                if (inPivs) {
                    edit_amount.setText(coin.toPlainString());
                    if(pivxRate!=null) {

                        txt_local_currency.setText(
                                pivxApplication.getCentralFormats().format(
                                        new BigDecimal(coin.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                                )
                                        + " " + pivxRate.getCode()
                        );
                    }
                } else {
                    if(pivxRate!=null) {

                        editCurrency.setText(
                                pivxApplication.getCentralFormats().format(
                                        new BigDecimal(coin.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                                )
                        );
                    }
                    txtShowPiv.setText(coin.toFriendlyString());
                }
            } else {
                Toast.makeText(this, R.string.validate_multi_send_enabled, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.btn_swap) {
            if (!isMultiSend) {
                inPivs = !inPivs;
                amountSwap.showNext();
            } else {
                Toast.makeText(this, R.string.validate_multi_send_enabled, Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.txt_coin_selection) {
            startCoinControlActivity(unspent);
        } else if (id == R.id.txt_multiple_outputs) {
            startMultiAddressSendActivity(outputWrappers);
        } else if (id == R.id.txt_custom_fee) {
            startCustomFeeActivity(customFee);
        }


    }

    private boolean checkConnectivity() {
        if (!isOnline()) {
            SimpleTwoButtonsDialog noConnectivityDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                    this,
                    getString(R.string.error_no_connectivity_title),
                    getString(R.string.error_no_connectivity_body),
                    new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                        @Override
                        public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
//                            try {
//                                send(true);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                showErrorDialog(e.getMessage());
//                            }
                            dialog.dismiss();

                        }

                        @Override
                        public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                            dialog.dismiss();
                        }
                    }
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                noConnectivityDialog.setRightBtnTextColor(getColor(R.color.lightGreen));
            } else {
                noConnectivityDialog.setRightBtnTextColor(ContextCompat.getColor(this, R.color.lightGreen));
            }
            noConnectivityDialog.setLeftBtnTextColor(Color.WHITE)
                    .setRightBtnTextColor(Color.BLACK)
                    .setRightBtnBackgroundColor(Color.WHITE)
                    .setLeftBtnTextColor(Color.BLACK)
                    .setLeftBtnText(getString(R.string.button_cancel))
                    .setRightBtnText(getString(R.string.button_ok))
                    .show();

            return false;
        }
        return true;
    }

    public boolean isOnline() {


        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT) {
            if (resultCode == RESULT_OK) {

                String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                edit_address.setText(address);
//                String address = "";
//                try {
//                    address = data.getStringExtra(INTENT_EXTRA_RESULT);
//                    String usedAddress;
//                    if (pivxModule.chechAddress(address)) {
//                        usedAddress = address;
//                    } else {
//                        PivxURI pivxUri = new PivxURI(address);
//                        usedAddress = pivxUri.getAddress().toBase58();
//                    }
//                    final String tempPubKey = usedAddress;
//                    edit_address.setText(tempPubKey);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "Bad address " + address, Toast.LENGTH_LONG).show();
//                }
            }
        }else if(requestCode == PIN_RESULT){
            if(resultCode == RESULT_OK){
                doSend();
            }
        }
// else if (requestCode == SEND_DETAIL) {
//            if (resultCode == RESULT_OK) {
//                try {
//                    // pin ok, send the tx now
//                    sendConfirmed();
//                } catch (Exception e) {
//                    e.printStackTrace();
////                    CrashReporter.saveBackgroundTrace(e, pivxApplication.getPackageInfo());
////                    showErrorDialog(R.string.commit_tx_fail);
//                }
//            }
//        } else if (requestCode == MULTIPLE_ADDRESSES_SEND_RESULT) {
//            if (resultCode == RESULT_OK) {
//                if (data.hasExtra(INTENT_EXTRA_OUTPUTS_CLEAR)) {
//                    outputWrappers = null;
//                    txt_multiple_outputs.setVisibility(View.GONE);
//                    container_address.setVisibility(View.VISIBLE);
//                    unBlockAmount();
//                    isMultiSend = false;
//                } else {
//                    outputWrappers = (List<OutputWrapper>) data.getSerializableExtra(INTENT_EXTRA_OUTPUTS_WRAPPERS);
//                    Coin totalAmount = Coin.ZERO;
//                    for (OutputWrapper outputWrapper : outputWrappers) {
//                        totalAmount = outputWrapper.getAmount().plus(totalAmount);
//                    }
//                    setAmountAndBlock(totalAmount);
//                    txt_multiple_outputs.setText(getString(R.string.multiple_address_send, outputWrappers.size()));
//                    txt_multiple_outputs.setVisibility(View.VISIBLE);
//                    container_address.setVisibility(View.GONE);
//                    isMultiSend = true;
//                }
//            }
//        } else if (requestCode == CUSTOM_INPUTS) {
//            if (resultCode == RESULT_OK) {
//                try {
//                    Set<InputWrapper> unspents = (Set<InputWrapper>) data.getSerializableExtra(INTENT_EXTRA_UNSPENT_WRAPPERS);
//                    for (InputWrapper inputWrapper : unspents) {
//                        inputWrapper.setUnspent(pivxModule.getUnspent(inputWrapper.getParentTxHash(), inputWrapper.getIndex()));
//                    }
//                    unspent = unspents;
//                    txt_coin_selection.setVisibility(View.VISIBLE);
//                } catch (TxNotFoundException e) {
//                    e.printStackTrace();
//                    CrashReporter.saveBackgroundTrace(e, pivxApplication.getPackageInfo());
//                    Toast.makeText(this, R.string.load_inputs_fail, Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                    CrashReporter.saveBackgroundTrace(e, pivxApplication.getPackageInfo());
//                    Toast.makeText(this, R.string.load_inputs_fail, Toast.LENGTH_LONG).show();
//                }
//            }
//        } else if (requestCode == CUSTOM_FEE_RESULT) {
//            if (resultCode == RESULT_OK) {
//                if (data.hasExtra(INTENT_EXTRA_CLEAR)) {
//                    customFee = null;
//                    txt_custom_fee.setVisibility(View.GONE);
//                } else {
//                    boolean isPerKb = data.getBooleanExtra(INTENT_EXTRA_IS_FEE_PER_KB, false);
//                    boolean isTotal = data.getBooleanExtra(INTENT_EXTRA_IS_TOTAL_FEE, false);
//                    boolean isMinimum = data.getBooleanExtra(INTENT_EXTRA_IS_MINIMUM_FEE, false);
//                    Coin feeAmount = (Coin) data.getSerializableExtra(INTENT_EXTRA_FEE);
//                    customFee = new CustomFeeFragment.FeeSelector(isPerKb, feeAmount, isMinimum);
//                    txt_custom_fee.setVisibility(View.VISIBLE);
//                }
//            }
//        } else if (requestCode == CUSTOM_CHANGE_ADDRESS) {
//            if (resultCode == RESULT_OK) {
//                if (data.hasExtra(ChangeAddressActivity.INTENT_EXTRA_CLEAR_CHANGE_ADDRESS)) {
//                    changeAddress = null;
//                    changeToOrigin = false;
//                    txt_change_address.setVisibility(View.GONE);
//                } else {
//                    if (data.hasExtra(INTENT_EXTRA_CHANGE_SEND_ORIGIN)) {
//                        changeAddress = null;
//                        changeToOrigin = true;
//                    } else {
//                        if (data.hasExtra(INTENT_EXTRA_CHANGE_ADDRESS)) {
//                            String address = data.getStringExtra(INTENT_EXTRA_CHANGE_ADDRESS);
//                            changeAddress = Address.fromBase58(pivxModule.getConf().getNetworkParams(), address);
//                        }
//                    }
//                    txt_change_address.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getAmountStr() {
        String amountStr = "0";
        if (inPivs) {
            amountStr = edit_amount.getText().toString();
        } else {
            // the value is already converted
            String valueStr = txtShowPiv.getText().toString();
            amountStr = valueStr.replace(" ULO", "");
            if (valueStr.length() > 0) {
                if (valueStr.charAt(0) == '.') {
                    amountStr = "0" + valueStr;
                }
            }
        }
        return amountStr;
    }

    public void setAmountAndBlock(Coin amount) {
        if (inPivs) {
            edit_amount.setText(amount.toPlainString());
            edit_amount.setEnabled(false);
        } else {
            BigDecimal result = new BigDecimal(amount.toPlainString()).multiply(pivxRate.getRate()).setScale(6, RoundingMode.FLOOR);
            editCurrency.setText(result.toPlainString());
            edit_amount.setEnabled(false);
        }
    }

    public void unBlockAmount() {
        if (inPivs) {
            edit_amount.setEnabled(true);
        } else {
            edit_amount.setEnabled(true);
        }
    }

    private void send() {
//        try {

        // check if the wallet is still syncing
        try {
            if (!pivxModule.isSyncWithNode()) {
                throw new IllegalArgumentException(getString(R.string.wallet_is_not_sync));
            }
        } catch (NoPeerConnectedException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(getString(R.string.no_peer_connection));
        }

        // first check amount
        String amountStr = getAmountStr();
        if (amountStr.length() < 1) throw new IllegalArgumentException("Amount not valid");
        if (amountStr.length() == 1 && amountStr.equals("."))
            throw new IllegalArgumentException("Amount not valid");
        if (amountStr.charAt(0) == '.') {
            amountStr = "0" + amountStr;
        }

         amount = Coin.parseCoin(amountStr);
        if (amount.isZero())
            throw new IllegalArgumentException("Amount zero, please correct it");
//        if (amount.isLessThan(Coin.valueOf(30000L)))
//            throw new IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, " + Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());
        if (amount.isGreaterThan(Coin.valueOf(pivxModule.getAvailableBalance())))
            throw new IllegalArgumentException("Insuficient balance");

        // memo
        memo = edit_memo.getText().toString();

        if(TxUtils.isEmpty(memo)){
            memo="";
        }
        addressStr = edit_address.getText().toString();
        if (!pivxModule.chechAddress(addressStr))
            throw new IllegalArgumentException("Address not valid");

        goCheckPincode();


//        logger.debug(result);

    }
    private void goCheckPincode(){
        Intent intent = new Intent(this, PincodeActivity.class);
        intent.putExtra(PincodeActivity.CHECK_PIN,true);
        startActivityForResult(intent,PIN_RESULT);
    }

    private void doSend(){
        showProgressDialog();
        final String finalMemo = memo;
        Executors.newSingleThreadExecutor()
                .submit(new Runnable() {
                    @Override
                    public void run() {
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
                        final String result = pivxModule.createTransaction(amount.getValue(), addressStr, finalMemo);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                try{
                                    Transaction transaction= GsonUtils.gsonToBean(result,Transaction.class);
                                    if(transaction!=null&&transaction.getErrno()==1){
                                        finish();
                                        showToast("Success");
                                    }else{
                                        String error="Error";
                                        try {
                                            error=transaction.getMessage();
                                        }catch (Exception eErrorBean){
                                            eErrorBean.printStackTrace();

                                        }
                                        showErrorDialog(error);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    String error="Error";
                                    try {
                                        ErrorBean errorBean = GsonUtils.gsonToBean(result, ErrorBean.class);
                                        error=errorBean.getMessage();
                                    }catch (Exception eErrorBean){
                                        eErrorBean.printStackTrace();

                                    }

                                    showErrorDialog(error);
                                }

                                Log.v("luo","bread = "+result);
                            }
                        });

                    }
                });
    }

//    public Coin getFee() {
//        Coin feePerKb;
//        // tx size calculation -> (148*inputs)+(34*outputs)+10
//        //long txSize = 148 * transaction.getInputs().size() + 34 * transaction.getOutputs().size() + 10;
//
//        if (customFee != null) {
//            if (customFee.isPayMinimum()) {
//                feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
//            } else {
//                if (customFee.isFeePerKbSelected()) {
//                    // fee per kB
//                    feePerKb = customFee.getAmount();
//                } else {
//                    // todo: total fee..
//                    feePerKb = customFee.getAmount();
//                }
//            }
//        } else {
//            feePerKb = Transaction.DEFAULT_TX_FEE;
//        }
//        return feePerKb;
//    }

    private void sendConfirmed() {
//        if (transaction == null) {
//            logger.error("## trying to send a NULL transaction");
//            try {
//                CrashReporter.appendSavedBackgroundTraces(new StringBuilder().append("ERROR ### sendActivity - sendConfirmed - transaction NULL"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            showErrorDialog(R.string.commit_tx_fail);
//            return;
//        }
//        pivxModule.commitTx(transaction);
//        Intent intent = new Intent(SendActivity.this, PivxWalletService.class);
//        intent.setAction(ACTION_BROADCAST_TRANSACTION);
//        intent.putExtra(DATA_TRANSACTION_HASH, transaction.getHash().getBytes());
//        startService(intent);
//        Toast.makeText(SendActivity.this, R.string.sending_tx, Toast.LENGTH_LONG).show();
//        finish();
//        NavigationUtils.goBackToHome(this);
    }
}
