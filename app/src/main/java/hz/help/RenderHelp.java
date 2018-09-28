package hz.help;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class RenderHelp {

    private static float[] mPosCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
    public static float[] mTexCoordinate = {1, 0, 0, 0, 1, 1, 0, 1};// {0, 0, 1, 0, 0, 1, 1, 1}

    private static int mFrameBufferId;
    private static int mRenderBufferId;

    public static int createProgram(String vertex, String fragment) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);

        int program = GLES20.glCreateProgram();
        if (program != 0)
        {
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, pixelShader);
            GLES20.glLinkProgram(program);
            checkError();
        }

        // 释放shader资源
        GLES20.glDeleteShader(vertexShader );
        GLES20.glDeleteShader(pixelShader);

        return program;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        checkError();
        return shader;
    }

    private static void checkError() {
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            Log.d("RenderHelp", "gles error!!!");
        }
    }

    public static int createOESTexture() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    public static FloatBuffer getPosBuffer() {
        FloatBuffer fb = ByteBuffer.allocateDirect(mPosCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(mPosCoordinate);
        fb.position(0);
        return fb;
    }

    public static FloatBuffer getTexBuffer() {
        FloatBuffer fb = ByteBuffer.allocateDirect(mTexCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(mTexCoordinate);
        fb.position(0);
        return fb;
    }

    public static int createFBO(int width, int height) {
        int[] texture=new int[1];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1,texture,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        int[] framebuffer = new int[1];
        GLES20.glGenFramebuffers(1, framebuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture[0], 0);
        mFrameBufferId = framebuffer[0];

        int[] renderBuffer = new int[1];
        GLES20.glGenRenderbuffers(1, renderBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER,renderBuffer[0]);
        mRenderBufferId = renderBuffer[0];

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("zhx", "createTextureWithFBO: faild!!!");
        }
        unbindFBO();

        return texture[0];
    }

    public static void bindFBO(int textureId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mRenderBufferId);
    }

    public static void unbindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

}
