package com.ulo.wallet.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseDrawerActivity;
import com.ulo.wallet.base.dialog.SimpleTextDialog;
import com.ulo.wallet.ui.transaction.SendActivity;
import com.ulo.wallet.utils.AnimationUtils;
import com.ulo.wallet.utils.DialogsUtil;
import com.ulo.wallet.utils.QrUtils;
import com.ulo.wallet.utils.scanner.ScanActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import global.utils.LogHelper;
import global.utils.StringUtils;
import wallet.Coin;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import chain.BlockchainState;
import global.PivxRate;
import global.exceptions.NoPeerConnectedException;
import okhttp3.Call;

import static android.Manifest.permission.CAMERA;
import static com.ulo.wallet.model.UloContext.OUT_OF_SYNC_TIME;
import static com.ulo.wallet.service.IntentsConstants.*;
import static com.ulo.wallet.ui.transaction.SendActivity.*;
import static com.ulo.wallet.utils.scanner.ScanActivity.*;

/**
 * Created by Neoperol on 5/11/17.
 */

public class WalletActivity extends BaseDrawerActivity {
    private static final int SCANNER_RESULT = 122;

    private View root;
    private View container_txs;

    private TextView txt_value;
    private TextView txt_unnavailable;
    private TextView txt_local_currency;
    private TextView txt_watch_only;
    private View view_background;
    private View container_syncing;
    private PivxRate pivxRate;
    private TransactionsFragmentBase txsFragment;
    private FloatingActionMenu floatingActionMenu;
    private TextView txt_sync_status;
    private ImageView img_sync;
    protected BlockchainState blockchainState = BlockchainState.SYNCING;

    // Receiver
    private LocalBroadcastManager localBroadcastManager;

    private IntentFilter pivxServiceFilter = new IntentFilter(ACTION_NOTIFICATION);
//    private BroadcastReceiver pivxServiceReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(ACTION_NOTIFICATION)) {
//                if (intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)) {
//                    // Check if the app is on foreground to update the view.
//                    if (!isOnForeground) return;
//                    updateBalance();
//                    txsFragment.refresh();
//                }
//            }
//
//        }
//    };

    private Timer timer = new Timer("wallet");
    int timeIndex=10;
    private void startTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeIndex++;
                        if(!isOnForeground){
                            return;
                        }
                        checkState();
                        onBlockchainStateChange();
                        updateBlockchainState();
                        updateBalance();
                        if(timeIndex>10||txsFragment.getList()==null||txsFragment.getList().size()==0){
                            timeIndex=0;
                            if(pivxModule.checkTransactionChanged()){
                                txsFragment.refresh();
                            }

                        }

                    }
                });
            }
        }, 0, 1000);
    }
//    private BroadcastReceiver walletServiceReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.hasExtra(INTENT_BROADCAST_DATA_TYPE)) {
//                if (intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE)) {
//                    BlockchainState blockchainStateNew = (BlockchainState) intent.getSerializableExtra(INTENT_EXTRA_BLOCKCHAIN_STATE);
//                    if (blockchainStateNew == null) {
//                        Log.e("APP", "blockchain state null..");
//                        return;
//                    }
//                    blockchainState = blockchainStateNew;
//                    onBlockchainStateChange();
//                    updateBlockchainState();
//                } else if (intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_PEER_CONNECTED)) {
//                    checkState();
//                    updateBlockchainState();
//                }
//            }
//        }
//    };

    @Override
    protected void beforeCreate() {
        /*
        if (!appConf.isAppInit()){
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        }
        // show report dialog if something happen with the previous process
        */
//        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle(R.string.my_wallet);
        root = getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        View containerHeader = getLayoutInflater().inflate(R.layout.fragment_pivx_amount, header_container);
        header_container.setVisibility(View.VISIBLE);
        txt_value = (TextView) containerHeader.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) containerHeader.findViewById(R.id.txt_unnavailable);
        container_txs = root.findViewById(R.id.container_txs);
        txt_sync_status = (TextView) root.findViewById(R.id.txt_sync_status);
        img_sync = (ImageView) root.findViewById(R.id.img_sync);
        txt_local_currency = (TextView) containerHeader.findViewById(R.id.txt_local_currency);
        txt_watch_only = (TextView) containerHeader.findViewById(R.id.txt_watch_only);
        view_background = root.findViewById(R.id.view_background);
        container_syncing = root.findViewById(R.id.container_syncing);
        // Open Send
        containerHeader.findViewById(R.id.lay_send_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pivxModule.isWalletWatchOnly()) {
                    Toast.makeText(v.getContext(), R.string.error_watch_only_mode, Toast.LENGTH_SHORT).show();
                    return;
                }
                floatingActionMenu.close(true);
                startActivity(new Intent(v.getContext(), SendActivity.class));
            }
        });
        containerHeader.findViewById(R.id.lay_fab_recieve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                startActivity(new Intent(v.getContext(), RequestActivity.class));
            }
        });

        floatingActionMenu = (FloatingActionMenu) root.findViewById(R.id.fab_menu);
        floatingActionMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    AnimationUtils.fadeInView(view_background, 200);
                } else {
                    AnimationUtils.fadeOutGoneView(view_background, 200);
                }
            }
        });

        txsFragment = (TransactionsFragmentBase) getSupportFragmentManager().findFragmentById(R.id.transactions_fragment);
//        localBroadcastManager.registerReceiver(walletServiceReceiver, new IntentFilter(ACTION_NOTIFICATION));

        setRightOne(R.mipmap.ic_scan);
        setRighTwo(R.mipmap.ic_qr_code_white);
        rightOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission(CAMERA)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        int permsRequestCode = 200;
                        String[] perms = {"android.permission.CAMERA"};
                        requestPermissions(perms, permsRequestCode);
                    }
                }
                startActivityForResult(new Intent(WalletActivity.this, ScanActivity.class), SCANNER_RESULT);
            }
        });
        rightTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletActivity.this, QrActivity.class));
            }
        });
        startTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(0);

        init();

        getRate();
        // register
//        localBroadcastManager.registerReceiver(pivxServiceReceiver, pivxServiceFilter);

        updateState();
        updateBalance();

//        checkState();
//        updateBlockchainState();
    }

    private void updateState() {
        txt_watch_only.setVisibility(pivxModule.isWalletWatchOnly() ? View.VISIBLE : View.GONE);
    }

    private void init() {
        // Start service if it's not started.
//        pivxApplication.startPivxService();
//
//        if (!pivxApplication.getAppConf().hasBackup()) {
//            long now = System.currentTimeMillis();
//            if (pivxApplication.getLastTimeRequestedBackup() + 1800000L < now) {
//                pivxApplication.setLastTimeBackupRequested(now);
//                SimpleTwoButtonsDialog reminderDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
//                        this,
//                        getString(R.string.reminder_backup),
//                        getString(R.string.reminder_backup_body),
//                        new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
//                            @Override
//                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
//                                startActivity(new Intent(WalletActivity.this, SettingsBackupActivity.class));
//                                dialog.dismiss();
//                            }
//
//                            @Override
//                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
//                                dialog.dismiss();
//                            }
//                        }
//                );
//                reminderDialog.setLeftBtnText(getString(R.string.button_dismiss));
//                reminderDialog.setLeftBtnTextColor(Color.WHITE);
//                reminderDialog.setRightBtnText(getString(R.string.button_ok));
//                reminderDialog.show();
//            }
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pivxModule!=null){
            pivxModule.backupWallet();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT) {
            if (resultCode == RESULT_OK) {
                try {

                    String url = data.getStringExtra(INTENT_EXTRA_RESULT);
                    Map<String,String> map= QrUtils.getUriParams(new String(url));

//                    final String usedAddress=url;
                    if (map==null||!map.containsKey(QrUtils.URI_ADDRESS)) {
                        if(pivxModule.chechAddress(url)){
                            Intent intent = new Intent(this, SendActivity.class);
                            intent.putExtra(INTENT_ADDRESS, url);
//                            intent.putExtra(INTENT_EXTRA_TOTAL_AMOUNT, amount);
//                            intent.putExtra(INTENT_MEMO, memo);
                            startActivity(intent);
                        }else{
//                            showToast("qr");
                            showErrorDialog(url);
//                            Toast.makeText(this, "Bad address", Toast.LENGTH_LONG).show();

                        }
                    } else {
//                        PivxURI pivxUri = new PivxURI(address);
                        final String usedAddress = map.get(QrUtils.URI_ADDRESS);
                        final Coin amount = Coin.parseCoin(map.get(QrUtils.URI_AMOUNT));
                        if (amount != null) {
                            final String memo = map.get(QrUtils.URI_DESC);
                            StringBuilder text = new StringBuilder();
                            text.append(getString(R.string.amount)).append(": ").append(amount.toFriendlyString());
                            if (memo != null) {
                                text.append("\n").append(getString(R.string.description)).append(": ").append(memo);
                            }

                            SimpleTextDialog dialogFragment = DialogsUtil.buildSimpleTextDialog(this,
                                    getString(R.string.payment_request_received),
                                    text.toString())
                                    .setOkBtnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(v.getContext(), SendActivity.class);
                                            intent.putExtra(INTENT_ADDRESS, usedAddress);
                                            intent.putExtra(INTENT_EXTRA_TOTAL_AMOUNT, amount);
                                            intent.putExtra(INTENT_MEMO, memo);
                                            startActivity(intent);
                                        }
                                    });
                            dialogFragment.setImgAlertRes(R.drawable.ic_send_action);
                            dialogFragment.setAlignBody(SimpleTextDialog.Align.LEFT);
                            dialogFragment.setImgAlertRes(R.drawable.ic_fab_recieve);
                            dialogFragment.show(getFragmentManager(), "payment_request_dialog");
                            return;
                        }

                    }
//                    DialogsUtil.showCreateAddressLabelDialog(this, usedAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Bad address", Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);

        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void updateBalance() {
        Coin availableBalance = pivxModule.getALLBalanceCoin();
        txt_value.setText(!availableBalance.isZero() ? availableBalance.toFriendlyString() : "0 ULO");
        Coin unnavailableBalance = pivxModule.getUnnavailableBalanceCoin();
        txt_unnavailable.setText(!unnavailableBalance.isZero() ? unnavailableBalance.toFriendlyString() : "0 ULO");
       if (pivxRate == null)
            pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());
        if (pivxRate != null) {
            txt_local_currency.setText(
                    "≈"+pivxApplication.getCentralFormats().format(
                            new BigDecimal(availableBalance.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + pivxRate.getCode()
            );
        } else {
            txt_local_currency.setText("0");
        }

    }

    @Override
    protected void onBlockchainStateChange() {
        if (blockchainState == BlockchainState.SYNCING) {
            AnimationUtils.fadeInView(container_syncing, 500);
        } else if (blockchainState == BlockchainState.SYNC) {
            AnimationUtils.fadeOutGoneView(container_syncing, 500);
        } else if (blockchainState == BlockchainState.NOT_CONNECTION) {
            AnimationUtils.fadeInView(container_syncing, 500);
        }
    }

    private void updateBlockchainState() {
        // Check if the activity is on foreground
        if (!isOnForeground) return;

        if (txt_sync_status != null) {
            String text = null;
            int color = 0;
            int imgSrc = 0;
            String progress = calculateBlockchainSyncProgress();
            switch (blockchainState) {
                case SYNC:
                    text = getString(R.string.sync);
                    color = Color.parseColor("#FF2B87A5");
                    imgSrc = 0;
                    break;
                case SYNCING:
                    text = getString(R.string.syncing) + " " + progress + "%";
                    color = Color.parseColor("#FF1F798F");
                    imgSrc = R.drawable.ic_header_unsynced;
                    break;
                case NOT_CONNECTION:
                    text = getString(R.string.not_connection);
                    color = Color.parseColor("#FF1F798F");
                    imgSrc = R.drawable.ic_header_unsynced;
                    break;
            }
            txt_sync_status.setText(text);
            txt_sync_status.setTextColor(color);
            if (imgSrc != 0) {
                img_sync.setImageResource(imgSrc);
                img_sync.setVisibility(View.VISIBLE);
            } else
                img_sync.setVisibility(View.INVISIBLE);
        }
    }

    private void checkState() {
        long now = System.currentTimeMillis();
//        long lastBlockTime = pivxApplication.getAppConf().getLastBestChainBlockTime();
//        if (lastBlockTime + OUT_OF_SYNC_TIME > now) {
        boolean isSynced=false;
        try {
            isSynced=pivxModule.isSyncWithNode();
        } catch (NoPeerConnectedException e) {
            e.printStackTrace();
            blockchainState = BlockchainState.NOT_CONNECTION;
            return;
        }
        if (isSynced) {
            // check if i'm syncing or i'm synched

            blockchainState = BlockchainState.SYNC;
//            long peerHeight = pivxModule.getConnectedPeerHeight();
//            if (peerHeight != -1) {
//                if (pivxModule.getChainHeight() + 10 > peerHeight) {
//                    blockchainState = BlockchainState.SYNC;
//                } else {
//                    blockchainState = BlockchainState.SYNCING;
//                }
//            } else {
//                blockchainState = BlockchainState.NOT_CONNECTION;
//            }
        } else {
//            if (pivxModule.isAnyPeerConnected()) {

            blockchainState = BlockchainState.SYNCING;
//                long peerHeight = pivxModule.getConnectedPeerHeight();
//                if (peerHeight != -1) {
//                    if (pivxModule.getChainHeight() + 10 > peerHeight) {
//                        blockchainState = BlockchainState.SYNC;
//                    } else {
//                        blockchainState = BlockchainState.SYNCING;
//                    }
//                } else {
//                    blockchainState = BlockchainState.NOT_CONNECTION;
//                }
//            } else {
//                blockchainState = BlockchainState.NOT_CONNECTION;
//            }
        }
    }


    private void getRate(){
        OkHttpUtils
                .get()
                .url("https://www.bitalong.net/Mapi/Ajax/whpj_usdt")
                .addParams("coins","eulo")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                    }

                    @Override
                    public void onResponse(String response, int id) {

                        LogHelper.v("response = "+response);
                        try {
                            JSONObject object = new JSONObject(response);

                            JSONObject data = (JSONObject) object.get("data");
                            double coin=data.getDouble("eulo");
                            long timestamp=object.getLong("timestamp");
                            PivxRate rate=new PivxRate("USD",new BigDecimal(coin),timestamp);
                            pivxModule.saveRate(rate);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                });
    }
}
