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
public class Recieve implements Serializable {

    private String address;
    private Long amount;
    private boolean isMyAddress;
    public void setAddress(String address) {
         this.address = address;
     }
     public String getAddress() {
         return address;
     }

    public void setAmount(Long amount) {
         this.amount = amount;
     }
     public Coin getAmount() {
         return Coin.valueOf(amount);
     }

    public void setIsMyAddress(boolean isMyAddress) {
         this.isMyAddress = isMyAddress;
     }
     public boolean getIsMyAddress() {
         return isMyAddress;
     }

}