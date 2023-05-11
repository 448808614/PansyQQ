//
// Created by HASEE on 2019-07-20.
//
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <elf.h>
#include <sys/mman.h>
#include <fcntl.h>


void init_decrypt() __attribute__((constructor));
unsigned long getLibAddr();

//解密函数
void init_decrypt(){
    unsigned int nblock;
    unsigned int nsize;
    unsigned long base;
    unsigned long text_addr;
    unsigned int i;
    Elf32_Ehdr *ehdr;

    base=getLibAddr();

    ehdr=(Elf32_Ehdr *)base;
    //text_addr=0x14A14+base;
    text_addr=ehdr->e_flags+base;//偏移

    nblock=ehdr->e_entry>>16;//长度
    nsize=ehdr->e_entry&0xffff;//页数

    __android_log_print(ANDROID_LOG_INFO,"JNITag","nblock=0x%x,nsize:%d",nblock,nsize);
    __android_log_print(ANDROID_LOG_INFO,"JNITag","base=0x%x",text_addr);
    // printf("nblock=%d\n,nblock");

    if(mprotect((void *)(text_addr/PAGE_SIZE*PAGE_SIZE),PAGE_SIZE*nsize,PROT_READ|PROT_EXEC|PROT_WRITE)!=0){
        //puts("mem privilege change failed");
        __android_log_print(ANDROID_LOG_INFO,"JNITag","mem privilege change failed");
    }

    for(i=0;i<nblock;i++){
        char *addr=(char*)(text_addr+i);
        *addr=(*addr)^1;
    }

    if(mprotect((void *)(text_addr/PAGE_SIZE*PAGE_SIZE),PAGE_SIZE*nsize,PROT_READ|PROT_EXEC)!=0){
        //puts("mem privilege change failed");
        __android_log_print(ANDROID_LOG_INFO,"JNITag","mem privilege change failed");
    }
    //puts("Decrypt success");
    __android_log_print(ANDROID_LOG_INFO,"JNITag","Decrypt success");

}

unsigned long getLibAddr(){
    unsigned long ret=0;
    char name[]="libOpenssl.so";
    char buf[PAGE_SIZE],*temp;
    int pid;
    FILE *fp;
    pid=getpid();
    sprintf(buf,"/proc/%d/maps",pid);
    fp=fopen(buf,"r");
    if(fp==NULL){
        puts("open failed");
        goto _error;
    }
    while(fgets(buf,sizeof(buf),fp)){
        if(strstr(buf,name)){
            temp=strtok(buf,"-");
            ret=strtoul(temp,NULL,16);
            break;
        }
    }
    _error:
    fclose(fp);
    return ret;

}

