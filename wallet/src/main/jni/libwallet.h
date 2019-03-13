#ifndef LIBWALLET_H
#define LIBWALLET_H
#include <stdint.h>
#include <ctype.h>
#include <stddef.h>

#define u256hex(u) ((const char[]) {\
    _hexc((u).u8[ 0] >> 4), _hexc((u).u8[ 0]), _hexc((u).u8[ 1] >> 4), _hexc((u).u8[ 1]),\
    _hexc((u).u8[ 2] >> 4), _hexc((u).u8[ 2]), _hexc((u).u8[ 3] >> 4), _hexc((u).u8[ 3]),\
    _hexc((u).u8[ 4] >> 4), _hexc((u).u8[ 4]), _hexc((u).u8[ 5] >> 4), _hexc((u).u8[ 5]),\
    _hexc((u).u8[ 6] >> 4), _hexc((u).u8[ 6]), _hexc((u).u8[ 7] >> 4), _hexc((u).u8[ 7]),\
    _hexc((u).u8[ 8] >> 4), _hexc((u).u8[ 8]), _hexc((u).u8[ 9] >> 4), _hexc((u).u8[ 9]),\
    _hexc((u).u8[10] >> 4), _hexc((u).u8[10]), _hexc((u).u8[11] >> 4), _hexc((u).u8[11]),\
    _hexc((u).u8[12] >> 4), _hexc((u).u8[12]), _hexc((u).u8[13] >> 4), _hexc((u).u8[13]),\
    _hexc((u).u8[14] >> 4), _hexc((u).u8[14]), _hexc((u).u8[15] >> 4), _hexc((u).u8[15]),\
    _hexc((u).u8[16] >> 4), _hexc((u).u8[16]), _hexc((u).u8[17] >> 4), _hexc((u).u8[17]),\
    _hexc((u).u8[18] >> 4), _hexc((u).u8[18]), _hexc((u).u8[19] >> 4), _hexc((u).u8[19]),\
    _hexc((u).u8[20] >> 4), _hexc((u).u8[20]), _hexc((u).u8[21] >> 4), _hexc((u).u8[21]),\
    _hexc((u).u8[22] >> 4), _hexc((u).u8[22]), _hexc((u).u8[23] >> 4), _hexc((u).u8[23]),\
    _hexc((u).u8[24] >> 4), _hexc((u).u8[24]), _hexc((u).u8[25] >> 4), _hexc((u).u8[25]),\
    _hexc((u).u8[26] >> 4), _hexc((u).u8[26]), _hexc((u).u8[27] >> 4), _hexc((u).u8[27]),\
    _hexc((u).u8[28] >> 4), _hexc((u).u8[28]), _hexc((u).u8[29] >> 4), _hexc((u).u8[29]),\
    _hexc((u).u8[30] >> 4), _hexc((u).u8[30]), _hexc((u).u8[31] >> 4), _hexc((u).u8[31]), '\0' })

#define _hexc(u) (((u) & 0x0f) + ((((u) & 0x0f) <= 9) ? '0' : 'a' - 0x0a))

typedef struct WalletStruct Wallet;

typedef struct{
    char txHash[65];
    uint32_t index;
    char address[75];
    uint64_t amount;
    uint8_t isMyAddress;
}TxInput;

typedef struct
{
    char address[75];
    uint64_t amount;
    uint8_t isMyAddress;
}TxOutput;

typedef struct
{
    TxInput* spend;
    int spendCount;
    TxOutput* recieve;
    int recieveCount;

    char hash[65];
    char description[41];
    uint64_t fee;
//    uint64_t totalAmount;
    uint64_t amount;
    uint32_t lockTime;
    uint32_t blockHeight;
    uint32_t timestamp; // time interval since unix epoch
    uint32_t confirms;
    uint32_t weights;
    uint8_t  isSpend;   //1 spend 0 recieve
}Transaction;

int AddNode(Wallet* wallet, const char* ip, int port, char *error);

Wallet* CreateWallet(const char* path, const char* account, const char* password, const char* seed, const char* salt, char* address, char *error);

Wallet* OpenWallet(const char* path, const char* account, const char* password, char* address, char *error);

int BackupWallet(const char* path, Wallet* wallet, char *error);

int UpdatePassword(Wallet* wallet, const char* oldpwd, const char* password, char * error);

int ResetBlockChain(Wallet* wallet, char *error);

uint8_t ifSyncFinished(Wallet* wallet, char *error);
//int ConnectedPeerCount(Wallet* wallet);

//0 disconnected, 1 connectting, 2 connected
uint8_t GetConnectStatus(Wallet* wallet, char *error);
uint32_t GetBlockHeight(Wallet* wallet, char *error);
uint32_t GetLastBlockHeight(Wallet* wallet, char *error);

int GetAddress(Wallet* wallet, char* address, char *error);

int GetSeed(Wallet* wallet, char* seed, char* salt, char *error);

uint64_t GetBalance(Wallet* wallet, char *error);
uint64_t GetAvailiable(Wallet* wallet, char *error);

int GetTransaction(Wallet* wallet, Transaction **trans, char *error);

void TransactionFree(Transaction *transaction, int count, char *error);


int aesEncrypt(const char *key, const char *pt, uint32_t len, char *ct, uint32_t outLen);

int aesDecrypt(const char *key, const char *ct, uint32_t len, char *pt, uint32_t outLen);

int CreateTransaction(Wallet* wallet, uint64_t amount, const char *addr, const char *des, Transaction **trans, char *error);

char *GetErrString(Wallet* wallet);
#endif // LIBWALLET_H
