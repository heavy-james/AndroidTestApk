package heavy.tool.test.test.wrapper;

import android.content.Context;

import heavy.test.plugin.model.data.Action;
import heavy.test.plugin.model.data.TestObject;
import heavy.tool.test.test.util.ViewTestUtil;


/**
 * Created by heavy on 17/6/29.
 */

public class TestActionRunner implements ITestObjectRunner {

    private Action mAction;

    public TestActionRunner(Action action) {
        mAction = action;
    }

    @Override
    public TestObject runTest(Context context) throws Throwable {


        TestObject testObject = ViewTestUtil.sendActivityKeyEvent(mAction);

        if (testObject != null) {
            return testObject;
        }

        return null;
    }
}
