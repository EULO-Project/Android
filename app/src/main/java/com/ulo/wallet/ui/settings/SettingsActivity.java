package com.ulo.wallet.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ulo.wallet.BuildConfig;
import com.ulo.wallet.R;
import com.ulo.wallet.base.BaseDrawerActivity;
import com.ulo.wallet.base.dialog.SimpleTwoButtonsDialog;
import com.ulo.wallet.ui.initial.TutorialActivity;
import com.ulo.wallet.ui.restore.RestoreActivity;
import com.ulo.wallet.utils.DialogsUtil;
import com.ulo.wallet.utils.IntentsUtils;
import com.ulo.wallet.utils.NavigationUtils;

import java.io.IOException;
import java.util.ArrayList;

import global.PivxModuleImp;
import wallet.WalletManager;

import static com.ulo.wallet.ui.initial.TutorialActivity.INTENT_EXTRA_INFO_TUTORIAL;

/**
 * Created by Neoperol on 5/11/17.
 */

public class SettingsActivity extends BaseDrawerActivity implements View.OnClickListener {
    private Switch videoSwitch;
    private Button buttonBackup;
    private Button buttonRestore;
    private Button btn_export_pub_key;
    private Button btn_import_xpub;
    private Button buttonChange;
    private Button btn_change_node;
    private Button btn_reset_blockchain;
    private Button btn_report;
    private Button btn_support;
    private Button buttonTutorial;
    private TextView textAbout, text_rates;
    private TextView txt_network_info;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_settings, container);
        setTitle("Settings");

        TextView app_version = (TextView) findViewById(R.id.app_version);
        app_version.setText(BuildConfig.VERSION_NAME);

        txt_network_info = (TextView) findViewById(R.id.txt_network_info);

        textAbout = (TextView) findViewById(R.id.text_about);
        String text = "Made by<br> <font color=#55476c>eulo.io</font> <br>(c) www.eulo.io";
        textAbout.setText(Html.fromHtml(text));
        // Open Backup Wallet
        buttonBackup = (Button) findViewById(R.id.btn_backup_wallet);
        buttonBackup.setOnClickListener(this);

        // Open Restore Wallet
        buttonRestore = (Button) findViewById(R.id.btn_restore_wallet);
        buttonRestore.setOnClickListener(this);

        btn_export_pub_key = (Button) findViewById(R.id.btn_export_pub_key);
        btn_export_pub_key.setOnClickListener(this);

        btn_import_xpub = (Button) findViewById(R.id.btn_import_xpub);
        btn_import_xpub.setOnClickListener(this);

        // Open Change Pincode
        buttonChange = (Button) findViewById(R.id.btn_change_pincode);
        buttonChange.setOnClickListener(this);

        btn_change_node = (Button) findViewById(R.id.btn_change_node);
        btn_change_node.setOnClickListener(this);

        btn_reset_blockchain = (Button) findViewById(R.id.btn_reset_blockchain);
        btn_reset_blockchain.setOnClickListener(this);

        // rates
        findViewById(R.id.btn_rates).setOnClickListener(this);
        text_rates = (TextView) findViewById(R.id.text_rates);
        text_rates.setText(pivxApplication.getAppConf().getSelectedRateCoin());

        // Open Network Monitor
        buttonChange = (Button) findViewById(R.id.btn_network);
        buttonChange.setOnClickListener(this);

        btn_report = (Button) findViewById(R.id.btn_report);
        btn_report.setOnClickListener(this);

        btn_support = (Button) findViewById(R.id.btn_support);
        btn_support.setOnClickListener(this);

        // Open Tutorial
        buttonTutorial = (Button) findViewById(R.id.btn_tutorial);
        buttonTutorial.setOnClickListener(this);

        // Video Switch
        videoSwitch = (Switch) findViewById(R.id.videoSwitch);
        videoSwitch.setChecked(pivxApplication.getAppConf().isSplashSoundEnabled());
        videoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                pivxApplication.getAppConf().setSplashSound(checked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(2);
        updateNetworkStatus();
        text_rates.setText(pivxApplication.getAppConf().getSelectedRateCoin());
    }

    private void updateNetworkStatus() {
        // Check if the activity is on foreground
        if (!isOnForeground) return;
        txt_network_info.setText(
                Html.fromHtml(
                        "Network<br><font color=#55476c>" + "io.eulo.seed1" +
                                "</font><br>" +
                                "Height<br><font color=#55476c>" + pivxModule.getChainHeight() + "</font><br>" +
                                "Protocol Version<br><font color=#55476c>" +
                                pivxModule.getProtocolVersion() + "</font>"

                )
        );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_backup_wallet) {
//            Intent myIntent = new Intent(v.getContext(), SettingsBackupActivity.class);
//            startActivity(myIntent);
        } else if (id == R.id.btn_tutorial) {
//            Intent myIntent = new Intent(v.getContext(), TutorialActivity.class);
//            myIntent.putExtra(INTENT_EXTRA_INFO_TUTORIAL, true);
//            startActivity(myIntent);
        } else if (id == R.id.btn_restore_wallet) {
//            Intent myIntent = new Intent(v.getContext(), RestoreActivity.class);
//            startActivity(myIntent);
        } else if (id == R.id.btn_change_pincode) {
            Intent myIntent = new Intent(v.getContext(), SettingsPincodeActivity.class);
            startActivity(myIntent);
        } else if (id == R.id.btn_network) {
//            startActivity(new Intent(v.getContext(), SettingsNetworkActivity.class));
        } else if (id == R.id.btn_change_node) {
            startActivity(new Intent(v.getContext(), StartNodeActivity.class));
        } else if (id == R.id.btn_reset_blockchain) {
            launchResetBlockchainDialog();
        } else if (id == R.id.btn_report) {
            launchReportDialog();
        } else if (id == R.id.btn_support) {
            IntentsUtils.startSend(
                    this,
                    getString(R.string.support_subject),
                    getString(R.string.report_issue_dialog_message_issue),
                    new ArrayList<Uri>()
            );
        } else if (id == R.id.btn_export_pub_key) {
//            startActivity(new Intent(v.getContext(), ExportKeyActivity.class));
        } else if (id == R.id.btn_import_xpub) {
//            startActivity(new Intent(v.getContext(), SettingsWatchOnly.class));
        } else if (id == R.id.btn_rates) {
//            startActivity(new Intent(v.getContext(), SettingsRatesActivity.class));
        }
    }

    private void launchResetBlockchainDialog() {
        SimpleTwoButtonsDialog dialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                this,
                getString(R.string.dialog_reset_blockchain_title),
                getString(R.string.dialog_reset_blockchain_body),
                new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                    @Override
                    public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
//                        pivxApplication.stopBlockchain();
                        try {
                            pivxModule.resetBlockChain();
                            Toast.makeText(SettingsActivity.this, R.string.reseting_blockchain, Toast.LENGTH_LONG).show();
                        } catch (WalletManager.WalletErrorException e) {
                            e.printStackTrace();
                            showErrorDialog(e.getMessage());
                        }
                        dialog.dismiss();

                    }

                    @Override
                    public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                        dialog.dismiss();
                    }
                }
        );
        dialog.setLeftBtnText(R.string.button_cancel)
                .setRightBtnText(R.string.button_ok);
        dialog.show();
    }

    private void launchReportDialog() {

        showToast("Report success");
//        ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(
//                this,
//                "pivx.org.pivxwallet.myfileprovider",
//                R.string.report_issuea_dialog_title,
//                R.string.report_issue_dialog_message_issue) {
//            @Nullable
//            @Override
//            protected CharSequence subject() {
//                return PivxContext.REPORT_SUBJECT_ISSUE + " " + pivxApplication.getVersionName();
//            }
//
//            @Nullable
//            @Override
//            protected CharSequence collectApplicationInfo() throws IOException {
//                final StringBuilder applicationInfo = new StringBuilder();
//                CrashReporter.appendApplicationInfo(applicationInfo, pivxApplication);
//                return applicationInfo;
//            }
//
//            @Nullable
//            @Override
//            protected CharSequence collectStackTrace() throws IOException {
//                return null;
//            }
//
//            @Nullable
//            @Override
//            protected CharSequence collectDeviceInfo() throws IOException {
//                final StringBuilder deviceInfo = new StringBuilder();
//                CrashReporter.appendDeviceInfo(deviceInfo, SettingsActivity.this);
//                return deviceInfo;
//            }
//
//            @Nullable
//            @Override
//            protected CharSequence collectWalletDump() throws IOException {
//                return ((PivxModuleImp) pivxModule).getWallet().toString(false, true, true, null);
//            }
//        };
//        dialog.show();
    }

    @Override
    protected void onBlockchainStateChange() {
        updateNetworkStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationUtils.goBackToHome(this);
    }
}
