package heavy.tool.test.test.wrapper;

import android.content.Context;

import com.ktcp.test.model.data.Action;
import com.ktcp.test.model.data.action.view.KeyEvent;
import com.ktcp.test.model.data.action.view.KeyPress;

import heavy.tool.test.test.model.TestResult;
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
    public boolean runTest(Context context, TestResult result) throws Throwable {

        if (mAction instanceof KeyEvent || mAction instanceof KeyPress) {
            return ViewTestUtil.sendActivityKeyEvent(mAction, result);
        }
        return true;
    }
}
