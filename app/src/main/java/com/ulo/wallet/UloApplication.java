package com.ulo.wallet;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor;
import com.ulo.wallet.model.WalletConfImp;
import com.ulo.wallet.utils.AppConf;
import com.ulo.wallet.utils.CentralFormats;
import com.ulo.wallet.utils.ContactsStore;
import com.zhy.http.okhttp.OkHttpUtils;

import org.acra.ACRA;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import global.ContextWrapper;
import global.PivtrumGlobalData;
import global.PivxModuleImp;
import global.PivxRate;
import global.UloModule;
import global.WalletConfiguration;
import okhttp3.OkHttpClient;
import pivtrum.NetworkConf;
import wallet.rate.db.NodeDb;
import wallet.rate.db.RateDb;

import static com.ulo.wallet.service.IntentsConstants.ACTION_RESET_BLOCKCHAIN;

/**
 * Created by EULO Developer team on 2019/1/10.
 */

public class UloApplication extends Application implements ContextWrapper {

    private static UloApplication instance;

    public static long TIME_OUT = 10000;

    public static UloApplication getInstance(){
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;

        appConf = new AppConf(getSharedPreferences(AppConf.PREFERENCE_NAME, MODE_PRIVATE));


        try {
            PackageManager manager = getPackageManager();
            info = manager.getPackageInfo(this.getPackageName(), 0);
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            //Bugsee.launch(this, "9b3473f1-984c-4f70-9aef-b0cf485839fd");

            // The following line triggers the initialization of ACRA
            ACRA.init(this);
            //if (BuildConfig.DEBUG)
            //    new ANRWatchDog().start();
            // Default network conf for localhost test
            networkConf = new NetworkConf();
            appConf = new AppConf(getSharedPreferences(AppConf.PREFERENCE_NAME, MODE_PRIVATE));
            centralFormats = new CentralFormats(appConf);
            WalletConfiguration walletConfiguration = new WalletConfImp(getSharedPreferences("pivx_wallet", MODE_PRIVATE));
            //todo: add this on the initial wizard..
            //walletConfiguration.saveTrustedNode(HardcodedConstants.TESTNET_HOST,0);
            //AddressStore addressStore = new SnappyStore(getDirPrivateMode("address_store").getAbsolutePath());
            ContactsStore contactsStore = new ContactsStore(this);
//            uloModule = new PivxModuleImp(this, walletConfiguration, contactsStore, new RateDb(this), new WalletBackupHelper());
            uloModule = new PivxModuleImp(this, walletConfiguration, contactsStore,new RateDb(this));
            uloModule.start();

            PivtrumGlobalData.getInstance().setNodeDb(new NodeDb(this));

        } catch (Exception e) {
            e.printStackTrace();
        }


        disableAPIDialog();
    }

    private void disableAPIDialog() {
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initOkhttp() {
        OkHttpClient okHttpClient;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)//设置写的超时时间
//                .sslSocketFactory(new NoSSLv3Factory())
//                .addInterceptor(new LogInterceptor())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
        if (BuildConfig.DEBUG) {

            builder.addInterceptor(new LogInterceptor());
        }
        okHttpClient = builder.build();

        OkHttpUtils.initClient(okHttpClient);
    }
    private AppConf appConf;
    private UloModule uloModule;
    private NetworkConf networkConf;
    private ActivityManager activityManager;
    private PackageInfo info;


    private CentralFormats centralFormats;
    public AppConf getAppConf() {
        return appConf;
    }

    public UloModule getModule() {
        return uloModule;
    }
    public CentralFormats getCentralFormats() {
        return centralFormats;
    }




    @Override
    public FileOutputStream openFileOutputPrivateMode(String name) throws FileNotFoundException {
        return openFileOutput(name, MODE_PRIVATE);
    }

    @Override
    public File getDirPrivateMode(String name) {
        return getDir(name, MODE_PRIVATE);
    }

    @Override
    public InputStream openAssestsStream(String name) throws IOException {
        return getAssets().open(name);
    }

    @Override
    public boolean isMemoryLow() {
        final int memoryClass = activityManager.getMemoryClass();
        return memoryClass <= uloModule.getConf().getMinMemoryNeeded();
    }

    @Override
    public String getVersionName() {
        return info.versionName;
    }

    @Override
    public void stopBlockchain() {
//        Intent intent = new Intent(this, PivxWalletService.class);
//        intent.setAction(ACTION_RESET_BLOCKCHAIN);
//        startService(intent);
    }

    @Override
    public String getPinCode() {
        return getAppConf().getPincode();
    }
}
