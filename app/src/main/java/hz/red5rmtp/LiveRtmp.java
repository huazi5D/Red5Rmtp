package hz.red5rmtp;

/**
 * Created by Administrator on 2018-04-10.
 */

public class LiveRtmp {

    private static LiveRtmp mInstance;

    private LiveRtmp() {

    }

    public synchronized static LiveRtmp getInstance() {
        if (mInstance == null)
            mInstance = new LiveRtmp();
        return mInstance;
    }

    static {
        System.loadLibrary("rtmp_push");
    }

    public native int connect(String url);

    public native int push(byte[] buf, int length);

    public native int close();
}
