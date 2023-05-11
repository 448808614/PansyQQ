/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_pansyqq_crypter_Java2CJNI */

#ifndef _Included_com_pansy_robot_crypter_JNI_Security
#define _Included_com_pansy_robot_crypter_JNI_Security
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_pansyqq_crypter_Java2CJNI
 * Method:    ecdh
 * Signature: ()Lcom/example/pansyqq/crypter/ECC;
 */
JNIEXPORT jstring JNICALL Java_com_pansy_robot_crypter_JNI_1Security_checkEnvironment
  (JNIEnv *,jobject);
JNIEXPORT jbyteArray JNICALL Java_com_pansy_robot_crypter_JNI_1Security_pluginEncrypt
  (JNIEnv *,jobject,jbyteArray,jint);


char* jstringToChar(JNIEnv* env, jstring jstr);
jstring charToJstring(JNIEnv* env, const char* pat);
bool is_frida_server_listening(int port);


/*
 * Class:     com_example_pansyqq_crypter_Java2CJNI
 * Method:    ecdh2
 * Signature: (Lcom/example/pansyqq/crypter/ECC;[B)[B
 */


#ifdef __cplusplus
}
#endif
#endif