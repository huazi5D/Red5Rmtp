package hz;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;
import java.util.List;

public class LiveCamera {

    private Camera mCamera;
    private int mWidth = 320;
    private int mHeight = 240;

    public LiveCamera() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mWidth, mHeight);
        parameters.setPreviewFormat(ImageFormat.NV21);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(parameters);
    }

    public void setPreview(SurfaceTexture surfaceTexture) {
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
