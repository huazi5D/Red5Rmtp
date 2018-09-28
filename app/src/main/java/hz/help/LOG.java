package hz.help;

import android.util.Log;

public class LOG {

    private static String TAG = "RTMPDemo";

    public static void d(String className, String text) {
        Log.d(TAG, className + " : " + text);
    }

}
