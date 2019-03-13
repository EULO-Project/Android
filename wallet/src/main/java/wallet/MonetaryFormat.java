//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package wallet;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.pivxj.core.Monetary;
import org.pivxj.utils.Fiat;

public final class MonetaryFormat {
    public static final MonetaryFormat BTC = (new MonetaryFormat()).shift(0).minDecimals(2).repeatOptionalDecimals(2, 3);
    public static final MonetaryFormat MBTC = (new MonetaryFormat()).shift(3).minDecimals(2).optionalDecimals(new int[]{2});
    public static final MonetaryFormat UBTC = (new MonetaryFormat()).shift(6).minDecimals(0).optionalDecimals(new int[]{2});
    public static final MonetaryFormat FIAT = (new MonetaryFormat()).shift(0).minDecimals(2).repeatOptionalDecimals(2, 1);
    public static final String CODE_BTC = "ULO";
    public static final String CODE_MBTC = "mULO";
    public static final String CODE_UBTC = "µULO";
    public static final int MAX_DECIMALS = 8;
    private final char negativeSign;
    private final char positiveSign;
    private final char zeroDigit;
    private final char decimalMark;
    private final int minDecimals;
    private final List<Integer> decimalGroups;
    private final int shift;
    private final RoundingMode roundingMode;
    private final String[] codes;
    private final char codeSeparator;
    private final boolean codePrefixed;
    private static final String DECIMALS_PADDING = "0000000000000000";

    public MonetaryFormat negativeSign(char negativeSign) {
        Preconditions.checkArgument(!Character.isDigit(negativeSign));
        Preconditions.checkArgument(negativeSign > 0);
        return negativeSign == this.negativeSign?this:new MonetaryFormat(negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat positiveSign(char positiveSign) {
        Preconditions.checkArgument(!Character.isDigit(positiveSign));
        return positiveSign == this.positiveSign?this:new MonetaryFormat(this.negativeSign, positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat digits(char zeroDigit) {
        return zeroDigit == this.zeroDigit?this:new MonetaryFormat(this.negativeSign, this.positiveSign, zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat decimalMark(char decimalMark) {
        Preconditions.checkArgument(!Character.isDigit(decimalMark));
        Preconditions.checkArgument(decimalMark > 0);
        return decimalMark == this.decimalMark?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat minDecimals(int minDecimals) {
        return minDecimals == this.minDecimals?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat optionalDecimals(int... groups) {
        List<Integer> decimalGroups = new ArrayList(groups.length);
        int[] var3 = groups;
        int var4 = groups.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int group = var3[var5];
            decimalGroups.add(Integer.valueOf(group));
        }

        return new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat repeatOptionalDecimals(int decimals, int repetitions) {
        Preconditions.checkArgument(repetitions >= 0);
        List<Integer> decimalGroups = new ArrayList(repetitions);

        for(int i = 0; i < repetitions; ++i) {
            decimalGroups.add(Integer.valueOf(decimals));
        }

        return new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat shift(int shift) {
        return shift == this.shift?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat roundingMode(RoundingMode roundingMode) {
        return roundingMode == this.roundingMode?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat noCode() {
        return this.codes == null?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, (String[])null, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat code(int codeShift, String code) {
        Preconditions.checkArgument(codeShift >= 0);
        String[] codes = null == this.codes?new String[8]:(String[])Arrays.copyOf(this.codes, this.codes.length);
        codes[codeShift] = code;
        return new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat codeSeparator(char codeSeparator) {
        Preconditions.checkArgument(!Character.isDigit(codeSeparator));
        Preconditions.checkArgument(codeSeparator > 0);
        return codeSeparator == this.codeSeparator?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat prefixCode() {
        return this.codePrefixed?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, true);
    }

    public MonetaryFormat postfixCode() {
        return !this.codePrefixed?this:new MonetaryFormat(this.negativeSign, this.positiveSign, this.zeroDigit, this.decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, false);
    }

    public MonetaryFormat withLocale(Locale locale) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        char negativeSign = dfs.getMinusSign();
        char zeroDigit = dfs.getZeroDigit();
        char decimalMark = dfs.getMonetaryDecimalSeparator();
        return new MonetaryFormat(negativeSign, this.positiveSign, zeroDigit, decimalMark, this.minDecimals, this.decimalGroups, this.shift, this.roundingMode, this.codes, this.codeSeparator, this.codePrefixed);
    }

    public MonetaryFormat() {
        this.negativeSign = 45;
        this.positiveSign = 0;
        this.zeroDigit = 48;
        this.decimalMark = 46;
        this.minDecimals = 2;
        this.decimalGroups = null;
        this.shift = 0;
        this.roundingMode = RoundingMode.HALF_UP;
        this.codes = new String[8];
        this.codes[0] = "ULO";
        this.codes[3] = "mULO";
        this.codes[6] = "µULO";
        this.codeSeparator = 32;
        this.codePrefixed = true;
    }

    private MonetaryFormat(char negativeSign, char positiveSign, char zeroDigit, char decimalMark, int minDecimals, List<Integer> decimalGroups, int shift, RoundingMode roundingMode, String[] codes, char codeSeparator, boolean codePrefixed) {
        this.negativeSign = negativeSign;
        this.positiveSign = positiveSign;
        this.zeroDigit = zeroDigit;
        this.decimalMark = decimalMark;
        this.minDecimals = minDecimals;
        this.decimalGroups = decimalGroups;
        this.shift = shift;
        this.roundingMode = roundingMode;
        this.codes = codes;
        this.codeSeparator = codeSeparator;
        this.codePrefixed = codePrefixed;
    }

    public CharSequence format(Monetary monetary) {
        int maxDecimals = this.minDecimals;
        int group;
        if(this.decimalGroups != null) {
            for(Iterator var3 = this.decimalGroups.iterator(); var3.hasNext(); maxDecimals += group) {
                group = ((Integer)var3.next()).intValue();
            }
        }

        int smallestUnitExponent = monetary.smallestUnitExponent();
        Preconditions.checkState(maxDecimals <= smallestUnitExponent, "The maximum possible number of decimals (%s) cannot exceed %s.", new Object[]{Integer.valueOf(maxDecimals), Integer.valueOf(smallestUnitExponent)});
        long satoshis = Math.abs(monetary.getValue());
        long precisionDivisor = LongMath.checkedPow(10L, smallestUnitExponent - this.shift - maxDecimals);
        satoshis = LongMath.checkedMultiply(LongMath.divide(satoshis, precisionDivisor, this.roundingMode), precisionDivisor);
        long shiftDivisor = LongMath.checkedPow(10L, smallestUnitExponent - this.shift);
        long numbers = satoshis / shiftDivisor;
        long decimals = satoshis % shiftDivisor;
        String decimalsStr = String.format(Locale.US, "%0" + (smallestUnitExponent - this.shift) + "d", new Object[]{Long.valueOf(decimals)});
        StringBuilder str = new StringBuilder(decimalsStr);

        while(str.length() > this.minDecimals && str.charAt(str.length() - 1) == 48) {
            str.setLength(str.length() - 1);
        }

        int i = this.minDecimals;
        int d;
        if(this.decimalGroups != null) {
            label68:
            for(Iterator var17 = this.decimalGroups.iterator(); var17.hasNext(); i += d) {
                d = ((Integer)var17.next()).intValue();
                if(str.length() > i && str.length() < i + d) {
                    while(true) {
                        if(str.length() >= i + d) {
                            break label68;
                        }

                        str.append('0');
                    }
                }
            }
        }

        if(str.length() > 0) {
            str.insert(0, this.decimalMark);
        }

        str.insert(0, numbers);
        if(monetary.getValue() < 0L) {
            str.insert(0, this.negativeSign);
        } else if(this.positiveSign != 0) {
            str.insert(0, this.positiveSign);
        }

        if(this.codes != null) {
            if(this.codePrefixed) {
                str.insert(0, this.codeSeparator);
                str.insert(0, this.code());
            } else {
                str.append(this.codeSeparator);
                str.append(this.code());
            }
        }

        if(this.zeroDigit != 48) {
            int offset = this.zeroDigit - 48;

            for(d = 0; d < str.length(); ++d) {
                char c = str.charAt(d);
                if(Character.isDigit(c)) {
                    str.setCharAt(d, (char)(c + offset));
                }
            }
        }

        return str;
    }

    public Coin parse(String str) throws NumberFormatException {
        return Coin.valueOf(this.parseValue(str, 8));
    }

    public Fiat parseFiat(String currencyCode, String str) throws NumberFormatException {
        return Fiat.valueOf(currencyCode, this.parseValue(str, 8));
    }

    private long parseValue(String str, int smallestUnitExponent) {
        Preconditions.checkState("0000000000000000".length() >= smallestUnitExponent);
        if(str.isEmpty()) {
            throw new NumberFormatException("empty string");
        } else {
            char first = str.charAt(0);
            if(first == this.negativeSign || first == this.positiveSign) {
                str = str.substring(1);
            }

            int decimalMarkIndex = str.indexOf(this.decimalMark);
            String numbers;
            String decimals;
            if(decimalMarkIndex != -1) {
                numbers = str.substring(0, decimalMarkIndex);
                decimals = (str + "0000000000000000").substring(decimalMarkIndex + 1);
                if(decimals.indexOf(this.decimalMark) != -1) {
                    throw new NumberFormatException("more than one decimal mark");
                }
            } else {
                numbers = str;
                decimals = "0000000000000000";
            }

            String satoshis = numbers + decimals.substring(0, smallestUnitExponent - this.shift);
            char[] var8 = satoshis.toCharArray();
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                char c = var8[var10];
                if(!Character.isDigit(c)) {
                    throw new NumberFormatException("illegal character: " + c);
                }
            }

            long value = Long.parseLong(satoshis);
            if(first == this.negativeSign) {
                value = -value;
            }

            return value;
        }
    }

    public String code() {
        if(this.codes == null) {
            return null;
        } else if(this.codes[this.shift] == null) {
            throw new NumberFormatException("missing code for shift: " + this.shift);
        } else {
            return this.codes[this.shift];
        }
    }
}
