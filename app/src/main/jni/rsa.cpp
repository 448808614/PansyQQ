#include <jni.h>
#include "com_pansy_robot_crypter_JNI_RSA.h"
#include "com_pansy_robot_crypter_JNI_Security.h"
#include <openssl/ssl.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <android/log.h>

#define MAX_BLOCK_LENGTH 100
#define EN_BLOCK_LENGTH 128

//
// Created by HASEE on 2019-07-15.
//

JNIEXPORT jstring JNICALL Java_com_pansy_robot_crypter_JNI_1RSA_encrypt
        (JNIEnv *env, jclass jcls,jstring jstr){
    char *cstr=jstringToChar(env,jstr);
    int en_len=0;
    char *en=RSA_Encrypt(cstr,env->GetStringUTFLength(jstr)+1,&en_len);
    return env->NewStringUTF(en);
}

JNIEXPORT jstring JNICALL Java_com_pansy_robot_crypter_JNI_1RSA_decrypt
        (JNIEnv *env, jclass jcls,jstring jstr){
   char *cstr=jstringToChar(env,jstr);
   char *de=RSA_Decrypt(cstr,env->GetStringUTFLength(jstr));
   return charToJstring(env,de);
}

char* RSA_Encrypt(char *in,int in_len,int* en_len) {
    const char *pubKey = "-----BEGIN PUBLIC KEY-----\n\
        MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD2dWPiW/MDMC/RaAT6tu2hPMiD\n\
        CPn11WYzXgWlGukwBWZcpxYAkHalJP2i23aj9ZJxdIDsLbUfDqjQN3ObvhCMKU2c\n\
        bDqWWybgHBmFI3ALL21RCKq1Xkh9kxeoz+erECUWc7Ieo/fYLYvBDAclwc7xp9Fq\n\
        K3S0iQks579JH0xSowIDAQAB\
        \n-----END PUBLIC KEY-----";
	*en_len = ((in_len - 1) / MAX_BLOCK_LENGTH + ((in_len - 1) % MAX_BLOCK_LENGTH == 0 ? 0 : 1))*EN_BLOCK_LENGTH + 1;
	char *en = (char*)malloc(*en_len);

	int i = 0;
	do {
		BIO* pubBio = BIO_new_mem_buf((void*)pubKey, -1);
		RSA* pubRsa = PEM_read_bio_RSA_PUBKEY(pubBio, NULL, NULL, NULL);
		char temp_en[EN_BLOCK_LENGTH] = { 0 };

		int malloc_len = in_len - i * MAX_BLOCK_LENGTH;
		if (malloc_len > MAX_BLOCK_LENGTH)
			malloc_len = MAX_BLOCK_LENGTH + 1;
		char *temp_in = (char*)malloc(malloc_len);
		memcpy(temp_in, in + i * MAX_BLOCK_LENGTH, malloc_len - 1);
		temp_in[malloc_len - 1] = '\0';

		RSA_public_encrypt(strlen(temp_in), (unsigned char*)temp_in, (unsigned char*)temp_en, pubRsa, RSA_PKCS1_PADDING);
		memcpy(en + i * EN_BLOCK_LENGTH, temp_en, EN_BLOCK_LENGTH);
		i++;
		RSA_free(pubRsa);
	} while (i*MAX_BLOCK_LENGTH < in_len - 1);

	en[*en_len - 1] = '\0';
    en = Base64Encode(en,en_len);
	return en;
}

char* RSA_Decrypt(char *en, int en_len) {
	const char *priKey = "-----BEGIN PRIVATE KEY-----\n\
         MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPZ1Y+Jb8wMwL9Fo\n\
         BPq27aE8yIMI+fXVZjNeBaUa6TAFZlynFgCQdqUk/aLbdqP1knF0gOwttR8OqNA3\n\
         c5u+EIwpTZxsOpZbJuAcGYUjcAsvbVEIqrVeSH2TF6jP56sQJRZzsh6j99gti8EM\n\
         ByXBzvGn0WordLSJCSznv0kfTFKjAgMBAAECgYA/mHUIwO9JIFMGdw/p3fAdjgCz\n\
         h0pwu/seQCG2w+XuJUaPm/HafWyQGqZ0Mqs/jauXKRfnWbgF5pN6/wQO6RF9h70t\n\
         hIARlpa0pqjDsznZHhjZD2NBrL8gCbFCRHIsnZikNBK7CkjAYNiW5HyNQCq2y4cr\n\
         WS0SnFdTgwDavm/vWQJBAPs7eRa3a/Xjgw4OZ4kJNdpKi1y2nSy/e7wSgnh3ONFr\n\
         r7h3ELTCfNszf1/GXiuhl5vMu4W+TY4IYV3+ZmODXQ0CQQD7Irm4dgogWaSjJa1x\n\
         /WgU907wsNHoSPn0q84yb2K7YeQf1t5yL6t/voxojIEXxlZ0n3iNwfiSfaQZMkIw\n\
         mGJvAkEA6h+oXoUcdQ9KiITf85LDvuQNL592pcbkdoYBVGY2auMh5JQA25MLa/N3\n\
         1jc0dxCmCqkmcCLCf4RLyJ5VqJM42QJAHQzWSPU2lKlp9wlbt+zlRk+ZTRRXI7SV\n\
         XW0mTAVAeYaIWfqD/tdu0jcbjNh78mEaDSGJU3SVqXRpMBT1PSPTdwJBAJvM87QK\n\
         Z3KyoKOsBNr+ggfi8iccqWIsQjWDpNRYARSBsiVlRg+7sjgiruVig62Gq25XtAA+\n\
         1r9uFK5meLse/rg=\
         \n-----END PRIVATE KEY-----";
    en = Base64Decode(en,en_len);
	//int de_len = en_len&128;//base64解密长度，应该是128的整数倍
	int de_len = en_len/128*128;
    char* de = (char*)malloc(en_len);

	int i = 0;

	do {
		char *temp_en = (char*)malloc(EN_BLOCK_LENGTH);
		memcpy(temp_en, en+i*EN_BLOCK_LENGTH, EN_BLOCK_LENGTH);

		BIO* priBIO = BIO_new_mem_buf((void*)priKey, -1);
		RSA* priRsa = PEM_read_bio_RSAPrivateKey(priBIO, NULL, NULL, NULL);
		char temp_de[MAX_BLOCK_LENGTH] = { 0 };
		RSA_private_decrypt(RSA_size(priRsa), (unsigned char*)temp_en, (unsigned char*)temp_de, priRsa, RSA_PKCS1_PADDING);

		memcpy(de+i*MAX_BLOCK_LENGTH,temp_de,MAX_BLOCK_LENGTH);
		i++;
		RSA_free(priRsa);
	} while (i*EN_BLOCK_LENGTH<de_len-1);
	de[i*MAX_BLOCK_LENGTH] = '\0';
	return de;
}

char *Base64Encode(char* input, int* length)
{
	BIO* bmem = NULL;
	BIO* b64 = NULL;
	BUF_MEM* bptr = NULL;
	b64 = BIO_new(BIO_f_base64());
	BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
	bmem = BIO_new(BIO_s_mem());
	b64 = BIO_push(b64, bmem);
	BIO_write(b64, input, *length);
	BIO_flush(b64);
	BIO_get_mem_ptr(b64, &bptr);
	*length  = bptr->length;
	char * buff = (char *)malloc(bptr->length + 1);
	memcpy(buff, bptr->data, bptr->length);
	buff[bptr->length] = 0;
	BIO_free_all(b64);
	return buff;
}

char *Base64Decode(char* input, int length)
{
	BIO * b64 = NULL;
	BIO * bmem = NULL;
	char * buffer = (char *)malloc(length);
	memset(buffer, 0, length);
	b64 = BIO_new(BIO_f_base64());
	BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
	bmem = BIO_new_mem_buf(input, length);
	bmem = BIO_push(b64, bmem);
	BIO_read(bmem, buffer, length);
	BIO_free_all(bmem);
	return buffer;
}

char* jstringToChar(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("GB2312");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	return rtn;
}

jstring charToJstring(JNIEnv* env, const char* pat) {
    jclass strClass = (env)->FindClass("java/lang/String");
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*) pat);
    jstring encoding = (env)->NewStringUTF("GB2312");
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}