/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_example_pansyqq_crypter_Java2CJNI */

#ifndef _Included_com_pansy_robot_crypter_JNI_Official
#define _Included_com_pansy_robot_crypter_JNI_Official
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_pansyqq_crypter_Java2CJNI
 * Method:    ecdh
 * Signature: ()Lcom/example/pansyqq/crypter/ECC;
 */
JNIEXPORT jbyteArray JNICALL Java_com_pansy_robot_crypter_JNI_1Official_computeOfficial
  (JNIEnv *, jobject,jbyteArray,jbyteArray,jbyteArray,jobject)__attribute__((section(".encryptFun")));
//__attribute__((section(".encryptFun")))
/*
 * Class:     com_example_pansyqq_crypter_Java2CJNI
 * Method:    ecdh2
 * Signature: (Lcom/example/pansyqq/crypter/ECC;[B)[B
 */
unsigned char* Md5_(unsigned char *data,int data_length);
unsigned char* ComputeOfficial(unsigned char *offkey,unsigned char *bufsig,unsigned char *bufTGTGT);
void PrintBytes(unsigned char *bytes, int size);
unsigned char* SubBytes(unsigned char* bytes, int start, int count);
unsigned char* ReverseBytes(unsigned char* bytes, int size);
void TeanEncipher(unsigned char *data, unsigned char *key, unsigned char *result);
unsigned char* Long2Bytes(unsigned long n);
unsigned long Bytes2Long(unsigned char *bytes);

#ifdef __cplusplus
}
#endif
#endif