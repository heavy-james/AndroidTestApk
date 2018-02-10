package heavy.tool.test.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.elvishew.xlog.LogLevel;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import heavy.test.plugin.logic.transport.SocketClient;
import heavy.test.plugin.model.data.IntentData;
import heavy.test.plugin.model.data.TestObject;
import heavy.test.plugin.model.data.factory.TestObjectFactory;
import heavy.test.plugin.model.data.reflection.MethodData;
import heavy.test.plugin.model.data.reflection.ObjectData;
import heavy.test.plugin.model.data.reflection.RuntimeValue;
import heavy.test.plugin.model.data.result.RecordResult;
import heavy.test.plugin.model.data.testable.global.StopTest;
import heavy.tool.test.test.util.ReflectionUtil;
import heavy.tool.test.test.wrapper.IntentWrapper;
import heavy.tool.test.test.wrapper.TestObjectRunner;
import heavy.tool.test.util.LogUtil;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;


@RunWith(AndroidJUnit4.class)
public class TestEntrance extends ActivityTestRule<Activity> {

    public static final String TAG = "TestEntrance";
    static TestEntrance instance = null;
    static boolean testRunning = true;
    static private Context mContext;
    Activity mActivity;
    private SocketClient mSocketClient;

    public TestEntrance() {
        super(null);
        instance = this;
    }

    public static synchronized TestEntrance getInstance() {
        return instance;
    }

    @BeforeClass
    public static void staticSetUp() throws Throwable {
        heavy.test.plugin.util.LogUtil.init(LogLevel.ALL, true, "/sdcard/xlog/heavy.tool.test/test_result.txt");
        LogUtil.i(TAG, "staticSetUp");
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @AfterClass
    public static void staticTearDown() {

    }

    public Context getContext() {
        return mContext;
    }

    @Test
    public void runTests() {
        LogUtil.d(TAG, "runTests start test server...");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable throwable) {
                LogUtil.d(TAG, "runTests occurs unexpected exception, aborting...");
                testRunning = false;
                String exceptionAsString = Log.getStackTraceString(throwable);
                LogUtil.e(TAG, exceptionAsString);
                final RecordResult recordResult = new RecordResult();
                recordResult.setInfo("execute command failed : " + exceptionAsString);
                recordResult.setFailed(true);
                recordResult.setLevel(RecordResult.LEVEL_DETAIL);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendResult(recordResult);
                        } catch (Throwable throwable1) {
                            throwable1.printStackTrace();
                        }
                        mSocketClient.close();
                        System.exit(-1);
                    }
                });
                throwable.printStackTrace();
            }
        });

        try {
            ServerSocket serverSocket = new ServerSocket(10001, 10);
            LogUtil.d(TAG, "runTests start create server socket success");
            Socket socket = serverSocket.accept();
            LogUtil.d(TAG, "runTests start accept socket : " + socket);
            mSocketClient = new SocketClient(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (testRunning && !mSocketClient.isClosed()) {
            try {

                String commandStr = mSocketClient.readLine();

                LogUtil.d(TAG, "runTests get string : " + commandStr);

                checkNotNull(commandStr, "get null string from test client command");

                TestObject command = TestObjectFactory.createTestObject(new JSONObject(commandStr));

                checkNotNull(command, "can not create test object for command : " + commandStr);

                TestObject testResult = new TestObjectRunner(command).runTest(mContext);

                checkNotNull(testResult, "no test result for command : " + commandStr);

                sendResult(testResult);

                if (testResult instanceof StopTest) {
                    stopTest();
                }

            } catch (Throwable throwable) {
                throwable.printStackTrace();
                RecordResult recordResult = new RecordResult().setInfo(Log.getStackTraceString(throwable));
                recordResult.setFailed(true);
                try {
                    sendResult(recordResult);
                    //wait 3 seconds for client to capture screen and collect log.
                    Thread.sleep(3000);
                } catch (Throwable throwable1) {
                    throwable1.printStackTrace();
                } finally {
                    stopTest();
                }
            }
        }
    }

    public void setUpActivity(IntentData intentData) {
        if (mActivity == null) {
            Intent intent = new IntentWrapper(intentData).buildIntent();
            LogUtil.i(TAG, "setUp intent-->" + intent);
            if (intent != null) {
                mActivity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
                LogUtil.i(TAG, "setUp mAtivity-->" + mActivity);
            }
        }
    }

    private void sendResult(TestObject testObject) throws Throwable {
        LogUtil.d(TAG, "device sendResult command String : " + testObject.getJsonObject().toString());
        mSocketClient.println(testObject.getJsonObject().toString());
    }

    public Object getGlobalField(ObjectData objectData) throws Throwable {
        return ReflectionUtil.getObjectFromData(mActivity, objectData);
    }

    public Object callGlobalMethod(MethodData methodData) throws Throwable {
        return ReflectionUtil.getObjectFromMethodCall(mActivity, methodData);
    }

    public void resolveRuntimeValue(RuntimeValue runtimeValue) throws Throwable {
        if (runtimeValue != null) {
            if (runtimeValue.isFromMethod()) {
                runtimeValue.setValue(callGlobalMethod(runtimeValue.getMethodData()));
            } else {
                runtimeValue.setValue(getGlobalField(runtimeValue.getObjectData()));
            }
        }
    }

    public void stopTest() {
        testRunning = false;
        mSocketClient.close();
    }

}
