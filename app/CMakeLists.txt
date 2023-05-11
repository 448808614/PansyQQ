cmake_minimum_required(VERSION 3.4.1)
#C++编译
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src/main/include)
#动态库
add_library(crypto SHARED IMPORTED )
add_library(ssl SHARED IMPORTED )
#引入第三方.so库
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/libcrypto.so)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/libssl.so)

add_library(
        Openssl
        SHARED
        src/main/jni/ecdh.cpp
        src/main/jni/aes.cpp
        src/main/jni/official.cpp
        src/main/jni/security.cpp
        src/main/jni/rsa.cpp
        src/main/jni/function_decrypt.cpp
)
find_library(
        log-lib
        log
)
target_link_libraries(
        Openssl
        crypto
        ssl
        ${log-lib}
        log
)
