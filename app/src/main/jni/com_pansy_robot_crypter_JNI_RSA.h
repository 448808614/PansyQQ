/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_pansyqq_crypter_Java2CJNI */

#ifndef _Included_com_pansy_robot_crypter_JNI_RSA
#define _Included_com_pansy_robot_crypter_JNI_RSA
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_pansyqq_crypter_Java2CJNI
 * Method:    ecdh
 * Signature: ()Lcom/example/pansyqq/crypter/ECC;
 */
JNIEXPORT jstring JNICALL Java_com_pansy_robot_crypter_JNI_1RSA_encrypt
  (JNIEnv *,jclass,jstring);
JNIEXPORT jstring JNICALL Java_com_pansy_robot_crypter_JNI_1RSA_decrypt
  (JNIEnv *,jclass,jstring);
char *Base64Encode(char* input, int* length);
char *Base64Decode(char* input, int length);
char* RSA_Encrypt(char *in,int in_len,int* en_len);
char* RSA_Decrypt(char *en, int en_len);

/*
 * Class:     com_example_pansyqq_crypter_Java2CJNI
 * Method:    ecdh2
 * Signature: (Lcom/example/pansyqq/crypter/ECC;[B)[B
 */


#ifdef __cplusplus
}
#endif
#endif
