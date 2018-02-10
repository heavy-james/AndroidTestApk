package heavy.tool.test.test.wrapper;

import android.content.Context;

import heavy.test.plugin.model.data.TestObject;
import heavy.test.plugin.model.data.Testable;
import heavy.test.plugin.model.data.testable.data.TestableData;
import heavy.test.plugin.model.data.testable.global.GlobalTestable;
import heavy.test.plugin.model.data.testable.view.TestableView;


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
    public TestObject runTest(Context context) throws Throwable {

        boolean result = true;
        if (mTestable instanceof TestableView) {
            return new TestViewRunner((TestableView) mTestable).runTest(context);
        }

        if (mTestable instanceof GlobalTestable) {
            return new GlobalTestableRunner((GlobalTestable) mTestable).runTest(context);
        }

        if (mTestable instanceof TestableData) {
            return new TestDataRunner((TestableData) mTestable).runTest(context);
        }
        throw new IllegalArgumentException("run unknown testable : " + mTestable.toString());
    }
}
