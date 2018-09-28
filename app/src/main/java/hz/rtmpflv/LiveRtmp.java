package hz.rtmpflv;

import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2018-04-10.
 */

public class LiveRtmp {

    private static LiveRtmp mInstance;

    private LiveRtmp() {

    }

    public synchronized static LiveRtmp getInstance() {
        if (mInstance == null)
            mInstance = new LiveRtmp();
        return mInstance;
    }

    static {
        System.loadLibrary("rtmp_push");
    }

    public void sendSpsPps(ByteBuffer spsPps, MediaCodec.BufferInfo info) {
        int j = 0;
        for (int i = spsPps.position(); i < info.offset + info.size - 3; i ++) {
            if (spsPps.get(i) == 0x00 && spsPps.get(i + 1) == 0x00 && spsPps.get(i + 2) == 0x00 && spsPps.get(i + 3) == 0x01) {
                break;
            }
            j ++;
        }

        byte[] sps = new byte[j];
        byte[] pps = new byte[info.size - j - 4 - 4];
        spsPps.get(sps);

        for (int i = 0; i < 4; i ++) {
            spsPps.get();
        }
        spsPps.get(pps);

        ByteBuffer buffer = ByteBuffer.allocate(16 + sps.length + pps.length);
        buffer.put((byte)0x17);
        buffer.put((byte)0x00);
        buffer.put((byte)0x00);
        buffer.put((byte)0x00);
        buffer.put((byte)0x00);
        buffer.put((byte)0x01);
        buffer.put(sps[1]);
        buffer.put(sps[2]);
        buffer.put(sps[3]);
        buffer.put((byte)0xff);

        buffer.put((byte)0xe1);
        buffer.putShort((short)sps.length);
        buffer.put(sps);

        buffer.put((byte)0x01);
        buffer.putShort((short)pps.length);
        buffer.put(pps);

        push(preTime,buffer.array(), buffer.array().length);

        preTime = System.currentTimeMillis();

    }

    public final static int SequenceHeader                 = 0;
    public final static int NALU                         = 1;

    public final static int KeyFrame                   = 1;
    public final static int InterFrame                 = 2;

    public final static int AVC                     = 7;

    private long preTime = 0;

    public void sendVideoTag(boolean isKeyFrame, ByteBuffer frameBuffer, MediaCodec.BufferInfo info) {

        byte[] data = new byte[info.size - 4];
        frameBuffer.get(data);

        ByteBuffer buffer = ByteBuffer.allocate(9 + data.length);
        int frameType = -1;
        if (isKeyFrame) {
            frameType = KeyFrame;
        } else {
            frameType = InterFrame;
        }
        byte first = (byte) (((frameType & 0x0F) << 4) | (AVC & 0x0F));
        buffer.put(first);
        buffer.put((byte) NALU);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.putInt(data.length);
        buffer.put(data);

        push(System.currentTimeMillis() - preTime,buffer.array(), buffer.array().length);

    }

    public native int connect(String url);

    public native int push(long timestamp, byte[] buf, int length);

    public native int close();

    public native int rtmpflv();

    public native int test();

}
