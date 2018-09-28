package hz.rtmpflv;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import hz.LiveView;
import hz.filters.CameraFilter;
import hz.filters.ScreenFilter;

public class MainActivity extends Activity {

    private LiveView mLiveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("zhx", "onCreate: ");
        /*mLiveView = findViewById(R.id.live_view);
        CameraFilter cameraFilter = new CameraFilter(mLiveView);
        mLiveView.setFilter(cameraFilter);
        ScreenFilter screenFilter = new ScreenFilter();
        cameraFilter.setNextFilter(screenFilter);*/
    }

}
