//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package wallet;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.google.common.primitives.Longs;
import java.io.Serializable;
import java.math.BigDecimal;

import org.pivxj.core.Monetary;

public final class Coin implements Monetary, Comparable<Coin>, Serializable {
    public static final int SMALLEST_UNIT_EXPONENT = 8;
    private static final long COIN_VALUE = LongMath.pow(10L, 8);
    public static final Coin ZERO = valueOf(0L);
    public static final Coin COIN;
    public static final Coin CENT;
    public static final Coin MILLICOIN;
    public static final Coin MICROCOIN;
    public static final Coin SATOSHI;
    public static final Coin FIFTY_COINS;
    public static final Coin NEGATIVE_SATOSHI;
    public final Long value;
    private final Long MAX_SATOSHIS;
    private static final MonetaryFormat FRIENDLY_FORMAT;
    private static final MonetaryFormat PLAIN_FORMAT;

    private Coin(Long satoshis) {
        this.MAX_SATOSHIS = COIN_VALUE * 22000000L;
        this.value = satoshis;
    }

    public static Coin valueOf(Long satoshis) {
        return new Coin(satoshis);
    }

    public int smallestUnitExponent() {
        return 8;
    }

    public long getValue() {
        return this.value;
    }

    public static Coin valueOf(int coins, int cents) {
        Preconditions.checkArgument(cents < 100);
        Preconditions.checkArgument(cents >= 0);
        Preconditions.checkArgument(coins >= 0);
        Coin coin = COIN.multiply((long)coins).add(CENT.multiply((long)cents));
        return coin;
    }

    public static Coin parseCoin(String str) {
        try {
            long satoshis = (new BigDecimal(str)).movePointRight(8).toBigIntegerExact().longValue();
//            long satoshis = (new BigDecimal(str)).toBigIntegerExact().longValue();
            return valueOf(satoshis);
        } catch (ArithmeticException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    public Coin add(Coin value) {
        return new Coin(LongMath.checkedAdd(this.value, value.value));
    }

    public Coin plus(Coin value) {
        return this.add(value);
    }

    public Coin subtract(Coin value) {
        return new Coin(LongMath.checkedSubtract(this.value, value.value));
    }

    public Coin minus(Coin value) {
        return this.subtract(value);
    }

    public Coin multiply(long factor) {
        return new Coin(LongMath.checkedMultiply(this.value, factor));
    }

    public Coin times(long factor) {
        return this.multiply(factor);
    }

    public Coin times(int factor) {
        return this.multiply((long)factor);
    }

    public Coin divide(long divisor) {
        return new Coin(this.value / divisor);
    }

    public Coin div(long divisor) {
        return this.divide(divisor);
    }

    public Coin div(int divisor) {
        return this.divide((long)divisor);
    }

    public Coin[] divideAndRemainder(long divisor) {
        return new Coin[]{new Coin(this.value / divisor), new Coin(this.value % divisor)};
    }

    public long divide(Coin divisor) {
        return this.value / divisor.value;
    }

    public boolean isPositive() {
        return this.signum() == 1;
    }

    public boolean isNegative() {
        return this.signum() == -1;
    }

    public boolean isZero() {
        return this.signum() == 0;
    }

    public boolean isGreaterThan(Coin other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(Coin other) {
        return this.compareTo(other) < 0;
    }

    public Coin shiftLeft(int n) {
        return new Coin(this.value << n);
    }

    public Coin shiftRight(int n) {
        return new Coin(this.value >> n);
    }

    public int signum() {
        return this.value == 0L?0:(this.value < 0L?-1:1);
    }

    public Coin negate() {
        return new Coin(-this.value);
    }

    public long longValue() {
        return this.value;
    }

    public String toFriendlyString() {
        return FRIENDLY_FORMAT.format(this).toString();
    }

    public String toPlainString() {
        return PLAIN_FORMAT.format(this).toString();
    }

    public String toString() {
        return Long.toString(this.value);
    }

    public boolean equals(Object o) {
        return this == o?true:(o != null && this.getClass() == o.getClass()?this.value == ((Coin)o).value:false);
    }

//    public int hashCode() {
//        return Integer.parseInt(this.value);
//    }

    public int compareTo(Coin other) {
        return Longs.compare(this.value, other.value);
    }

    static {
        COIN = valueOf(COIN_VALUE);
        CENT = COIN.divide(100L);
        MILLICOIN = COIN.divide(1000L);
        MICROCOIN = MILLICOIN.divide(1000L);
        SATOSHI = valueOf(1L);
        FIFTY_COINS = COIN.multiply(50L);
        NEGATIVE_SATOSHI = valueOf(-1L);
        FRIENDLY_FORMAT = MonetaryFormat.BTC.minDecimals(2).repeatOptionalDecimals(1, 6).postfixCode();
        PLAIN_FORMAT = MonetaryFormat.BTC.minDecimals(0).repeatOptionalDecimals(1, 8).noCode();
    }
}
