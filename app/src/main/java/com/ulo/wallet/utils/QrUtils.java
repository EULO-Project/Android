package com.ulo.wallet.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.HashMap;
import java.util.Map;

import global.utils.StringUtils;

/**
 * Created by mati on 08/12/16.
 */

public class QrUtils {

    public static final String URI_ADDRESS="eulo";
    public static final String URI_COIN="coin";
    public static final String URI_AMOUNT="amount";
    public static final String URI_DESC="message";


    public static Bitmap encodeAsBitmap(String str, int widht, int height, int qrColor, int backgroundColor) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, widht, height, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? qrColor : backgroundColor;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }


    public static String toShareQRuri(String address, String amount, String memo) {

//        return URI_ADDRESS+"="+address+"&"+URI_COIN+"=eulo&"+URI_AMOUNT+"="+amount+"&"+URI_DESC+"="+memo;
        return URI_ADDRESS+":"+address+"?"+URI_AMOUNT+"="+amount+"&"+URI_DESC+"="+memo;
    }

    //eulo:UNa8EiS9DAjRWd8JN1HvVneY5PYo7Y2Kvc?amount=1234.00000000&label=1234&message=1234
    public static Map<String,String> getUriParams(String url) {
        Map<String,String> map=new HashMap<>();
//        Uri uri = Uri.parse(url);
        Log.e("zpan", "=uri= " + url);
        // 得到参数字符串
//        String zpParams = uri.getEncodedQuery();
//        String zpParams = url;
//        Log.e("zpan", "=zpParams= " + zpParams);


        String address=getUriAddress(url);
        if(!StringUtils.isEmpty(address)){
            map.put(URI_ADDRESS,address);
        }
        map.putAll(UrlUtil.URLRequest(url));
//        // 拆分获得单个参数
//        if (!StringUtils.isEmpty(zpParams)) {
//
//            if(url.contains("?")){
//                 zpParams = Uri.parse(url).getEncodedQuery();
//            }
//            String[] params = zpParams.split("&");
//
//            for (String param : params) {
//                String[] key_Value = param.split("=");
//                if (key_Value != null && key_Value.length == 2) {
//                    map.put(key_Value[0],key_Value[1]);
//                    Log.e("zpan", "=key= " + key_Value[0] + " =value= " + key_Value[1]);
//                }
//            }
//        }
        return map;
    }

    public static String getUriAddress(String url){
        String front=UrlUtil.UrlPage(url);

        if(StringUtils.isEmpty(front)){
            return "";
        }
        String[] arrSplit = null;
        arrSplit=front.split("[:]");
        if(front.length() > 0){
            if(arrSplit.length > 1){
                return arrSplit[1];
            }
        }
        return "";
    }
}