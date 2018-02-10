package heavy.tool.test.test.wrapper;

import android.content.Context;

import heavy.test.plugin.model.data.Action;
import heavy.test.plugin.model.data.TestObject;
import heavy.test.plugin.model.data.Testable;
import heavy.test.plugin.model.data.result.RecordResult;
import heavy.test.plugin.model.data.testable.global.GetRuntimeValue;
import heavy.test.plugin.model.data.testable.global.SetUpActivity;
import heavy.test.plugin.model.data.testable.global.StopTest;
import heavy.tool.test.activity.TestEntrance;


/**
 * Created by heavy on 2017/6/12.
 */

public class TestObjectRunner implements ITestObjectRunner {

    private static final String TAG = "TestObjectRunner";
    private TestObject mTestObject;

    public TestObjectRunner(TestObject testObject) {
        this.mTestObject = testObject;
    }

    @Override
    public TestObject runTest(Context context) throws Throwable {

        if (mTestObject == null) {
            return null;
        }

        if (mTestObject instanceof SetUpActivity) {
            SetUpActivity setUpActivity = (SetUpActivity) mTestObject;
            TestEntrance.getInstance().setUpActivity(setUpActivity.getIntentData());
            return new RecordResult().setInfo("set up activity : " + setUpActivity.getIntentData().getClassName())
                    .setLevel(RecordResult.LEVEL_PAGE);
        }

        if (mTestObject instanceof GetRuntimeValue) {
            GetRuntimeValue getRuntimeValueCmd = (GetRuntimeValue) mTestObject;
            TestEntrance.getInstance().resolveRuntimeValue(getRuntimeValueCmd.getRuntimeValue());
            return getRuntimeValueCmd;
        }

        if (mTestObject instanceof StopTest) {
            return mTestObject;
        }

        if (mTestObject instanceof Testable) {
            return new TestableRunner((Testable) mTestObject).runTest(context);
        }

        if (mTestObject instanceof Action) {
            return new TestActionRunner((Action) mTestObject).runTest(context);
        }

        throw new IllegalArgumentException("unknown test object type : " + mTestObject);
    }
}
