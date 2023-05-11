//
// Created by HASEE on 2019-07-14.
//

#include <jni.h>
#include "com_pansy_robot_crypter_JNI_Security.h"
#include <string.h>
#include <android/log.h>

#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

JNIEXPORT jstring JNICALL Java_com_pansy_robot_crypter_JNI_1Security_checkEnvironment
  (JNIEnv *env,jobject jobj){
    if(is_frida_server_listening(27042)){
        //退出app
         jclass sys_cls=env->FindClass("java/lang/System");
         jmethodID exit_id=env->GetStaticMethodID(sys_cls,"exit","(I)V");
         env->CallStaticVoidMethod(sys_cls,exit_id);
    }
    return env->NewStringUTF("de.robv.android.xposed|de.robv.android.xposed.installer|io.va.exposed|me.weishu.exp|org.meowcat.edxposed.manager|com.solohsu.android.edxp.manager|io.virtualapp.sandvxposed|com.topjohnwu.magisk");
}

bool is_frida_server_listening(int port) {
    struct sockaddr_in sa;
    memset(&sa, 0, sizeof(sa));
    sa.sin_family = AF_INET;
    sa.sin_port = htons(port);
    inet_aton("127.0.0.1", &(sa.sin_addr));
    int sock = socket(AF_INET , SOCK_STREAM , 0);
    if (connect(sock , (struct sockaddr*)&sa , sizeof sa) != -1) {
       //__android_log_print(ANDROID_LOG_INFO,"aaa","frida");
       return true;
    }else{
        char line[512];
        FILE* fp;
        fp = fopen("/proc/self/maps", "r");
        if (fp) {
            while (fgets(line, 512, fp)) {
                //__android_log_print(ANDROID_LOG_INFO,"aaa","%s",line);
                if (strstr(line, "frida")) {
                    return true;
                }
            }
            fclose(fp);
        } else {
              //Error opening /proc/self/maps. If this happens, something is off.
              return true;
        }
    }

    return false;

}

JNIEXPORT jbyteArray JNICALL Java_com_pansy_robot_crypter_JNI_1Security_pluginEncrypt
  (JNIEnv *env,jobject jobj,jbyteArray data,jint len){
     //不能用静态方法
     unsigned char *c_data=(unsigned char*)(*env).GetByteArrayElements(data,0);
     unsigned char token[256]={ 138, 185, 179, 225, 247, 9, 67, 244, 181, 33, 68, 87, 153, 149, 200, 105, 206, 241, 0, 34, 192, 5, 70, 3, 57, 128, 97, 77, 202, 193, 5, 114, 109, 233, 154, 201, 104, 53, 62, 133, 117, 139, 13, 232, 31, 82, 160, 180, 111, 135, 211, 227, 200, 13, 204, 151, 16, 69, 57, 84, 16, 223, 216, 109, 30, 240, 61, 188, 249, 68, 208, 150, 244, 245, 21, 254, 87, 60, 48, 25, 2, 136, 168, 227, 83, 205, 107, 31, 74, 31, 18, 148, 235, 127, 103, 118, 228, 245, 36, 35, 112, 223, 254, 16, 123, 136, 226, 2, 7, 252, 127, 129, 206, 27, 4, 137, 41, 237, 43, 132, 48, 53, 118, 117, 35, 73, 184, 118, 9, 32, 216, 100, 150, 173, 210, 217, 82, 107, 254, 90, 248, 58, 149, 210, 28, 105, 113, 63, 17, 20, 20, 172, 10, 176, 236, 95, 127, 228, 214, 83, 211, 154, 224, 233, 51, 87, 83, 218, 193, 199, 241, 112, 242, 157, 123, 246, 52, 154, 118, 109, 213, 234, 47, 128, 32, 183, 253, 186, 201, 249, 198, 247, 139, 139, 196, 24, 15, 132, 137, 251, 16, 196, 65, 171, 189, 205, 56, 211, 94, 213, 156, 121, 59, 25, 131, 232, 185, 115, 47, 238, 199, 47, 145, 72, 120, 27, 13, 91, 241, 221, 125, 37, 134, 137, 120, 114, 32, 115, 212, 83, 226, 66, 105, 204, 12, 70, 24, 205, 31, 12, 13, 99, 66, 47, 64, 48 };
     int c=0;
     for (int i=(len%10+2)*28;c<256;i++,c++){
         c_data[i] ^= token[c];
     }
     jbyteArray j_ret=(*env).NewByteArray(len);
     (*env).SetByteArrayRegion(j_ret,0,len,(jbyte *)c_data);
     return j_ret;
}



