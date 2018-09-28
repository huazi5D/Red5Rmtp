package hz.rtmpflv;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hz.help.RenderHelp;

public class LiveGLSurfaceView extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener{

    private Camera mCamera;
    private int mWidth = 320;
    private int mHeight = 240;
    public LiveGLSurfaceView(Context context) {
        this(context, null);
    }

    public LiveGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(new GLRender());
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    private class GLRender implements Renderer {

        private int uPosHandle;
        private int aTexHandle;
        private int mMVPMatrixHandle;
        private SurfaceTexture surfaceTexture;
        private float[] mMVPMatrix = new float[16];
        private ByteBuffer ib;
        private HardEncode mHardEncode;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            /*int program = RenderHelp.createProgram();
            GLES20.glUseProgram(program);
            surfaceTexture = new SurfaceTexture(RenderHelp.createOESTexture());
            surfaceTexture.setOnFrameAvailableListener(LiveGLSurfaceView.this);

            uPosHandle           = GLES20.glGetAttribLocation (program, "a_Position");
            aTexHandle           = GLES20.glGetAttribLocation (program, "a_Coordinate");

            GLES20.glVertexAttribPointer(uPosHandle, 2, GLES20.GL_FLOAT, false, 0, RenderHelp.getPosBuffer());
            GLES20.glVertexAttribPointer(aTexHandle, 2, GLES20.GL_FLOAT, false, 0, RenderHelp.getTexBuffer());

            GLES20.glEnableVertexAttribArray(uPosHandle);
            GLES20.glEnableVertexAttribArray(aTexHandle);

//            RenderHelp.mFBOTextureId = RenderHelp.createFBO(mWidth, mHeight);
            ib = ByteBuffer.allocate(mWidth * mHeight);
            openCamera();*/
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

//            RenderHelp.bindFBO();
            surfaceTexture.updateTexImage();
            surfaceTexture.getTransformMatrix(mMVPMatrix);
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
//            mHardEncode.encodeYUV(ib);
//            RenderHelp.unbindFBO();

        }

        public void openCamera() {
            try {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(ImageFormat.YV12);
                parameters.setPictureSize(mWidth, mHeight);
                parameters.setPreviewSize(mWidth, mHeight);
                parameters.setPreviewFormat(ImageFormat.YV12);
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains("continuous-video")) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                mCamera.setParameters(parameters);
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();

                mHardEncode = new HardEncode();
                mHardEncode.initVideoEncode(mWidth, mHeight);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
