/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wallet;



import java.util.List;

public class Wallet {

    static {
        System.loadLibrary("monerujo");
    }

    public static final long ERROR_CODE=0;
    public static final long NEW_WALLET=-1;

    public static final long CONNECT_STATUS_CONNECTED=2;
    public static final long CONNECT_STATUS_CONNECTTING=1;
    public static final long CONNECT_STATUS_DISCONNECTED=0;

    private List<String> seed;

    private WalletManager mWalletManager;
    private long handle = 0;

    String path;



    String password;

    public Wallet(long handle, WalletManager mWalletManager) {
        this.handle = handle;
        this.mWalletManager=mWalletManager;

//        coin=new Coin();
    }

    public static Wallet HANDlE(long handle, WalletManager mWalletManager){
        return new Wallet(handle,mWalletManager);
    }


    public static Wallet NEW_WALLET( WalletManager mWalletManager){
        return new Wallet(NEW_WALLET,mWalletManager);
    }

    public boolean isNewWallet(){
        if(handle==NEW_WALLET){
            return true;
        }
        return false;
    }
    public boolean isError(){
        if(handle==ERROR_CODE){
            return true;
        }
        return false;
    }
    public boolean isEnable(){
        if(isError()||isNewWallet()){
            return false;
        }
        return true;
    }
    public long getHandle(){
        return handle;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Wallet){

            if(handle==((Wallet) obj).handle){
                return true;
            }
        }
        return false;
    }

    public List<String> getSeed(){
        return seed;
    }
    public void setSeed(List<String> seed){
        this.seed=seed;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
