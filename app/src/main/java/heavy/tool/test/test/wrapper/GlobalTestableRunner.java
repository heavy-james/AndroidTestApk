package heavy.tool.test.test.wrapper;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import heavy.test.plugin.model.data.testable.global.GlobalTestable;
import heavy.test.plugin.model.data.testable.global.TestableDelay;
import heavy.test.plugin.model.data.testable.global.TestableException;
import heavy.test.plugin.model.data.testable.global.TestableMessage;
import heavy.test.plugin.model.data.testable.global.TestableStop;
import heavy.test.plugin.model.data.testable.global.TestableToast;
import heavy.tool.test.util.LogUtil;

import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.test.model.AbortTestException;
import heavy.tool.test.test.model.TestResult;
import heavy.tool.test.test.util.DelayUtil;


/**
 * Created by heavy on 2017/6/1.
 */

public class GlobalTestableRunner implements ITestObjectRunner {

    public static final String TAG = "GlobalTestableRunner";

    private GlobalTestable mGlobalTestable;

    public GlobalTestableRunner(GlobalTestable globalTestable) {
        mGlobalTestable = globalTestable;
    }

    public static void showToastMessage(final Context context, final String text, final long duration) throws Throwable {
        LogUtil.i(TAG, "showToastMessage text : " + text + "; duration :" + duration);
        TestEntrance.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                toast.show();
                Handler handler = new Handler(context.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, duration);
            }
        });

    }

    @Override
    public boolean runTest(Context context, TestResult testResult) throws Throwable {

        if (mGlobalTestable instanceof TestableDelay) {
            TestableDelay testableDelay = (TestableDelay) mGlobalTestable;
            testResult.writeTestableInfo(TAG, "execute TestableDelay : " + testableDelay.getDelayMillis());
            DelayUtil.delay(testableDelay.getDelayMillis());
            return true;
        }
        if (mGlobalTestable instanceof TestableException) {
            TestableException testableException = (TestableException) mGlobalTestable;
            testResult.writeTestableInfo(TAG, "execute TestableException : " + testableException.getExceptionMessage());
            throw new RuntimeException(testableException.getExceptionMessage());
        }
        if (mGlobalTestable instanceof TestableMessage) {
            TestableMessage testableMessage = (TestableMessage) mGlobalTestable;
            testResult.writeTestableInfo(TAG, "execute TestableMessage : " + testableMessage.getMessage());
            return true;
        }
        if (mGlobalTestable instanceof TestableToast) {
            TestableToast testableToast = (TestableToast) mGlobalTestable;
            testResult.writeTestableInfo(TAG, "execute TestableToast : " + testableToast.getToastMessage());
            showToastMessage(context, testableToast.getToastMessage(), testableToast.getToastTime());
            return true;
        }
        if (mGlobalTestable instanceof TestableStop) {
            TestableStop testableStop = (TestableStop) mGlobalTestable;
            testResult.writeTestableInfo(TAG, "execute TestableStop : " + testableStop.getStopMessage());
            throw new AbortTestException(testableStop.getStopMessage());
        }

        throw new IllegalArgumentException("unknown global testable : " + mGlobalTestable);
    }

}
