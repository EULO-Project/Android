/**
  * Copyright 2019 bejson.com 
  */
package wallet.transaction;
import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;
import java.util.List;

import wallet.Coin;

/**
 * Auto-generated: 2019-01-18 16:33:44
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Transaction   implements Serializable,Comparable<Transaction> {

    private Long amount;
    private long blockHeight;
    private long confirms;
    private String description;
    private Long fee;
    private String hash;
    private boolean isSpend;
    private int lockTime;
    private List<Recieve> recieve;
    private List<Spend> spend;
    private long timestamp;
    private long weights;



    private int errno=1;
    private String message;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAmount(Long amount) {
         this.amount = amount;
     }
     public Coin getAmount() {
         return Coin.valueOf(amount);
     }

    public void setBlockHeight(long blockHeight) {
         this.blockHeight = blockHeight;
     }
     public long getBlockHeight() {
         return blockHeight;
     }

    public void setConfirms(long confirms) {
         this.confirms = confirms;
     }
     public long getConfirms() {
         return confirms;
     }

    public void setDescription(String description) {
         this.description = description;
     }
     public String getDescription() {
         return description;
     }

    public void setFee(Long fee) {
         this.fee = fee;
     }
     public Coin getFee() {
         return Coin.valueOf(fee);
     }

    public void setHash(String hash) {
         this.hash = hash;
     }
     public String getHash() {
         return hash;
     }

    public void setIsSpend(boolean isSpend) {
         this.isSpend = isSpend;
     }
     public boolean getIsSpend() {
         return isSpend;
     }

    public void setLockTime(int lockTime) {
         this.lockTime = lockTime;
     }
     public int getLockTime() {
         return lockTime;
     }

    public void setRecieve(List<Recieve> recieve) {
         this.recieve = recieve;
     }
     public List<Recieve> getRecieve() {
         return recieve;
     }

    public void setSpend(List<Spend> spend) {
         this.spend = spend;
     }
     public List<Spend> getSpend() {
         return spend;
     }

    public void setTimestamp(long timestamp) {
         this.timestamp = timestamp;
     }
     public long getTimestamp() {
         return timestamp;
     }

    public void setWeights(int weights) {
         this.weights = weights;
     }
     public long getWeights() {
         return weights;
     }


    @Override
    public int compareTo(Transaction o) {

        return (int)(o.getTimestamp()-timestamp);
    }
}