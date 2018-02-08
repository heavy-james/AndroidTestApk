package heavy.tool.test.test.wrapper;

import android.content.Context;

import com.ktcp.test.model.data.Action;
import com.ktcp.test.model.data.Testable;
import com.ktcp.test.model.data.interf.ITestObject;

import heavy.tool.test.test.model.TestResult;


/**
 * Created by heavy on 2017/6/12.
 */

public class TestObjectRunner implements ITestObjectRunner {

    private static final String TAG = "TestObjectRunner";
    private ITestObject mTestObject;

    public TestObjectRunner(ITestObject testObject) {
        this.mTestObject = testObject;
    }

    @Override
    public boolean runTest(Context context, TestResult testResult) throws Throwable {

        if (mTestObject == null) {
            return true;
        }

        if (mTestObject instanceof Testable) {
            return new TestableRunner((Testable) mTestObject).runTest(context, testResult);
        }

        if (mTestObject instanceof Action) {
            return new TestActionRunner((Action) mTestObject).runTest(context, testResult);
        }

        throw new IllegalArgumentException("unknown test object type : " + mTestObject);
    }
}
