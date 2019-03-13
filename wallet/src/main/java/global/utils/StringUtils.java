package global.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import wallet.Coin;

/**
 * Created by EULO Developer team on 2019/1/18.
 */

public class StringUtils {



    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }


}
