/**
  * Copyright 2019 bejson.com 
  */
package wallet.transaction;

import java.io.Serializable;

import wallet.Coin;

/**
 * Auto-generated: 2019-01-18 16:33:44
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Spend implements Serializable {

    private String address;
    private long amount;
    private int index;
    private boolean isMyAddress;
    private String txHash;
    public void setAddress(String address) {
         this.address = address;
     }
     public String getAddress() {
         return address;
     }

    public void setAmount(long amount) {
         this.amount = amount;
     }
     public Coin getAmount() {
         return Coin.valueOf(amount);
     }

    public void setIndex(int index) {
         this.index = index;
     }
     public int getIndex() {
         return index;
     }

    public void setIsMyAddress(boolean isMyAddress) {
         this.isMyAddress = isMyAddress;
     }
     public boolean getIsMyAddress() {
         return isMyAddress;
     }

    public void setTxHash(String txHash) {
         this.txHash = txHash;
     }
     public String getTxHash() {
         return txHash;
     }

}