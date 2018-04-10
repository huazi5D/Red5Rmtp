package hz.red5rmtp;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hz.flv.FlvPacker;
import hz.flv.Packer;

/**
 * Created by Administrator on 2018-04-04.
 */

public class HardEncode {

    private MediaCodec mMediaCodec;
    private int mEncodeWidth;
    private int mEncodeHeight;
    private FlvPacker mFlvPacker;
    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();


    public void initVideoEncode(int w, int h) {
        try {
            String VIDEO_MIME = "video/avc";
            int FRAME_RATE = 15;
            int BIT_RATE = 2 * w * h * FRAME_RATE / 20;
            mEncodeWidth = w;
            mEncodeHeight = h;

            mFlvPacker = new FlvPacker();
            mFlvPacker.initVideoParams(mEncodeWidth, mEncodeHeight, 15);
            mFlvPacker.setPacketListener(new Packer.OnPacketListener() {
                @Override
                public void onPacket(final byte[] data, int packetType) {
                    mSingleThreadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            LiveRtmp.getInstance().push(data, data.length);
                        }
                    });
                }
            });
            mFlvPacker.start();

            mSingleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int ret = LiveRtmp.getInstance().connect("rtmp://192.168.10.136:5051/live/demo");
                    Log.d("zhx", "LiveRtmp: " + ret);
                }
            });
            mMediaCodec = MediaCodec.createEncoderByType(VIDEO_MIME);

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(VIDEO_MIME, w, h);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encodeYUV(byte[] buf) {
        int LENGTH = mEncodeWidth * mEncodeHeight;
        //YV12数据转化成COLOR_FormatYUV420Planar
        for (int i = LENGTH; i < (LENGTH + LENGTH / 4); i++) {
            byte temp = buf[i];
            buf[i] = buf[i + LENGTH / 4];
            buf[i + LENGTH / 4] = temp;
        }

        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        try {
            int bufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            if (bufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[bufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buf, 0, buf.length);
                mMediaCodec.queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(), System.nanoTime() / 1000, 0);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    // TODO push
                    mFlvPacker.onVideoData(outputBuffer, bufferInfo);
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                }
            } else {
                Log.d("zhx", "encodeYUV: No buffer available !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
