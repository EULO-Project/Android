cmake_minimum_required(VERSION 3.4.1)
message(STATUS ABI_INFO  = ${ANDROID_ABI})
set(EXTERNAL_LIBS_DIR ${CMAKE_SOURCE_DIR}/lib)

add_library( monerujo
             SHARED
             src/main/jni/walletjni.cpp )



add_library(wallet STATIC IMPORTED)
set_target_properties(wallet PROPERTIES IMPORTED_LOCATION
    ${EXTERNAL_LIBS_DIR}/${ANDROID_ABI}/libwallet.a)

add_compile_options(-std=c++11)

find_library( log-lib log )

target_link_libraries(  monerujo

                        wallet
                        ${log-lib}
                        )



