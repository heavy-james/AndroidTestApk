package heavy.tool.test.test.wrapper;

import android.content.Context;

import heavy.tool.test.test.model.TestResult;


/**
 * Created by heavy on 2017/5/22.
 */

public interface ITestObjectRunner {

    public boolean runTest(Context context, TestResult result) throws Throwable;
}
