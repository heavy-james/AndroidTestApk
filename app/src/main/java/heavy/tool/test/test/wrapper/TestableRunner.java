package heavy.tool.test.test.wrapper;

import android.content.Context;

import com.ktcp.test.model.data.Testable;
import com.ktcp.test.model.data.testable.data.TestableData;
import com.ktcp.test.model.data.testable.global.GlobalTestable;
import com.ktcp.test.model.data.testable.view.TestableView;

import heavy.tool.test.test.model.TestResult;


/**
 * Created by heavy on 2017/6/1.
 */

public class TestableRunner implements ITestObjectRunner {

    public static final String TAG = "TestableRunner";
    Testable mTestable;

    public TestableRunner(Testable testable) {
        this.mTestable = testable;
    }

    @Override
    public boolean runTest(Context context, TestResult testResult) throws Throwable {

        boolean result = true;
        if (mTestable instanceof TestableView) {
            return new TestViewRunner((TestableView) mTestable).runTest(context, testResult);
        }

        if (mTestable instanceof GlobalTestable) {
            return new GlobalTestableRunner((GlobalTestable) mTestable).runTest(context, testResult);
        }

        if (mTestable instanceof TestableData) {
            return new TestDataRunner((TestableData) mTestable).runTest(context, testResult);
        }
        throw new IllegalArgumentException("run unknown testable : " + mTestable.getJsonObject().toString());
    }
}
