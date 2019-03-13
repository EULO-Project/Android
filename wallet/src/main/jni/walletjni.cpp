/**
 * Copyright (c) 2017 m2049r
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <inttypes.h>
#include "walletjni.h"
#include "json/json.h"

//TODO explicit casting jlong, jint, jboolean to avoid warnings

#ifdef __cplusplus
extern "C"
{
#endif
#include "libwallet.h"
#include <string.h>
#include <android/log.h>
#define LOG_TAG "WalletNDK"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG,__VA_ARGS__)

//static JavaVM *cachedJVM;
//static jclass class_ArrayList;
//static jclass class_WalletListener;
//static jclass class_TransactionInfo;
//static jclass class_Transfer;
//static jclass class_Ledger;
#ifdef __cplusplus
}
#endif

extern "C"
JNIEXPORT jint JNICALL
Java_wallet_WalletManager_AddNode(
        JNIEnv *env, jobject instance,
        jobject wallet, jstring ip_, jint port_)
{
    const char *ip = env->GetStringUTFChars(ip_, 0);
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    int ret = AddNode(w, ip, port_, error);

    env->ReleaseStringUTFChars(ip_, ip);
//    env->ReleaseStringUTFChars(port_, port);

    return static_cast<jint>(ret);	//0 success, failed return >0.
}

extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_CreateWallet(JNIEnv *env, jobject instance, jstring path_,
                                       jstring account_, jstring password_, jstring seed_,
                                       jstring salt_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    const char *account = env->GetStringUTFChars(account_, 0);
    const char *password = env->GetStringUTFChars(password_, 0);
    const char *seed = env->GetStringUTFChars(seed_, 0);
    const char *salt = env->GetStringUTFChars(salt_, 0);
//    const char *address = env->GetStringUTFChars(address_, 0);

    // TODO
    char error[512] = {0};
    char address[75] = {0};
    Wallet *w = CreateWallet(path, account, password, seed, salt, address, error);

    env->ReleaseStringUTFChars(path_, path);
    env->ReleaseStringUTFChars(account_, account);
    env->ReleaseStringUTFChars(password_, password);
    env->ReleaseStringUTFChars(seed_, seed);
    env->ReleaseStringUTFChars(salt_, salt);
//    env->ReleaseStringUTFChars(address_, address);

    return reinterpret_cast<jlong>(w);
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_OpenWallet(JNIEnv *env, jobject instance, jstring path_, jstring account_,
                                     jstring password_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    const char *account = env->GetStringUTFChars(account_, 0);
    const char *password = env->GetStringUTFChars(password_, 0);
//    const char *address = env->GetStringUTFChars(address_, 0);

    char error[512] = {0};
    char address[75] = {0};
    Wallet *w = OpenWallet(path, account, password, address, error);

    env->ReleaseStringUTFChars(path_, path);
    env->ReleaseStringUTFChars(account_, account);
    env->ReleaseStringUTFChars(password_, password);
//    env->ReleaseStringUTFChars(address_, address);

    return reinterpret_cast<jlong>(w);
}extern "C"
JNIEXPORT jint JNICALL
Java_wallet_WalletManager_BackupWallet(JNIEnv *env, jobject instance, jstring path_,
                                       jobject wallet) {
    const char *path = env->GetStringUTFChars(path_, 0);

    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    int ret = BackupWallet(path, w, error);

    env->ReleaseStringUTFChars(path_, path);
    return static_cast<jint>(ret); //1 success, failed return 0.
}extern "C"
JNIEXPORT jint JNICALL
Java_wallet_WalletManager_UpdatePassword(JNIEnv *env, jobject instance, jobject wallet,
                                         jstring oldpwd_, jstring password_) {
    const char *oldpwd = env->GetStringUTFChars(oldpwd_, 0);
    const char *password = env->GetStringUTFChars(password_, 0);

    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    int ret = UpdatePassword(w, oldpwd, password, error);

    env->ReleaseStringUTFChars(oldpwd_, oldpwd);
    env->ReleaseStringUTFChars(password_, password);
    return static_cast<jint>(ret); //0 success, failed return >0.
	//
}extern "C"
JNIEXPORT jint JNICALL
Java_wallet_WalletManager_ResetBlockChain(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    int ret = ResetBlockChain(w, error);

    return static_cast<jint>(ret); //0 success, failed return non-zero.
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_ifSyncFinished(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    long ret = ifSyncFinished(w, error);

    return static_cast<jlong>(ret);		//1 success, failed return 0.
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_GetConnectStatus(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    long ret = GetConnectStatus(w, error);

    return static_cast<jlong>(ret);	 	//0 disconnected, 1 connectting, 2 connectted.
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_GetBlockHeight(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    long ret = GetBlockHeight(w, error);

    return static_cast<jlong>(ret);
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_GetLastBlockHeight(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    long ret = GetLastBlockHeight(w, error);

    return static_cast<jlong>(ret);
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_GetAddress(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    char address[75] = {0};
    GetAddress(w, address, error);

    return env->NewStringUTF(address);
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_GetSeed(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};

    char    words[512] = {0};
    char    salt[128] = {0};
    GetSeed(w, words, salt, error);

    return env->NewStringUTF(words);
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_GetSalt(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};

    char    words[512] = {0};
    char    salt[128] = {0};
    GetSeed(w, words, salt, error);

    return env->NewStringUTF(salt);
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_GetBalance(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    long ret = GetBalance(w, error);

    return static_cast<jlong>(ret);
}extern "C"
JNIEXPORT jlong JNICALL
Java_wallet_WalletManager_GetAvailiable(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    long ret = GetAvailiable(w, error);

    return static_cast<jlong>(ret);
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_GetTransaction(JNIEnv *env, jobject instance, jobject wallet) {
    Wallet *w = getHandle<Wallet>(env, wallet);

    char error[512] = {0};
    Transaction *trans = NULL;
    int count = GetTransaction(w, &trans, error);
    Json::Value root;
    for(int i = 0; i < count; i++)
    {
        Json::Value data;
        data["hash"] = trans[i].hash;
		if(trans[i].description)
			data["description"] = trans[i].description;
        data["fee"] = static_cast<Json::UInt64>(trans[i].fee);
        data["amount"] = static_cast<Json::UInt64>(trans[i].amount);
        data["lockTime"] = static_cast<Json::UInt>(trans[i].lockTime);
        data["blockHeight"] = static_cast<Json::UInt>(trans[i].blockHeight);
        data["timestamp"] = static_cast<Json::UInt>(trans[i].timestamp);
        data["confirms"] = static_cast<Json::UInt>(trans[i].confirms);
        data["weights"] = static_cast<Json::UInt>(trans[i].weights);
        data["isSpend"] = static_cast<bool>(trans[i].isSpend);
        Json::Value spends;
        for(int j = 0;j < trans[i].spendCount; j++)
        {
            Json::Value spendOut;
            spendOut["txHash"] = trans[i].spend[j].txHash;
            spendOut["index"] = static_cast<Json::UInt>(trans[i].spend[j].index);
            spendOut["address"] = trans[i].spend[j].address;
            spendOut["amount"] = static_cast<Json::UInt64>(trans[i].spend[j].amount);
            spendOut["isMyAddress"] = static_cast<bool>(trans[i].spend[j].isMyAddress);
            spends.append(spendOut);
        }
        data["spend"] = spends;
        Json::Value recieves;
        for(int j = 0;j < trans[i].recieveCount; j++)
        {
            Json::Value recieve;
            recieve["address"] = trans[i].recieve[j].address;
            recieve["amount"] = static_cast<Json::UInt64>(trans[i].recieve[j].amount);
            recieve["isMyAddress"] = static_cast<bool>(trans[i].recieve[j].isMyAddress);
            recieves.append(recieve);
        }
        data["recieve"] = recieves;
        root.append(data);
    }

	if(trans)
		TransactionFree(trans, count, error);

    return env->NewStringUTF(root.toStyledString().c_str());
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_aesEncrypt(JNIEnv *env, jobject instance, jstring key_, jstring pt_) {
    const char *key = env->GetStringUTFChars(key_, 0);
    const char *pt = env->GetStringUTFChars(pt_, 0);

    char ct[512] = {0};
    aesEncrypt(key, pt, strlen(pt), ct, 512);

    env->ReleaseStringUTFChars(key_, key);
    env->ReleaseStringUTFChars(pt_, pt);

    return env->NewStringUTF(ct);
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_aesDecrypt(JNIEnv *env, jobject instance, jstring key_, jstring ct_) {
    const char *key = env->GetStringUTFChars(key_, 0);
    const char *ct = env->GetStringUTFChars(ct_, 0);

    char pt[512] = {0};
    aesDecrypt(key, ct, strlen(ct), pt, 512);

    env->ReleaseStringUTFChars(key_, key);
    env->ReleaseStringUTFChars(ct_, ct);

    return env->NewStringUTF(pt);
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_CreateTransaction(JNIEnv *env, jobject instance, jobject wallet,
                                            jlong amount, jstring addr_, jstring des_) {
    const char *addr = env->GetStringUTFChars(addr_, 0);
    const char *des = env->GetStringUTFChars(des_, 0);

    Wallet *w = getHandle<Wallet>(env, wallet);
    Transaction *trans = NULL;
    char error[512] = {0};
    int ret = CreateTransaction(w, amount, addr, des, &trans, error);
    
    Json::Value root;
	if(ret == -1)
    {
//		cout << "no available utxo:" << endl;
        LOGI("no available utxo:");
		root["errno"] = ret;
		root["message"] = "no available utxo.";
	}
    else if(ret == 0)
    {
//		cout << "sign failed." << endl;
        LOGI("sign failed.");
		root["errno"] = ret;
		root["message"] = "wait for tx confirming.";
	}
	if(trans)
    {
		root["hash"] = (*trans).hash;
		root["description"] = (*trans).description;
		root["fee"] = static_cast<Json::UInt64>((*trans).fee);
		root["amount"] = static_cast<Json::UInt64>((*trans).amount);
		root["lockTime"] = static_cast<Json::UInt>((*trans).lockTime);
		root["blockHeight"] = static_cast<Json::UInt>((*trans).blockHeight);
		root["timestamp"] = static_cast<Json::UInt>((*trans).timestamp);
		root["confirms"] = static_cast<Json::UInt>((*trans).confirms);
		root["weights"] = static_cast<Json::UInt>((*trans).weights);
		root["isSpend"] = static_cast<bool>((*trans).isSpend);
		Json::Value spends;
		for(int j = 0;j < (*trans).spendCount; j++)
		{
			Json::Value spendOut;
			spendOut["txHash"] = (*trans).spend[j].txHash;
			spendOut["index"] = static_cast<Json::UInt>((*trans).spend[j].index);
			spendOut["address"] = (*trans).spend[j].address;
			spendOut["amount"] = static_cast<Json::UInt64>((*trans).spend[j].amount);
			spendOut["isMyAddress"] = static_cast<bool>((*trans).spend[j].isMyAddress);
			spends.append(spendOut);
		}
		root["spend"] = spends;
		Json::Value recieves;
		for(int j = 0;j < (*trans).recieveCount; j++)
		{
			Json::Value recieve;
			recieve["address"] = (*trans).recieve[j].address;
			recieve["amount"] = static_cast<Json::UInt64>((*trans).spend[j].amount);
			recieve["isMyAddress"] = static_cast<bool>((*trans).spend[j].isMyAddress);
			recieves.append(recieve);
		}
		root["recieve"] = recieves;
		TransactionFree(trans, 1, error);
	}

    env->ReleaseStringUTFChars(addr_, addr);
    env->ReleaseStringUTFChars(des_, des);

    return env->NewStringUTF(root.toStyledString().c_str());	
	//err  return {"errno": , "message":}
	//success return json Transaction.
}extern "C"
JNIEXPORT jstring JNICALL
Java_wallet_WalletManager_GetErrString(JNIEnv *env, jobject instance, jobject wallet) {
	Wallet *w = getHandle<Wallet>(env, wallet);
    
    return env->NewStringUTF(GetErrString(w));
}
