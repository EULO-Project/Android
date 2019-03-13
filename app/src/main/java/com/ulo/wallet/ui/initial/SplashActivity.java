package com.ulo.wallet.ui.initial;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.ulo.wallet.R;
import com.ulo.wallet.UloApplication;
import com.ulo.wallet.ui.main.WalletActivity;


/**
 * Created by Neoperol on 6/13/17.
 */

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        initView();
    }


    private void initView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Init First Run Params
                if (UloApplication.getInstance().getAppConf().isAppInit()){
                    Intent intent = new Intent(SplashActivity.this, WalletActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000);

    }
}
