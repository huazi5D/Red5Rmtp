package hz.rtmpflv;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018-04-03.
 */

public class LiveCamera implements Camera.PreviewCallback{

    private Camera mCamera;
    private int mWidth = 320;
    private int mHeight = 240;
    private byte[] callbackBuffer;
    private HardEncode mHardEncode;
    public LiveCamera() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        parameters.setPreviewSize(mWidth, mHeight);
        parameters.setPreviewFormat(ImageFormat.NV21);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);

        mHardEncode = new HardEncode();
        mHardEncode.initVideoEncode(mWidth, mHeight);

    }

    public void setPreview(SurfaceView surfaceView) {
        try {
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            callbackBuffer = new byte[mWidth * mHeight * 3 / 2];
            mCamera.addCallbackBuffer(callbackBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        Log.d("zhx", "onPreviewFrame: ");
        mHardEncode.encodeYUV(data);
        mCamera.addCallbackBuffer(callbackBuffer);
    }
}
