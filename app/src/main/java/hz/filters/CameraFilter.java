package hz.filters;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import hz.LiveCamera;
import hz.LiveView;
import hz.help.Key;
import hz.help.RenderHelp;
import hz.help.RotationUtil;
import hz.help.YuvUtils;
import hz.rtmpflv.HardEncode;

public class CameraFilter extends BaseFilter{

    private SurfaceTexture mSurfaceTexture;
    private LiveCamera mLiveCamera = new LiveCamera();
    private float[] mMVPMtrix = new float[16];
    private ByteBuffer mFBOBuffer;
    private HardEncode mHardEncode;
    private byte[] yuvData;
    private int i = 0;
    private int mIndex = 0;
    private ByteBuffer[] mBuffers = new ByteBuffer[10];
    private ConcurrentLinkedQueue<ByteBuffer> mGLByteByfferCache = new ConcurrentLinkedQueue<>();

    private ByteBuffer getBuffer() {
        if (mIndex == 10)
            mIndex = 0;
        return mBuffers[mIndex ++];
    }

    public CameraFilter(final LiveView liveView) {

        for (int i = 0; i < 10; i ++) {
            mBuffers[i] = ByteBuffer.allocate(320 * 240 * 4);
        }

        mHardEncode = new HardEncode();
        mHardEncode.initVideoEncode(240, 320);

        vertex = "attribute vec4 a_Position;\n" +
                "uniform mat4 u_Matrix;" +
                "attribute vec2 a_Coordinate;\n" +
                "varying   vec2 v_Coordinate;\n" +
                "void main() {\n" +
                "   gl_Position = a_Position;\n" +
                "   v_Coordinate = (u_Matrix * vec4(a_Coordinate, 1, 1)).xy;\n" +
                "}";

        fragment = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES a_Texture;\n" +
                "varying vec2 v_Coordinate;\n" +
                "void main() {\n" +
                "   gl_FragColor = texture2D(a_Texture, v_Coordinate);\n" +
                "}";

        mFBOBuffer = ByteBuffer.allocate(320 * 240 * 4);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mMVPMtrix);
                GLES20.glUniformMatrix4fv(u_Matrix, 1, false, mMVPMtrix, 0);
            }
        };

        yuvData = new byte[320 * 240 * 3 / 2];

        mReadRunable = new Runnable() {
            @Override
            public void run() {
                ByteBuffer byteBuffer = getBuffer();
                GLES20.glReadPixels(0, 0, 240, 320, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
                if (mGLByteByfferCache.size() == 10) {
                    mGLByteByfferCache.poll();
                }
                mGLByteByfferCache.add(byteBuffer);
                i ++;
                if (i == 100) {
                    convertToBitmap(byteBuffer.array());
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                ByteBuffer picture = mGLByteByfferCache.poll();
                                if (picture != null) {
                                    YuvUtils.RgbaToI420(Key.ARGB_TO_I420, picture.array(), yuvData, 240, 320);
                                    mHardEncode.encodeYUV(yuvData);
                                }
                            }
                        }
                    }).start();*/
                }
                liveView.requestRender();
            }
        };
    }

    @Override
    public void initFilter(int fbotextureId) {
        super.initFilter(fbotextureId, RotationUtil.TEXTURE_NO_ROTATION, false);
        mSurfaceTexture = new SurfaceTexture(RenderHelp.createOESTexture());
        mLiveCamera.setPreview(mSurfaceTexture);
    }

    @Override
    public void draw() {
        GLES20.glViewport(0, 0, 320, 240);
        super.draw();
    }
}
