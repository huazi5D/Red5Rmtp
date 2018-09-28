package hz.filters;

import android.opengl.GLES20;

public class ScreenFilter extends BaseFilter {

    private int mWidth;
    private int mHeight;

    public ScreenFilter() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDrawTextureId);
            }
        };
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    @Override
    public void initFilter(int fbotextureId) {
        super.initFilter(fbotextureId);

        aTexHandle = GLES20.glGetUniformLocation (mProgram, "a_Texture");
        GLES20.glUniform1f(aTexHandle, 0);
    }

    @Override
    public void draw() {
        GLES20.glViewport(0, 0, 500, 500);
        super.draw();
    }
}
