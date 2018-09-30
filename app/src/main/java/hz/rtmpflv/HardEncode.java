package hz.rtmpflv;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hz.LiveView;
import hz.flv.Flv;

/**
 * Created by Administrator on 2018-04-04.
 */

public class HardEncode {

    private MediaCodec mMediaCodec;
    private int mEncodeWidth;
    private int mEncodeHeight;
    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public void initVideoEncode(int w, int h) {
        try {
            String VIDEO_MIME = "video/avc";
            int FRAME_RATE = 15;
            int BIT_RATE = 2 * w * h * FRAME_RATE / 20;
            mEncodeWidth = w;
            mEncodeHeight = h;

            mSingleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int ret = LiveRtmp.getInstance().connect("rtmp://192.168.10.8/live/aaa");
                    Log.d("zhx", "LiveRtmp: " + ret);
                }
            });
            mMediaCodec = MediaCodec.createEncoderByType(VIDEO_MIME);

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(VIDEO_MIME, w, h);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
//            {color-format=21, i-frame-interval=1, mime=video/avc, width=352, bitrate-mode=2, bitrate=400000, frame-rate=15, height=640}
//            {color-format=2134288520, i-frame-interval=5, mime=video/avc, width=320, bitrate=125000, frame-rate=15, height=240}
            try {
                mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            } catch (Exception e) {
                Log.d("zhx", "initVideoEncode: ");
            }
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encodeYUV(byte[] buf) {
        /*int LENGTH = mEncodeWidth * mEncodeHeight;
        //YV12数据转化成COLOR_FormatYUV420Planar
        for (int i = LENGTH; i < (LENGTH + LENGTH / 4); i++) {
            byte temp = buf[i];
            buf[i] = buf[i + LENGTH / 4];
            buf[i + LENGTH / 4] = temp;
        }*/
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        try {
            int bufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            if (bufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[bufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buf);
                mMediaCodec.queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(), System.nanoTime() / 1000, 0);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

//                    mFlv.findNals(outputBuffer, bufferInfo);
                    int type = 0;
                    // 去H264头
                    for (int i = 0; i < 4; i ++) {
                        outputBuffer.get();
                    }

                    ByteBuffer frame = outputBuffer.slice();

                    type = frame.get(0) & 0x0f;

                    if (type == 7) {
                        Log.d("zhx", "encodeYUV: SPS" + bufferInfo.size);
                        LiveRtmp.getInstance().sendSpsPps(frame, bufferInfo);
                    } else if (type == 5) {
                        Log.d("zhx", "encodeYUV: I" + bufferInfo.size);
//                        LiveRtmp.getInstance().sendVideoTag(true, frame, bufferInfo);
                    } else {
//                        LiveRtmp.getInstance().sendVideoTag(false, frame, bufferInfo);
                    }

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
