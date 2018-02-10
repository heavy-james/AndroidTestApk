package heavy.tool.test.test.wrapper;

import android.content.Context;

import heavy.test.plugin.model.data.TestObject;


/**
 * Created by heavy on 2017/5/22.
 */

public interface ITestObjectRunner {

    public TestObject runTest(Context context) throws Throwable;
}
