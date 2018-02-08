package heavy.tool.test.test.wrapper;


import android.content.Context;

import heavy.test.plugin.model.data.Assertion;
import heavy.test.plugin.model.data.Atom;
import heavy.test.plugin.model.data.assertion.data.Equals;
import heavy.test.plugin.model.data.assertion.data.NullCheck;
import heavy.test.plugin.model.data.testable.data.TestableData;
import heavy.test.plugin.model.data.testable.data.TestableField;
import heavy.test.plugin.model.data.testable.data.TestableMethod;
import heavy.test.plugin.util.LogUtil;
import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.test.model.TestResult;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;


import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by heavy on 2017/6/2.
 */

public class TestDataRunner implements ITestObjectRunner {

    public static final String TAG = "TestDataRunner";

    TestableData mTestableData;

    public TestDataRunner(TestableData mTestableData) {
        this.mTestableData = mTestableData;
    }


    @Override
    public boolean runTest(Context context, TestResult testResult) throws Throwable {
        if (mTestableData instanceof TestableField) {
            TestableField testableField = (TestableField) mTestableData;
            Object result = TestEntrance.getInstance().getGlobalField(testableField.getFieldData());
            for (Atom atom : testableField.getFieldAtoms()) {
                if (atom instanceof Assertion) {
                    Assertion assertion = (Assertion) atom;
                    assertThat(result, createDataMatcher(assertion));
                }
            }
            return true;
        }

        if (mTestableData instanceof TestableMethod) {
            TestableMethod testableMethod = (TestableMethod) mTestableData;
            Object result = TestEntrance.getInstance().callGlobalMethod(testableMethod.getMethodData());
            for (Atom atom : testableMethod.getMethodAtoms()) {
                if (atom instanceof Assertion) {
                    Assertion assertion = (Assertion) atom;
                    assertThat(result, createDataMatcher(assertion));
                }
            }
            return true;
        }
        testResult.writeTestableInfo(TAG, "warning ! finished data test with not testable instance");
        throw new IllegalArgumentException("unknown testable data : " + mTestableData);
    }

    private Matcher<Object> createDataMatcher(Assertion assertion) throws Throwable {
        Object matcherData = null;
        matcherData = assertion.getExtra().getValue();
        LogUtil.i(TAG, "createDataMatcher assertion : " + assertion.getJsonObject().toString() + "; matcher data : " + matcherData);
        if (assertion instanceof Equals) {
            Equals equals = (Equals) assertion;
            if (matcherData == null) {
                matcherData = equals.getContent();
            }
            Matcher<Object> result = new IsEqual<Object>(matcherData);
            if (equals.isConversed()) {
                result = new IsNot<Object>(result);
            }
            return result;
        }

        if (assertion instanceof NullCheck) {
            NullCheck nullCheck = (NullCheck) assertion;
            if (nullCheck.isNull()) {
                return new IsNull<Object>();
            } else {
                return new IsNot<Object>(new IsNull<Object>());
            }
        }
        throw new IllegalArgumentException("createDataMatcher with unknown assertion : " + assertion.getJsonObject().toString());
    }
}
