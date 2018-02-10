package heavy.tool.test.test.wrapper;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import heavy.test.plugin.model.data.TestObject;
import heavy.test.plugin.model.data.result.RecordResult;
import heavy.test.plugin.model.data.testable.global.Delay;
import heavy.test.plugin.model.data.testable.global.GlobalTestable;
import heavy.test.plugin.model.data.testable.global.MakeToast;
import heavy.test.plugin.model.data.testable.global.SendMessage;
import heavy.test.plugin.model.data.testable.global.StopTest;
import heavy.test.plugin.model.data.testable.global.ThrowException;
import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.test.util.DelayUtil;
import heavy.tool.test.util.LogUtil;


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
    public TestObject runTest(Context context) throws Throwable {

        if (mGlobalTestable instanceof Delay) {
            Delay Delay = (Delay) mGlobalTestable;
            String message = "execute Delay : " + Delay.getDelayMillis();
            DelayUtil.delay(Delay.getDelayMillis());
            LogUtil.d(TAG, message);
            return new RecordResult().setInfo(message);
        }
        if (mGlobalTestable instanceof ThrowException) {
            ThrowException ThrowException = (ThrowException) mGlobalTestable;
            LogUtil.d(TAG, "execute ThrowException : " + ThrowException.getExceptionMessage());
            throw new RuntimeException(ThrowException.getExceptionMessage());
        }
        if (mGlobalTestable instanceof SendMessage) {
            SendMessage SendMessage = (SendMessage) mGlobalTestable;
            String message = "execute SendMessage : " + SendMessage.getMessage();
            LogUtil.d(TAG, message);
            return new RecordResult().setInfo(message);
        }
        if (mGlobalTestable instanceof MakeToast) {
            MakeToast MakeToast = (MakeToast) mGlobalTestable;
            String message = "execute MakeToast : " + MakeToast.getToastMessage();
            showToastMessage(context, MakeToast.getToastMessage(), MakeToast.getToastTime());
            LogUtil.d(TAG, message);
            return new RecordResult().setInfo(message);
        }
        if (mGlobalTestable instanceof StopTest) {
            StopTest StopTest = (StopTest) mGlobalTestable;
            LogUtil.d(TAG, "execute StopTest : " + StopTest.getStopMessage());
            return mGlobalTestable;
        }

        throw new IllegalArgumentException("unknown global testable : " + mGlobalTestable);
    }

}
