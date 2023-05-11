//
// Created by HASEE on 2019-04-01.
//

#include <jni.h>
#include "com_pansy_robot_crypter_JNI_Official.h"
#include <openssl/md5.h>
#include <openssl/ssl.h>
#include "com_pansy_robot_crypter_JNI_Security.h"
#include <math.h>
#include <android/log.h>

JNIEXPORT jbyteArray JNICALL Java_com_pansy_robot_crypter_JNI_1Official_computeOfficial
        (JNIEnv *env, jobject obj,jbyteArray offkey,jbyteArray bufsig,jbyteArray bufTGTGT,jobject context){
    char *sign="3082032f30820217a0030201020204445683b5300d06092a864886f70d01010b05003048310a30080603550406130134310a30080603550408130132310a30080603550407130139310a3008060355040a130136310a3008060355040b130134310a30080603550403130139301e170d3139303731393136313733305a170d3434303731323136313733305a3048310a30080603550406130134310a30080603550408130132310a30080603550407130139310a3008060355040a130136310a3008060355040b130134310a3008060355040313013930820122300d06092a864886f70d01010105000382010f003082010a028201010092296d128bb2822582610fc8a06aceb2ebc5ad0f6e313fc1c5a5107bddae5b0b7496881ec997edf861b4181f1ea4b43eea697b1e419b01cfac9938d1909b43011343312973e28f7ef7d337325c318198812797b8699812d9046290f9eb3803c89965b7a031e8b0c28a39fc753b55dc8f18de73bdc37e0228fc4031be079958f387b4294801bd1deb497acd890af538330f0dfa648d6f37458af8778fd8e9061d63938c2355f28b7ecf6a57107bd2bbfc80dde2c23c76bb6e7f34180e9010673c792e74a0f5afdcf4581bebd3771784ac4cc87323877b813824f08558c5f42b2f6ff2fcaf0a9b6799a12b0fb1a4dc458005c42196abd4c4695a6cb382934d73fd0203010001a321301f301d0603551d0e0416041477f9b074a883af75ec1b495244fb3c1fa958f8bf300d06092a864886f70d01010b050003820101002dcecce38c6d1abc406f66c9c868ff4e351dc87396706b0b09710702f9258ec3b02daeecd6f7f5a70bcf8d2ab125d57e4cce0905fbb7b1cb12b4f3c90239398241448ebac8f63d6b68956d7d5d40d1381aa50c5f556f1fe2a861be5f7dc0ca92ca6eb2891e507509d1ead9d26dfca046575e1cd4e65c3b6d635ca534cb4e17584e3034cd83698a959f3081d7e65874d520d3651d9f39729e1556f28d044703d5ab8f6d1bff935697346038fab5aca622a89ddf09c34146bf0d22d59001e52c1ce2ed70639d22fdd8d51c5731ef0e6674d4a11393005e8ecc6599cea2a735f80b516f58ac477ebbbd85b9eca3885096e732077539b39aab279f3c3df37936076b";
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
    char * csignature = jstringToChar(env,signature);

    if(strcmp(csignature,sign)==0){
        unsigned char *c_offkey=(unsigned char*)(*env).GetByteArrayElements(offkey,0);
        unsigned char *c_bufsig=(unsigned char*)(*env).GetByteArrayElements(bufsig,0);
        unsigned char *c_bufTGTGT=(unsigned char*)(*env).GetByteArrayElements(bufTGTGT,0);
        unsigned char *c_official=ComputeOfficial(c_offkey,c_bufsig,c_bufTGTGT);
        jbyteArray j_official=(*env).NewByteArray(16);
        (*env).SetByteArrayRegion(j_official,0,16,(jbyte *)c_official);
        return j_official;
    }else{
         //如果已到期，返回错误的official
         //退出app
         jclass sys_cls=env->FindClass("java/lang/System");
         jmethodID exit_id=env->GetStaticMethodID(sys_cls,"exit","(I)V");
         env->CallStaticVoidMethod(sys_cls,exit_id);

         jbyteArray j_official=(*env).NewByteArray(8);
         unsigned char c_official[8]={217,56,215,240,53,106,174,108};
         (*env).SetByteArrayRegion(j_official,0,8,(jbyte *)c_official);
         return j_official;
    }

    //crc校验
    /*unsigned long dexCrc=917409309;
    jclass pack_cls=env->GetObjectClass(context);
    jmethodID pack_id=env->GetMethodID(pack_cls,"getPackageCodePath","()Ljava/lang/String;");
    jstring apkPath=(jstring)env->CallObjectMethod(context,pack_id);

    jclass zip_cls=env->FindClass("java/util/zip/ZipFile");
    jmethodID zip_id=env->GetMethodID(zip_cls,"<init>","(Ljava/lang/String;)V");
    jobject zipfile=env->NewObject(zip_cls,zip_id,apkPath);
    jclass zf_cls=env->GetObjectClass(zipfile);

    jclass ze_cls=env->FindClass("java/util/zip/ZipEntry");
    jmethodID getEntry_id=env->GetMethodID(zf_cls,"getEntry","(Ljava/lang/String;)Ljava/util/zip/ZipEntry;");
    jobject dexentry=env->CallObjectMethod(zipfile,getEntry_id,env->NewStringUTF("classes.dex"));

    jmethodID getCrc_id=env->GetMethodID(ze_cls,"getCrc","()J");
    jlong crc=env->CallLongMethod(dexentry,getCrc_id);*/

    //if(crc==dexCrc){

    /*} else{

    }*/

    /*jclass app_cls=env->FindClass("com/pansy/robot/APP");
        jmethodID getQQ_id=env->GetStaticMethodID(app_cls,"getQQ","()J");
        jlong QQ=env->CallStaticLongMethod(app_cls,getQQ_id);
        //long转jstring
        jclass long_cls=env->FindClass("java/lang/Long");
        jmethodID toString_id=env->GetStaticMethodID(long_cls,"toString","(J)Ljava/lang/String;");
        jstring jstrQQ=(jstring)env->CallStaticObjectMethod(long_cls,toString_id,QQ);
        //jstring jQQ=env->NewStringUTF((char*)QQ);
        //jstring转char*
        char* cQQ =jstringToChar(env,jstrQQ);
        char pstr[20]="QQ=";
        strcat(pstr,cQQ);
        jstring param=env->NewStringUTF(pstr);
        //验证授权
        jclass http_cls=env->FindClass("com/pansy/robot/utils/HttpRequest");
        jmethodID postSync_id=env->GetStaticMethodID(http_cls,"postSync","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        jstring data=(jstring)env->CallStaticObjectMethod(http_cls,postSync_id,env->NewStringUTF(Base64Decode("aHR0cDovLzE5My4xMTIuNDIuMTUvUGFuc3kvZGV2ZWxvcC92ZXJpZnlfZXhwaXJlLnBocA==",72)),param);
        //RSA到期时间解密
        data=Java_com_pansy_robot_crypter_JNI_1RSA_decrypt(env,obj,data);
        jclass json_cls=env->FindClass("org/json/JSONObject");
        jmethodID json_id=env->GetMethodID(json_cls,"<init>","(Ljava/lang/String;)V");
        jobject json=env->NewObject(json_cls,json_id,data);
        jmethodID getString_id=env->GetMethodID(json_cls,"getString","(Ljava/lang/String;)Ljava/lang/String;");
        jstring s_QQ=(jstring)env->CallObjectMethod(json,getString_id,env->NewStringUTF("QQ"));
        jmethodID parseLong_id=env->GetStaticMethodID(long_cls,"parseLong","(Ljava/lang/String;)J");
        jlong l_QQ=env->CallStaticLongMethod(long_cls,parseLong_id,s_QQ);
        jmethodID setQQ_id=env->GetStaticMethodID(app_cls,"setQQ","(J)V");
        env->CallStaticVoidMethod(app_cls,setQQ_id,l_QQ);
        jstring s_expire=(jstring)env->CallObjectMethod(json,getString_id,env->NewStringUTF("expire"));
        jmethodID getSync_id=env->GetStaticMethodID(http_cls,"getSync","(Ljava/lang/String;)Ljava/lang/String;");
        jstring s_current=(jstring)env->CallStaticObjectMethod(http_cls,getSync_id,env->NewStringUTF(Base64Decode("aHR0cDovLzE5My4xMTIuNDIuMTUvUGFuc3kvZGV2ZWxvcC9ub3cucGhw",56)));
        //RSA现在时间解密
        s_current=Java_com_pansy_robot_crypter_JNI_1RSA_decrypt(env,obj,s_current);
        jclass sdf_cls=env->FindClass("java/text/SimpleDateFormat");
        jmethodID sdf_id=env->GetMethodID(sdf_cls,"<init>","(Ljava/lang/String;)V");
        jobject sdf=env->NewObject(sdf_cls,sdf_id,env->NewStringUTF(Base64Decode("eXl5eS1NTS1kZCBISDptbTpzcw==",28)));
        jmethodID parse_id=env->GetMethodID(sdf_cls,"parse","(Ljava/lang/String;)Ljava/util/Date;");
        jobject expire=env->CallObjectMethod(sdf,parse_id,s_expire);
        jobject current=env->CallObjectMethod(sdf,parse_id,s_current);
        jclass date_cls=env->GetObjectClass(expire);
        jmethodID getTime_id=env->GetMethodID(date_cls,"getTime","()J");
        jlong expire_time=env->CallLongMethod(expire,getTime_id);
        jlong current_time=env->CallLongMethod(current,getTime_id);
        //判断时差
        jclass sys_cls=env->FindClass("java/lang/System");
        jmethodID getCurTime_id=env->GetStaticMethodID(sys_cls,"currentTimeMillis","()J");
        jlong sysCurTime=env->CallStaticLongMethod(sys_cls,getCurTime_id);*/

}


unsigned char* ComputeOfficial(unsigned char *offkey, unsigned char *bufsig, unsigned char *bufTGTGT){
    int	MD5InfoCount = 4;
    int	round = 256;
    int	TmOffMod = 19;
    int	TmOffModAdd = 5;
    unsigned char *MD5Info0 = Md5_(offkey,16);
    unsigned char *MD5Info1 = Md5_(bufsig,56);
    unsigned char *MD5Info3 = Md5_(bufTGTGT,120);
    int keyround = 480 % TmOffMod + TmOffModAdd;
    unsigned char *seq = new unsigned char[round]{0};
    unsigned char *ls = new unsigned char[round]{0};
    for (int i = 0; i < round; i++){
        seq[i] = i;
        ls[i] = MD5Info1[i%16];
    }
    int i = 0;
    unsigned char k=0;
    unsigned char v=0;
    for (int i = 0; i < round; i++){
        k +=  seq[i] + ls[i];
        v = seq[k];
        seq[k] = seq[i];
        seq[i] = v;
    }
    k = 0;
    unsigned char *MD5Info2 = new unsigned char[16]{0};
    for (int i = 0; i < 16; i++){
        k = (k + seq[i + 1]) % round;
        v = seq[k];
        seq[k] = seq[i + 1];
        seq[i + 1] = v;
        v = seq[k] + seq[i + 1];
        MD5Info2[i] = seq[v] ^ MD5Info0[i];
    }
    unsigned char *totalMd5 = new unsigned char[64]{0};
    memcpy(totalMd5, MD5Info0,16);
    memcpy(totalMd5 + 16, MD5Info1, 16);
    memcpy(totalMd5 + 32, MD5Info2, 16);
    memcpy(totalMd5 + 48, MD5Info3, 16);
    unsigned char *MD5MD5Info = Md5_(totalMd5,64);
    MD5Info0 = MD5MD5Info;
    for (int i = 0; i < keyround; i++){
        MD5Info0 = Md5_(MD5Info0,16);
    }
    unsigned char *td1 = SubBytes(MD5MD5Info,0,8);
    unsigned char *td2 = SubBytes(MD5MD5Info, 8, 8);
    unsigned char *official = new unsigned char[16]{0};
    for (int i = 0; i < MD5InfoCount; i++){
        unsigned char *key = new unsigned char[16];
        switch (i){
            case 0:
                key = MD5Info0;
                break;
            case 1:
                key = MD5Info1;
                break;
            case 2:
                key = MD5Info2;
                break;
            case 3:
                key = MD5Info3;
                break;
        }
        key = ReverseBytes(key,16);
        unsigned char *v2 = new unsigned char[8];
        unsigned char *v1 = new unsigned char[8];
        TeanEncipher(td2, key, v2);
        TeanEncipher(td1, key, v1);
        unsigned char *tea_result = new unsigned char[16];
        memcpy(tea_result,v1,8);
        memcpy(tea_result+8,v2,8);
        for (int j = i; j < 16; j++){
            official[j] = official[j] ^ tea_result[j];
        }
        delete[] key;
        delete[] v2;
        delete[] v1;
        delete[] tea_result;
    }
    official = Md5_(official, 16);

    delete[] MD5Info0;
    delete[] MD5Info1;
    delete[] MD5Info2;
    delete[] MD5Info3;
    delete[] seq;
    delete[] ls;
    delete[] totalMd5;
    delete[] MD5MD5Info;
    delete[] td1;
    delete[] td2;

    return official;

}
void PrintBytes(unsigned char *bytes, int size){
    for (int i = 0; i < size; i++){
        printf("%d ", bytes[i]);
    }
    printf("\n");
}
unsigned char* SubBytes(unsigned char* bytes, int start, int count){
    unsigned char *temp = new unsigned char[count];
    for (int i = 0; i < count; i++){
        temp[i] = bytes[start + i];
    }
    return temp;
}
unsigned char* ReverseBytes(unsigned char* bytes, int size){
    unsigned char *temp = new unsigned char[size];
    unsigned char* b=new unsigned char[4];
    for (int i = 0; i < size/4; i++){
        b = SubBytes(bytes,4*i,4);
        for (int j = 0; j < 4; j++){
            temp[i*4+j] = b[4-j-1];
        }
    }
    return temp;
}
void TeanEncipher(unsigned char *data, unsigned char *key, unsigned char *result){
    unsigned long eax = Bytes2Long(SubBytes(data, 0, 4));
    unsigned long esi = Bytes2Long(SubBytes(data, 4, 4));
    unsigned long var4 = Bytes2Long(ReverseBytes(SubBytes(key, 0, 4), 4));
    unsigned long ebp = Bytes2Long(ReverseBytes(SubBytes(key, 4, 4), 4));
    unsigned long var3 = Bytes2Long(ReverseBytes(SubBytes(key, 8, 4), 4));
    unsigned long var2 = Bytes2Long(ReverseBytes(SubBytes(key, 12, 4), 4));
    //printf("%lu %lu %lu %lu %lu %lu\n", eax,esi,var4, ebp, var3, var2);

    unsigned long edi = 0x9E3779B9;
    unsigned long edx = 0;
    unsigned long ecx = 0;
    for (int i = 0; i < 16; i++){
        edx = esi;
        ecx = esi;
        edx = edx >> 5;
        ecx = ecx << 4;
        edx += ebp;
        ecx += var4;
        edx = edx ^ ecx;
        ecx = esi + edi;
        edx = edx ^ ecx;
        eax += edx;
        edx = eax;
        ecx = eax;
        edx = edx << 4;
        edx = edx + var3;
        ecx = ecx >> 5;
        ecx += var2;
        edx = edx ^ ecx;
        ecx = eax + edi;
        edx = edx ^ ecx;
        edi -= 0x61C88647;
        esi += edx;
    }
    memcpy(result, Long2Bytes(eax), 4);
    memcpy(result + 4, Long2Bytes(esi), 4);
}
unsigned char* Long2Bytes(unsigned long n){
    unsigned char *temp = new unsigned char[4];
    temp[0] = n / 16777216;
    temp[1] = (n - temp[0] * 16777216) / 65536;
    temp[2] = (n - temp[0] * 16777216 - temp[1] * 65536) / 256;
    temp[3] = (n - temp[0] * 16777216 - temp[1] * 65536 - temp[2] * 256);
    return temp;
}
unsigned long Bytes2Long(unsigned char *bytes){
    unsigned long a = bytes[0] * 16777216;
    unsigned long  b = bytes[1] * 65536;
    unsigned long  c = bytes[2] * 256;
    unsigned long  d = bytes[3];
    unsigned long  n = a + b + c + d;
    return n;
}
unsigned char *Md5_(unsigned char* data,int data_length){
    //计算md5
    MD5_CTX ctx;
    unsigned char *outmd = (unsigned char*)malloc(sizeof(unsigned char)* 16);
    memset(outmd, 0, sizeof(outmd));
    MD5_Init(&ctx);
    MD5_Update(&ctx, data, data_length);
    MD5_Final(outmd, &ctx);
    return outmd;
}
