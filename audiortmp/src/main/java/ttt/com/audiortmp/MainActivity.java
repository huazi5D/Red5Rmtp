package ttt.com.audiortmp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i ++) {
                    Log.d("zhx", "run: " + i);
                }
            }
        };
        runnable.run();
        Log.d("zhx", "onCreate: end");
    }
}
