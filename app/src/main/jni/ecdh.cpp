
//
// Created by HASEE on 2019-01-02.
//
#include <memory.h>
#include "com_pansy_robot_crypter_JNI_ECDH.h"
#include "openssl/ssl.h"
#include <android/log.h>

JNIEXPORT jobject JNICALL Java_com_pansy_robot_crypter_JNI_1ECDH_ecdh
        (JNIEnv *env, jobject jobj,jobject context){
    //签名校验
    unsigned int sign=-732647668;//-732647668
    jclass pwm_class = env->GetObjectClass(context);
    jmethodID pm_id = env->GetMethodID(pwm_class, "getPackageManager","()Landroid/content/pm/PackageManager;");
    jobject pm_obj =env->CallObjectMethod(context, pm_id);
    jclass pm_class = env->GetObjectClass(pm_obj);
    jmethodID package_info_id = env->GetMethodID(pm_class, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jmethodID mId = env->GetMethodID(pwm_class, "getPackageName","()Ljava/lang/String;");
    jstring pkg_str = (jstring)(env->CallObjectMethod(context, mId));
    jobject pi_obj = env->CallObjectMethod(pm_obj, package_info_id, pkg_str, 64);
    jclass pi_clazz = env->GetObjectClass(pi_obj);
    jfieldID signatures_fieldId = env->GetFieldID(pi_clazz, "signatures","[Landroid/content/pm/Signature;");
    jobject signatures_obj = env->GetObjectField(pi_obj, signatures_fieldId);
    jobjectArray signaturesArray = (jobjectArray) signatures_obj;
    jobject signature_obj = env->GetObjectArrayElement(signaturesArray, 0);
    jclass signature_clazz = env->GetObjectClass(signature_obj);
    jmethodID string_id = env->GetMethodID(signature_clazz, "toCharsString","()Ljava/lang/String;");
    jstring signature = (jstring)(env->CallObjectMethod(signature_obj, string_id));
    jclass strcls = env->GetObjectClass(signature);
    jmethodID hashCodeId = env->GetMethodID(strcls, "hashCode","()I");
    jint sign_hash =env->CallIntMethod(signature, hashCodeId);
    //__android_log_print(ANDROID_LOG_INFO,"JNITag hash","");

    //if(sign_hash==sign)
    if(true){
        unsigned char pub_key[25] = {};
        unsigned char sha_key[16] = {};
        unsigned char tk[49] = { 0x04, 0xBF, 0x47, 0xA1, 0xCF, 0x78, 0xA6, 0x29, 0x66, 0x8B,
                                 0x0B, 0xC3, 0x9F, 0x8E, 0x54, 0xC9, 0xCC, 0xF3, 0xB6, 0x38,
                                 0x4B, 0x08, 0xB8, 0xAE, 0xEC, 0x87, 0xDA, 0x9F, 0x30, 0x48,
                                 0x5E, 0xDF, 0xE7, 0x67, 0x96, 0x9D, 0xC1, 0xA3, 0xAF, 0x11,
                                 0x15, 0xFE, 0x0D, 0xCC, 0x8E, 0x0B, 0x17, 0xCA, 0xCF
        };
        EC_KEY *ec_key = EC_KEY_new_by_curve_name(NID_secp192k1);
        const EC_GROUP *ec_group = EC_KEY_get0_group(ec_key);
        EC_POINT *ec_point = EC_POINT_new(ec_group);
        if (EC_KEY_generate_key(ec_key) == 1){
            EC_POINT_point2oct(ec_group, EC_KEY_get0_public_key(ec_key), POINT_CONVERSION_COMPRESSED, pub_key, 25, 0);
            if (EC_POINT_oct2point(ec_group, ec_point, tk, 49, 0) == 1){
                ECDH_compute_key(sha_key, 16, ec_point, ec_key, 0); }
        }

        jclass cls=env->FindClass("com/pansy/robot/crypter/ECC");
        jmethodID id=env->GetMethodID(cls,"<init>","()V");
        jobject ecc=env->NewObjectA(cls,id,0);

        jfieldID ec_key_id=env->GetFieldID(cls,"ec_key","I");
        env->SetIntField(ecc,ec_key_id,(jint)ec_key);

        jfieldID ec_group_id=env->GetFieldID(cls,"ec_group","I");
        env->SetIntField(ecc,ec_group_id,(jint)ec_group);

        jfieldID ec_point_id=env->GetFieldID(cls,"ec_point","I");
        env->SetIntField(ecc,ec_point_id,(jint)ec_point);

        jfieldID pub_key_id=env->GetFieldID(cls,"pub_key","[B");
        jbyteArray jPubKey=env->NewByteArray(25);
        env->SetByteArrayRegion(jPubKey,0,25,(const jbyte *)pub_key);
        env->SetObjectField(ecc,pub_key_id,jPubKey);

        jfieldID sha_key_id=env->GetFieldID(cls,"sha_key","[B");
        jbyteArray jShaKey=env->NewByteArray(16);
        env->SetByteArrayRegion(jShaKey,0,16,(const jbyte *)sha_key);
        env->SetObjectField(ecc,sha_key_id,jShaKey);
        return ecc;
    }else{
        //退出app
        jclass sys_cls=env->FindClass("java/lang/System");
        jmethodID exit_id=env->GetStaticMethodID(sys_cls,"exit","(I)V");
        env->CallStaticVoidMethod(sys_cls,exit_id);
        //返回错误的key
        unsigned char pub_key[25] = {87,190,16,123,72,36,120,178,141,215,31,193,152,76,51,105,52,253,217,140,206,106,33,74,19};
        unsigned char sha_key[16] = {15,199,124,113,67,238,250,98,117,136,213,15,243,191,100,3};

        jclass cls=env->FindClass("com/pansy/robot/crypter/ECC");
        jmethodID id=env->GetMethodID(cls,"<init>","()V");
        jobject ecc=env->NewObjectA(cls,id,0);

        jfieldID ec_key_id=env->GetFieldID(cls,"ec_key","I");
        env->SetIntField(ecc,ec_key_id,0);

        jfieldID ec_group_id=env->GetFieldID(cls,"ec_group","I");
        env->SetIntField(ecc,ec_group_id,0);

        jfieldID ec_point_id=env->GetFieldID(cls,"ec_point","I");
        env->SetIntField(ecc,ec_point_id,0);

        jfieldID pub_key_id=env->GetFieldID(cls,"pub_key","[B");
        jbyteArray jPubKey=env->NewByteArray(25);
        env->SetByteArrayRegion(jPubKey,0,25,(const jbyte *)pub_key);
        env->SetObjectField(ecc,pub_key_id,jPubKey);

        jfieldID sha_key_id=env->GetFieldID(cls,"sha_key","[B");
        jbyteArray jShaKey=env->NewByteArray(16);
        env->SetByteArrayRegion(jShaKey,0,16,(const jbyte *)sha_key);
        env->SetObjectField(ecc,sha_key_id,jShaKey);
        return ecc;
    }

  };
  JNIEXPORT jbyteArray JNICALL Java_com_pansy_robot_crypter_JNI_1ECDH_ecdh2
    (JNIEnv *env, jobject jobj,jobject ecc,jbyteArray tk){
    unsigned char sha_key[16]={};
       jclass cls=env->GetObjectClass(ecc);

       jfieldID ec_key_id=env->GetFieldID(cls,"ec_key","I");
       jint ec_key=env->GetIntField(ecc,ec_key_id);

       jfieldID ec_group_id=env->GetFieldID(cls,"ec_group","I");
       jint ec_group=env->GetIntField(ecc,ec_group_id);

       jfieldID ec_point_id=env->GetFieldID(cls,"ec_point","I");
       jint ec_point=env->GetIntField(ecc,ec_point_id);

       jboolean isCopy;
       unsigned char *tk_c=(unsigned char *)env->GetByteArrayElements(tk,&isCopy);
       if(EC_POINT_oct2point((EC_GROUP*)ec_group,(EC_POINT*)ec_point,tk_c,49,0)==1){
           ECDH_compute_key(sha_key,16,(EC_POINT*)ec_point,(EC_KEY*)ec_key,0);
       }
       jbyteArray jShaKey=env->NewByteArray(16);
       env->SetByteArrayRegion(jShaKey,0,16,(const jbyte *)sha_key);
       return jShaKey;
    }

//JNIEXPORT jbyteArray JNICALL Java_com_pansy_robot_crypter_JNI_1ECDH_ecdh2
        //(JNIEnv *env, jobject jobj, jobject ecc,jbyteArray tk){
    /*unsigned char sha_key[16]={};
    jclass cls=env->GetObjectClass(ecc);

    jfieldID ec_key_id=env->GetFieldID(cls,"ec_key","I");
    jint ec_key=env->GetIntField(ecc,ec_key_id);

    jfieldID ec_group_id=env->GetFieldID(cls,"ec_group","I");
    jint ec_group=env->GetIntField(ecc,ec_group_id);

    jfieldID ec_point_id=env->GetFieldID(cls,"ec_point","I");
    jint ec_point=env->GetIntField(ecc,ec_point_id);

    jboolean isCopy;
    unsigned char *tk_c=(unsigned char *)env->GetByteArrayElements(tk,&isCopy);
    if(EC_POINT_oct2point((EC_GROUP*)ec_group,(EC_POINT*)ec_point,tk_c,49,0)==1){
        ECDH_compute_key(sha_key,16,(EC_POINT*)ec_point,(EC_KEY*)ec_key,0);
    }*/
    //jbyteArray jShaKey=env->NewByteArray(16);
    //env->SetByteArrayRegion(jShaKey,0,16,(const jbyte *)sha_key);
    //return jShaKey;

    //return NULL;
//};


