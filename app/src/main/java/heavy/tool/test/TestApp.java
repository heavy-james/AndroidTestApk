package heavy.tool.test;

import android.app.Application;
import android.os.StrictMode;

import heavy.tool.test.util.LogUtil;


/**
 * Created by v_hfzhan on 2017/5/17.
 */

public class TestApp extends Application {

    private static final String TAG = "TestApp";
    public static boolean DEVELOPER_MODE = true;

    @Override
    public void onCreate() {
        LogUtil.i(TAG, "onCreate");
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
