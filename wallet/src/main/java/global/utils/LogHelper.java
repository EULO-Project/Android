package global.utils;

import android.util.Log;

import wallet.BuildConfig;

/**
 * Created by EULO Developer team on 2019/1/19.
 */

public class LogHelper {
    public static final String Tag="eulo";
    public static void  v(String str){
        if(BuildConfig.DEBUG){
            Log.v(Tag,str);
        }
    }
}
