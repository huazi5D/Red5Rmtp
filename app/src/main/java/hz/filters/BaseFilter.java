package hz.filters;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import hz.help.RenderHelp;
import hz.help.RotationUtil;

public class BaseFilter {

    public int mProgram;
    public int aPosHandle;
    public int aCooHandle;
    public int aTexHandle;
    public BaseFilter mNextFilter = null;
    public int mFboTextureId = -1;
    public Runnable mRunnable = null;
    public Runnable mReadRunable = null;
    public int u_Matrix = -1;
    public int mDrawTextureId = -1;

    public String vertex = "attribute vec4 a_Position;\n" +
            "attribute vec2 a_Coordinate;\n" +
            "varying   vec2 v_Coordinate;\n" +
            "void main() {\n" +
            "   gl_Position = a_Position;\n" +
            "   v_Coordinate = a_Coordinate;\n" +
            "}";

    public String fragment = "precision mediump float;\n" +
            "uniform sampler2D a_Texture;\n" +
            "varying vec2 v_Coordinate;\n" +
            "void main() {\n" +
            "   gl_FragColor = texture2D(a_Texture, v_Coordinate);\n" +
            "}";

    public void setNextFilter(BaseFilter nextFilter) {
        mNextFilter = nextFilter;
    }

    public void initFilter(int fboTextureId) {
        initFilter(fboTextureId, RotationUtil.TEXTURE_ROTATED_180, true);
    }

    float[] rotate;
    boolean flipHorizontal;
    public void initFilter(int fboTextureId, float[] rotate, boolean flipHorizontal) {
        this.rotate = rotate;
        this.flipHorizontal = flipHorizontal;
        mDrawTextureId = fboTextureId;
        mProgram = RenderHelp.createProgram(vertex, fragment);
        GLES20.glUseProgram(mProgram);

        aPosHandle           = GLES20.glGetAttribLocation (mProgram, "a_Position");
        aCooHandle           = GLES20.glGetAttribLocation (mProgram, "a_Coordinate");
        u_Matrix             = GLES20.glGetUniformLocation (mProgram, "u_Matrix");

        GLES20.glEnableVertexAttribArray(aPosHandle);
        GLES20.glEnableVertexAttribArray(aCooHandle);

        if (mNextFilter != null) {
            mFboTextureId = RenderHelp.createFBO(320, 240);
            mNextFilter.initFilter(mFboTextureId);
        }
    }

    public void draw() {
        if (mNextFilter != null)
            RenderHelp.bindFBO(mFboTextureId);
        GLES20.glUseProgram(mProgram);
        GLES20.glVertexAttribPointer(aPosHandle, 2, GLES20.GL_FLOAT, false, 0, RenderHelp.getPosBuffer());
        GLES20.glVertexAttribPointer(aCooHandle, 2, GLES20.GL_FLOAT, false, 0, RotationUtil.getRotation(rotate, flipHorizontal, false));
        if (mRunnable != null)
            mRunnable.run();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        if (mReadRunable != null)
            mReadRunable.run();

        if (mNextFilter != null) {
            RenderHelp.unbindFBO();
            mNextFilter.draw();
        }
    }

    private Bitmap convertToBitmap(byte[] ia) {
        if (ia == null) return null;
        int mWidth = 320;
        int mHeight = 240;
        byte[] iat = new byte[mWidth * mHeight * 4];
        for (int i = 0; i < mHeight; i++) {
            System.arraycopy(ia, i * mWidth * 4, iat, (mHeight - i - 1) * mWidth * 4, mWidth * 4);
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(iat));
        File file = new File("/sdcard/PNG/test.png");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

}
