#include <jni.h>
#include <string>
#include <malloc.h>
#include <librtmp/rtmp.h>
#include <librtmp/log.h>
#include "librtmp/rtmp_sys.h"

#include<android/log.h>

#define HTON16(x)  ((x>>8&0xff)|(x<<8&0xff00))
#define HTON24(x)  ((x>>16&0xff)|(x<<16&0xff0000)|(x&0xff00))
#define HTON32(x)  ((x>>24&0xff)|(x>>8&0xff00)|\
    (x<<8&0xff0000)|(x<<24&0xff000000))
#define HTONTIME(x) ((x>>16&0xff)|(x<<16&0xff0000)|(x&0xff00)|(x&0xff000000))

RTMP *rtmp = NULL;
RTMPPacket *packet = NULL;

extern "C"
JNIEXPORT jint JNICALL Java_hz_rtmpflv_LiveRtmp_connect( JNIEnv *env, jobject obj, jstring url_) {

    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = 5;
    RTMP_SetupURL(rtmp, "rtmp://192.168.10.95/live/aaa");
    RTMP_EnableWrite(rtmp);
    RTMP_Connect(rtmp, NULL);
    RTMP_ConnectStream(rtmp, 0);
    packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, 1024 * 64);
    RTMPPacket_Reset(packet);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    return 0;
}
int p = 0;
extern "C"
JNIEXPORT jint JNICALL Java_hz_rtmpflv_LiveRtmp_push( JNIEnv *env, jobject obj, jlong timestamp, jbyteArray buf_, jint length) {

    packet->m_body = reinterpret_cast<char *>(env->GetByteArrayElements(buf_, 0));
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp = timestamp;
    packet->m_packetType = 0x09;
    packet->m_nBodySize  = length;

    /*if (p == 1) {
        FILE *f=NULL;
        f = fopen("sdcard/1/body1.txt","wb");
        fwrite(packet->m_body, 1, length, f);
        fclose(f);
    }
    p ++;*/

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, 0);
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL Java_hz_rtmpflv_LiveRtmp_close( JNIEnv *env, jobject obj) {
    if (rtmp != NULL) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp = NULL;
    }
    if (packet != NULL) {
        RTMPPacket_Free(packet);
        free(packet);
        packet = NULL;
    }
    return 0;
}


/*read 1 byte*/
int ReadU8(uint32_t *u8,FILE*fp){
    if(fread(u8,1,1,fp)!=1)
        return 0;
    return 1;
}
/*read 2 byte*/
int ReadU16(uint32_t *u16,FILE*fp){
    if(fread(u16,2,1,fp)!=1)
        return 0;
    *u16=HTON16(*u16);
    return 1;
}
/*read 3 byte*/
int ReadU24(uint32_t *u24,FILE*fp, int a = 0){
    if(fread(u24,3,1,fp)!=1)
        return 0;
    *u24=HTON24(*u24);
    return 1;
}
/*read 4 byte*/
int ReadU32(uint32_t *u32,FILE*fp){
    if(fread(u32,4,1,fp)!=1)
        return 0;
    *u32=HTON32(*u32);
    return 1;
}
/*read 1 byte,and loopback 1 byte at once*/
int PeekU8(uint32_t *u8,FILE*fp){
    if(fread(u8,1,1,fp)!=1)
        return 0;
    fseek(fp,-1,SEEK_CUR);
    return 1;
}
/*read 4 byte and convert to time format*/
int ReadTime(uint32_t *utime,FILE*fp){
    if(fread(utime,4,1,fp)!=1)
        return 0;
    *utime=HTONTIME(*utime);
    return 1;
}


extern "C"
JNIEXPORT jint JNICALL Java_hz_rtmpflv_LiveRtmp_test( JNIEnv *env, jobject obj) {
    uint32_t datalength=258;
//    FILE*fp=NULL;
//    fp=fopen("sdcard/1/aaa.txt","rb");
//    ReadU24(&datalength,fp, 1);
    __android_log_print(ANDROID_LOG_INFO, "zhx", "%d %d", (datalength >> 8) & 0xff, datalength & 0xff);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL Java_hz_rtmpflv_LiveRtmp_rtmpflv( JNIEnv *env, jobject obj) {
    RTMP *rtmp=NULL;
    RTMPPacket *packet=NULL;
    uint32_t preTagsize=0;

    uint32_t type=0;
    uint32_t datalength=0;
    uint32_t timestamp=0;
    uint32_t streamid=0;

    FILE*fp=NULL;
    fp=fopen("sdcard/1/easy1.flv","rb");
    if (!fp){
        RTMP_LogPrintf("Open File Error.\n");
        return -1;
    }

    rtmp=RTMP_Alloc();
    RTMP_Init(rtmp);
    //set connection timeout,default 30s
    rtmp->Link.timeout=5;
    RTMP_SetupURL(rtmp,"rtmp://192.168.10.136/live/aaa");
    RTMP_EnableWrite(rtmp);
    RTMP_Connect(rtmp,NULL);
    RTMP_ConnectStream(rtmp,0);

    packet=(RTMPPacket*)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet,1024*64);
    RTMPPacket_Reset(packet);

    // 是否含有Extend timeStamp字段
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    fseek(fp,9,SEEK_SET);
    fseek(fp,4,SEEK_CUR);

    while(1)
    {
        if(!ReadU8(&type,fp))
            break;
        if(!ReadU24(&datalength,fp, 1))
            break;
        if(!ReadTime(&timestamp,fp))
            break;
        if(!ReadU24(&streamid,fp))
            break;

        if (type!=0x08&&type!=0x09){
            fseek(fp,datalength + 4,SEEK_CUR);
            continue;
        }

        if(fread(packet->m_body,1,datalength,fp)!=datalength)
            break;

        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
        packet->m_nTimeStamp = timestamp;
        packet->m_packetType = type;
        packet->m_nBodySize  = datalength;

        if (p == 2) {
            FILE *f=NULL;
            f = fopen("sdcard/1/body.txt","wb");
            fwrite(packet->m_body, 1, datalength, f);
            fclose(f);
        }
        p ++;

        __android_log_print(ANDROID_LOG_INFO, "ZHX", "SHIJIANCHUO:%d",timestamp);
        if (!RTMP_IsConnected(rtmp)){
            RTMP_Log(RTMP_LOGERROR,"rtmp is not connect\n");
            break;
        }
        if (!RTMP_SendPacket(rtmp,packet,0)){
            RTMP_Log(RTMP_LOGERROR,"Send Error\n");
            break;
        }

        if(!ReadU32(&preTagsize,fp))
            break;
    }

    RTMP_LogPrintf("\nSend Data Over\n");

    if(fp)
        fclose(fp);

    if (rtmp!=NULL){
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp=NULL;
    }
    if (packet!=NULL){
        RTMPPacket_Free(packet);
        free(packet);
        packet=NULL;
    }

    return 0;
}
