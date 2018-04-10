#include <jni.h>
#include <string>
#include <malloc.h>
#include <librtmp/rtmp.h>
#include <librtmp/log.h>
#include<android/log.h>

#define HTON16(x)  ((x>>8&0xff)|(x<<8&0xff00))
#define HTON24(x)  ((x>>16&0xff)|(x<<16&0xff0000)|(x&0xff00))
#define HTON32(x)  ((x>>24&0xff)|(x>>8&0xff00)|\
    (x<<8&0xff0000)|(x<<24&0xff000000))
#define HTONTIME(x) ((x>>16&0xff)|(x<<16&0xff0000)|(x&0xff00)|(x&0xff000000))

RTMP *rtmp = NULL;
RTMPPacket *packet = NULL;

extern "C"
JNIEXPORT jint JNICALL Java_hz_red5rmtp_LiveRtmp_connect( JNIEnv *env, jobject obj, jstring url_) {

    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = 5;
    RTMP_SetupURL(rtmp, "rtmp://192.168.10.136:5051/live/demo");
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

extern "C"
JNIEXPORT jint JNICALL Java_hz_red5rmtp_LiveRtmp_push( JNIEnv *env, jobject obj, jbyteArray buf_, jint length) {
    jbyte *buf = env->GetByteArrayElements(buf_, NULL);
    if (length < 15) {
        return -1;
    }

    uint32_t type = 0;
    uint32_t datalength = 0;
    uint32_t timestamp = 0;
    uint32_t streamid = 0;

    memcpy(&type, buf, 1);
    buf++;
    memcpy(&datalength, buf, 3);

    datalength = HTON24(datalength);
    buf += 3;
    memcpy(&timestamp, buf, 4);
    timestamp = HTONTIME(timestamp);
    buf += 4;
    memcpy(&streamid, buf, 3);
    streamid = HTON24(streamid);
    buf += 3;

    if (type != 0x08 && type != 0x09) {
        return -2;
    }
    if (datalength != (length - 11 - 4)) {
        return -3;
    }

    memcpy(packet->m_body, buf, length - 11 - 4);

    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp = timestamp;
    packet->m_packetType = type;
    packet->m_nBodySize = datalength;
    RTMP_IsConnected(rtmp);
    RTMP_SendPacket(rtmp, packet, 0);

    buf -= 11;
    env->ReleaseByteArrayElements(buf_, buf, 0);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL Java_hz_red5rmtp_LiveRtmp_close( JNIEnv *env, jobject obj) {
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
