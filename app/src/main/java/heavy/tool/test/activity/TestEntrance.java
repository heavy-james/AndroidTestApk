package heavy.tool.test.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.elvishew.xlog.LogLevel;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import heavy.test.plugin.logic.TestCommand;
import heavy.test.plugin.logic.TestCommandFactory;
import heavy.test.plugin.logic.command.GetRuntimeValue;
import heavy.test.plugin.logic.command.RecordResult;
import heavy.test.plugin.logic.command.RunTestObject;
import heavy.test.plugin.logic.command.SetUpActivity;
import heavy.test.plugin.logic.command.StopTest;
import heavy.test.plugin.logic.transport.SocketClient;
import heavy.test.plugin.model.data.IntentData;
import heavy.test.plugin.model.data.reflection.MethodData;
import heavy.test.plugin.model.data.reflection.ObjectData;
import heavy.test.plugin.model.data.reflection.RuntimeValue;
import heavy.test.plugin.util.LogUtil;
import heavy.test.plugin.util.TextUtil;
import heavy.tool.test.test.model.TestResult;
import heavy.tool.test.test.util.ReflectionUtil;
import heavy.tool.test.test.wrapper.IntentWrapper;
import heavy.tool.test.test.wrapper.TestObjectRunner;


@RunWith(AndroidJUnit4.class)
public class TestEntrance extends ActivityTestRule<Activity> {

    public static final String TAG = "TestEntrance";
    static TestResult mTestResult;
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
        LogUtil.init(LogLevel.ALL, true, "/sdcard/xlog/heavy.tool.test/test_result.txt");
        LogUtil.i(TAG, "staticSetUp");
        mContext = InstrumentationRegistry.getTargetContext();
        mTestResult = new TestResult();
    }

    @AfterClass
    public static void staticTearDown() {

    }

    @Test
    public void runTests() throws Throwable {
        LogUtil.d(TAG, "runTests start test server...");
        TestCommand command = null;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable throwable) {
                LogUtil.d(TAG, "runTests occurs unexpected exception, do aborting...");
                LogUtil.e(TAG, throwable.getMessage());
                final TestCommand responseCommand = new RecordResult().setInfo("execute command failed, cause : " + throwable.getMessage())
                        .setFailed(true).setLevel(RecordResult.LEVEL_DETAIL).setRunAsCondition(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendForResult(responseCommand);
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

        while (testRunning) {
            try {
                if (mSocketClient == null) {
                    ServerSocket serverSocket = new ServerSocket(10001, 10);
                    LogUtil.d(TAG, "runTests start create server socket success");
                    Socket socket = serverSocket.accept();
                    LogUtil.d(TAG, "runTests start accept socket : " + socket);
                    mSocketClient = new SocketClient(socket);
                }
                LogUtil.d(TAG, "runTests start read socket input with client");
                String commandStr = mSocketClient.readLine();
                LogUtil.d(TAG, "get string : " + commandStr);
                if (!TextUtil.isEmpty(commandStr)) {
                    command = TestCommandFactory.createCommand(new JSONObject(commandStr));
                    execute(command);
                }
                testRunning = false;
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private void setUpActivity(IntentData intentData) {
        if (mActivity == null) {
            Intent intent = new IntentWrapper(intentData).buildIntent();
            LogUtil.i(TAG, "setUp intent-->" + intent);
            if (intent != null) {
                mActivity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
                LogUtil.i(TAG, "setUp mAtivity-->" + mActivity);
            }
        }
    }

    private void sendForResult(TestCommand testCommand) throws Throwable {
        if (testCommand != null) {
            mTestResult.writeResult(TAG, testCommand.getJsonObject().toString());
            mSocketClient.println(testCommand.getJsonObject().toString());
            LogUtil.d(TAG, "device sendForResult command String : " + testCommand.getJsonObject().toString());
            String resultString = mSocketClient.readLine();
            LogUtil.d(TAG, "device sendForResult getResponse String : " + resultString);
            if (!TextUtil.isEmpty(resultString)) {
                TestCommand resultCommand = TestCommandFactory.createCommand(new JSONObject(resultString));
                LogUtil.d(TAG, "device sendForResult getResponse to Command : " + resultCommand.getJsonObject().toString());
                execute(resultCommand);
            }
        }
    }

    private void execute(TestCommand testCommand) throws Throwable {
        if (testCommand == null) {
            return;
        }
        TestCommand responseCommand = null;
        do {
            try {
                if (testCommand instanceof SetUpActivity) {
                    SetUpActivity setUpActivity = (SetUpActivity) testCommand;
                    setUpActivity(setUpActivity.getIntentData());
                    responseCommand = new RecordResult().setInfo("set up activity : " + setUpActivity.getIntentData().getClassName())
                            .setLevel(RecordResult.LEVEL_PAGE);
                    break;
                }

                if (testCommand instanceof RunTestObject) {
                    RunTestObject runTestObject = (RunTestObject) testCommand;
                    new TestObjectRunner(runTestObject.getTestObject()).runTest(mContext, mTestResult);
                    responseCommand = new RecordResult().setInfo("execute object : " + runTestObject.getJsonObject().toString())
                            .setLevel(RecordResult.LEVEL_TESTABLE);
                    break;
                }

                if (testCommand instanceof GetRuntimeValue) {
                    GetRuntimeValue getRuntimeValueCmd = (GetRuntimeValue) testCommand;
                    resolveRuntimeValue(getRuntimeValueCmd.getRuntimeValue());
                    responseCommand = getRuntimeValueCmd;
                    break;
                }

                if (testCommand instanceof StopTest) {
                    mSocketClient.close();
                    testRunning = false;
                    responseCommand = new RecordResult().setInfo("stopped test by test client .").setLevel(RecordResult.LEVEL_TESTABLE);
                    break;
                }
            } catch (Throwable throwable) {
                responseCommand = new RecordResult().setInfo("execute command failed, cause : " + throwable.getMessage())
                        .setFailed(true).setLevel(RecordResult.LEVEL_DETAIL).setRunAsCondition(testCommand.isRunAsCondition());
                if (!testCommand.isRunAsCondition()) {
                    sendForResult(responseCommand);
                    mSocketClient.close();
                    throw throwable;
                }
            }
        } while (false);
        if (responseCommand != null) {
            responseCommand.setRunAsCondition(testCommand.isRunAsCondition());
        }
        sendForResult(responseCommand);
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

}
