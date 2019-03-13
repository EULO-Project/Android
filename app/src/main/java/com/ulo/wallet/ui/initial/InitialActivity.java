package com.ulo.wallet.ui.initial;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ulo.wallet.UloApplication;
import com.ulo.wallet.ui.main.WalletActivity;
import com.ulo.wallet.utils.AppConf;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import global.PivxRate;
import okhttp3.Call;


/**
 * Created by furszy on 8/19/17.
 */

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UloApplication mApplication = UloApplication.getInstance();
        AppConf appConf = mApplication.getAppConf();
        // show report dialog if something happen with the previous process
        Intent intent;
        if (!appConf.isAppInit() || appConf.isSplashSoundEnabled()) {
            intent = new Intent(this, SplashActivity.class);
        } else {
            intent = new Intent(this, WalletActivity.class);
        }
        startActivity(intent);
        finish();
    }


}
