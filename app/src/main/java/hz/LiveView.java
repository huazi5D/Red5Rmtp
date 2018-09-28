package hz;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hz.filters.BaseFilter;
import hz.help.RotationUtil;

public class LiveView extends GLSurfaceView {

    private BaseFilter mOrgFilter = null;

    public LiveView(Context context) {
        super(context);
    }

    public LiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(new GLRender());
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    private class GLRender implements Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mOrgFilter.initFilter(-1);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
            mOrgFilter.draw();
        }

    }

    /*------------------------------------------接口 start------------------------------------------------*/
    public void setFilter(BaseFilter filter) {
        mOrgFilter = filter;
    }

    /*------------------------------------------接口 end--------------------------------------------------*/
}
