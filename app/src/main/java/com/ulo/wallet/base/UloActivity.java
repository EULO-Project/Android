package com.ulo.wallet.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ulo.wallet.R;
import com.ulo.wallet.UloApplication;
import com.ulo.wallet.base.dialog.BitalongProgressDialog;
import com.ulo.wallet.base.dialog.SimpleTextDialog;
import com.ulo.wallet.utils.DialogsUtil;

import global.UloModule;

import static com.ulo.wallet.service.IntentsConstants.*;


/**
 * 
 */
public class UloActivity extends AppCompatActivity {

    protected UloApplication pivxApplication;
    protected UloModule pivxModule;

    protected LocalBroadcastManager localBroadcastManager;
    private static final IntentFilter intentFilter = new IntentFilter(ACTION_TRUSTED_PEER_CONNECTION_FAIL);
    private static final IntentFilter errorIntentFilter = new IntentFilter(ACTION_STORED_BLOCKCHAIN_ERROR);

    protected boolean isOnForeground = false;

    private BroadcastReceiver trustedPeerConnectionDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_TRUSTED_PEER_CONNECTION_FAIL)) {
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleErrorTextDialog(context, R.string.title_no_trusted_peer_connection,R.string.message_no_trusted_peer_connection);
                simpleTextDialog.show(getFragmentManager(),"fail_node_connection_dialog");
            }else if (action.equals(ACTION_STORED_BLOCKCHAIN_ERROR)){
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleErrorTextDialog(context,R.string.title_blockstore_error,R.string.message_blockstore_error);
                simpleTextDialog.show(getFragmentManager(),"blockstore_error_dialog");
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pivxApplication = UloApplication.getInstance();
        pivxModule = pivxApplication.getModule();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnForeground = true;
        localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver,intentFilter);
        localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver,errorIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnForeground = false;
        localBroadcastManager.unregisterReceiver(trustedPeerConnectionDownReceiver);
    }


    public void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }


    private BitalongProgressDialog mProgressDialog;
    public void showProgressDialog() {
        if(isDestroyed()||isFinishing()){
            return;
        }
        dismissProgressDialog();
        mProgressDialog = new BitalongProgressDialog(this, R.style.CustomDialog);
        mProgressDialog.show();
    }
    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private SimpleTextDialog errorDialog;

    public void showErrorDialog(int resStr) {
        showErrorDialog(getString(resStr));
    }

    public void showErrorDialog(String message) {
        if (errorDialog == null) {
            errorDialog = DialogsUtil.buildSimpleErrorTextDialog(this, getResources().getString(R.string.title_blockstore_error), message);
        } else {
            errorDialog.setBody(message);
        }
        errorDialog.show(getFragmentManager(), getResources().getString(R.string.send_error_dialog_tag));
    }

}
