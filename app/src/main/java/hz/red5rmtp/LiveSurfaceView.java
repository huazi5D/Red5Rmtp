package hz.red5rmtp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2018-04-04.
 */

public class LiveSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private LiveCamera mLiveCamera;

    public LiveSurfaceView(Context context) {
        this(context, null);
    }

    public LiveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLiveCamera = new LiveCamera();
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mLiveCamera.setPreview(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
