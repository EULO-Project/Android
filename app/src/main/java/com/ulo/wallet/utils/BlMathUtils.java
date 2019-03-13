package com.ulo.wallet.utils;

import android.text.TextUtils;

import com.google.zxing.common.StringUtils;

import java.math.BigDecimal;

/**
 * Created by DoubleDa on 2018/1/29.
 */

public class BlMathUtils {
    public static final int DEFULT_SCALE=8;//默认保留小数位
    public static final int DEFULT_RMB_SCALE=4;//默认保留小数位

    public static String getRmbNum(double num) {
        BigDecimal decimal = new BigDecimal(num);
        return getRmbNum(decimal); }
    public static String getRmbNum(String num) {
        BigDecimal decimal = new BigDecimal(num);
        return getRmbNum(decimal);
    }
    public static String getRmbNum(BigDecimal num) {
        return toShowDecimal(num.setScale(DEFULT_RMB_SCALE, BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    public static String toRMBString(double num) {
        return toShowDecimal(num,2);
    }
    public static String toRMBString(String num) {
        return toShowDecimal(num,2);
    }
    public static double getCoinNum(double num) {
        BigDecimal decimal = new BigDecimal(num);
        return decimal.setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double getThreeNum(double price) {
        BigDecimal decimal = new BigDecimal(price);
        return decimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double getSixNum(double num) {
        BigDecimal decimal = new BigDecimal(num);
        return decimal.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static float objectToFloat(Object object) {
        return Float.valueOf(String.valueOf(object));
    }

    public static String scienceNumToString(String s) {
        BigDecimal bd = new BigDecimal(s);
        return bd.toPlainString();
    }

    public static String getRandomNum() {
        return "&" + Math.random();
    }

    public static String generateRandomNum() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    public static String getCorrectNumber(String result, int num) {
        result = toShowDecimal(result);
        if (!TextUtils.isEmpty(result)) {
            if (result.length() <= num) {
                return result;
            } else {
                return result.substring(0, num+1);
            }
        }
        return result;
    }

    /**
     *
     * @param result
     * @param num   总字符串长度
     * @param scale 保留小数位数
     * @return
     */
    public static String getCorrectNumber(String result, int num,int scale) {
        result = toShowDecimal(result,scale);
        if (!TextUtils.isEmpty(result)) {
            if (result.length() <= num) {
                return result;
            } else {
                return result.substring(0, num+1);
            }
        }
        return result;
    }

    public static BigDecimal toBigDecimal(double num, int scale) {
        BigDecimal decimal = new BigDecimal(num);
        return decimal.setScale(scale, BigDecimal.ROUND_HALF_UP);

    }
    public static BigDecimal toBigDecimal(String num, int scale) {
        BigDecimal decimal = new BigDecimal(num);
        return decimal.setScale(scale, BigDecimal.ROUND_HALF_UP);

    }
    public static String toShowDecimal(String value) {

        return toShowDecimal(value, DEFULT_SCALE);
    }


    public static String toShowDecimal(String value, int scale) {
        if(isEqualZero(value)){
            return "0.0";
        }
        try {
            return toBigDecimal(value, scale).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
            return value + "";
        }
    }

    public static String toShowDecimal(double value) {
        return toShowDecimal(value, DEFULT_SCALE);

    }
    public static String toShowDecimal(float value) {
        return toShowDecimal(String.valueOf(value) , DEFULT_SCALE);

    }
    public static String toShowDecimal(double value, int scale) {

        return toShowDecimal(String.valueOf(value), scale);
    }

    public static boolean isEqualZero(String num){
        try{
           double d= Double.parseDouble(num);
           if(d==0){
               return true;
           }
        }catch (Exception e){
            return false;
        }
        return false;
    }

//    /**
//     * 数据转换为 万 亿  K　Ｍ为单位
//     * @param result
//     * @return
//     */
//    public static String getTransformationNumber(String  result) {
//
//        if(AppUtils.isCNLanguage()){
//            return getTransformationNumberCN(result);
//        }else{
//            return getTransformationNumberKM(result);
//        }
//    }
    /**
     * 数据转换为 万 亿 为单位
     * @param result
     * @return
     */
    public static String getTransformationNumberCN(String  result) {

        result = toShowDecimal(result);

        if (!TxUtils.isEmpty(result)) {

            int index =result.indexOf(".");

            if(index==-1){
                index=result.length();
            }
            if (index >= 8) {
                return toShowDecimal(Double.valueOf(result)/100000000,2)+"亿";
            } else if(index>=4) {
//			return result.substring(0, index + 2)+"万";
                return toShowDecimal(Double.valueOf(result)/10000,2)+"万";
            }else {
//			return result.substring(0, index + 8);
                return getCorrectNumber(result,8);
            }
        }
        return result;
    }
    /**
     * 数据转换为 K M 为单位
     * @param result
     * @return
     */
    public static String getTransformationNumberKM(String  result) {

        result = toShowDecimal(result);

        if (!TxUtils.isEmpty(result)) {

            int index =result.indexOf(".");

            if(index==-1){
                index=result.length();
            }
            if (index >= 6) {
                return toShowDecimal(Double.valueOf(result)/1000000,2)+"M";
            } else if(index>=3) {
//			return result.substring(0, index + 2)+"万";
                return toShowDecimal(Double.valueOf(result)/10000,2)+"K";
            }else {
//			return result.substring(0, index + 8);
                return getCorrectNumber(result,8);
            }
        }
        return result;
    }
    public static String doubleToPresent(double d){
        return toShowDecimal(d*100,2)+"%";
    }


    /**
     * 舍模式，值永远不回大
     * @param value
     * @return
     */
    public static String toShowDecimalRoundFloor(double value,int scale) {
            return toShowDecimalRoundFloor(String.valueOf(value),scale);

    }
    public static String toShowDecimalRoundFloor(double value) {

      return toShowDecimalRoundFloor(value,DEFULT_SCALE);

    }
    public static String toShowDecimalRoundFloor(String value) {

        return toShowDecimalRoundFloor(value, DEFULT_SCALE);
    }


    public static String toShowDecimalRoundFloor(String value, int scale) {
        if(isEqualZero(value)){
            return "0.0";
        }
        try {
            BigDecimal decimal = new BigDecimal(value);
            return decimal.setScale(scale, BigDecimal.ROUND_FLOOR).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
            return value + "";
        }
    }
}
