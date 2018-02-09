package heavy.tool.test.test.wrapper;

import android.content.Context;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import java.lang.reflect.Field;

import heavy.test.plugin.model.data.Action;
import heavy.test.plugin.model.data.Assertion;
import heavy.test.plugin.model.data.Identifier;
import heavy.test.plugin.model.data.action.global.Delay;
import heavy.test.plugin.model.data.interf.ITestObject;
import heavy.test.plugin.model.data.testable.view.TestableAdapterView;
import heavy.test.plugin.model.data.testable.view.TestableRecyclerView;
import heavy.test.plugin.model.data.testable.view.TestableView;
import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.test.model.TestResult;
import heavy.tool.test.test.util.DelayUtil;
import heavy.tool.test.test.util.ViewTestUtil;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static heavy.tool.test.test.util.ViewTestUtil.createAdapterViewMatcher;
import static heavy.tool.test.test.util.ViewTestUtil.createRecyclerViewAction;
import static heavy.tool.test.test.util.ViewTestUtil.createRecyclerViewAssertion;
import static heavy.tool.test.test.util.ViewTestUtil.createViewAction;
import static heavy.tool.test.test.util.ViewTestUtil.createViewAssertion;
import static heavy.tool.test.test.util.ViewTestUtil.createViewMatcher;
import static heavy.tool.test.test.util.ViewTestUtil.getViewFromDevice;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by heavy on 2017/5/20.
 */

public class TestViewRunner implements ITestObjectRunner {

    public static final String TAG = "TestViewRunner";

    TestableView mTestableView;

    static UiController uiController;

    public TestViewRunner(TestableView testableView) {
        mTestableView = testableView;
    }

    @Override
    public boolean runTest(Context context, TestResult testResult) throws Throwable {

        boolean result = true;
        //the order of the instanceof check must be like what shows below, corresponding to the extension of ViewData classes
        if (mTestableView instanceof TestableRecyclerView) {
            TestableRecyclerView parentData = (TestableRecyclerView) mTestableView;
            testResult.writeTestableInfo(TAG, "execute TestableRecyclerView with id : " + parentData.getIdentifier().getId());
            for (TestableView childData : parentData.getChildView()) {
                for (ITestObject action : childData.getContentObjects()) {
                    result &= testRecyclerView(context, parentData.getIdentifier(), childData.getPosition(), action, testResult);
                }
            }
        } else if (mTestableView instanceof TestableAdapterView) {
            TestableAdapterView parentData = (TestableAdapterView) mTestableView;
            testResult.writeTestableInfo(TAG, "execute TestableAdapterView with id : " + parentData.getIdentifier().getId());
            for (TestableView childData : parentData.getChildView()) {
                for (ITestObject atom : childData.getContentObjects()) {
                    result &= testAdapterView(context, parentData.getClassName(), childData.getPosition(), atom, testResult);
                }
            }
        }
        //test all common view action and assertions in view data
        testResult.writeTestableInfo(TAG, "execute TestableView with id : " + mTestableView.getIdentifier().getId());
        for (ITestObject atom : mTestableView.getContentObjects()) {
            result &= testView(context, mTestableView.getIdentifier(), atom, testResult);
        }

        return result;
    }

    private boolean testView(Context context, Identifier identifier, final ITestObject atom, TestResult testResult) throws Throwable {
        if (sendDelayAction(atom, testResult) || ViewTestUtil.sendActivityKeyEvent(atom, testResult)) {
            return true;
        }

        final View targetView = getViewFromDevice(createViewMatcher(context, identifier));

        checkNotNull(targetView, "find no view with identifier : "  + identifier.getJsonObject().toString());

        if (atom instanceof Action) {

            if (uiController == null) {

                Class<?> classViewInteraction = Class.forName("android.support.test.espresso.ViewInteraction");

                checkNotNull(classViewInteraction, "can not reflect android.support.test.espresso.ViewInteraction");

                Field uiControllerFiled = classViewInteraction.getDeclaredField("uiController");

                checkNotNull(classViewInteraction, "can not reflect field android.support.test.espresso.ViewInteraction.uiController");

                uiControllerFiled.setAccessible(true);

                ViewInteraction container = onView(createViewMatcher(context, identifier));

                checkNotNull(container, "can not create instance of android.support.test.espresso.ViewInteraction");

                uiController = (UiController) uiControllerFiled.get(container);

                checkNotNull(uiController, "can't get instance of ui controller");

            }


            ViewAction viewAction = createViewAction((Action) atom);

            checkNotNull(viewAction, "un supported view action : " + atom.getJsonObject().toString());

            TestEntrance.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createViewAction((Action) atom).perform(uiController, targetView);
                }
            });

            return true;

        } else if (atom instanceof Assertion) {

            ViewAssertion assertion = createViewAssertion((Assertion) atom);

            checkNotNull(assertion, "unsupported assertion : "+ atom.getJsonObject().toString());

            assertion.check(targetView, null);

            testResult.writeActionInfo(TAG, "test view id : " + identifier.getId() + "; check assertion : " + atom.getJsonObject().toString());

            return true;
        }

        throw new IllegalArgumentException("testView id : " + identifier.getId() + " with unknown atom !");
    }

    private boolean testAdapterView(Context context, String adapterClass, int position, ITestObject atom, TestResult testResult) throws Throwable {
        if (sendDelayAction(atom, testResult) || ViewTestUtil.sendActivityKeyEvent(atom, testResult)) {
            return true;
        }
        if (atom instanceof Action) {
            Action action = (Action) atom;
            testResult.writeActionInfo(TAG, "test adapterView with class : " + adapterClass +
                    "at child position : " + position + "; perform action : " + action.getJsonObject().toString());
            onData(createAdapterViewMatcher(context, adapterClass)).atPosition(position).perform(createViewAction(action));
            return true;
        } else if (atom instanceof Assertion) {
            Assertion assertion = (Assertion) atom;
            testResult.writeActionInfo(TAG, "test adapterView with class : " + adapterClass +
                    "at child position : " + position + "; check assertion : " + assertion.getJsonObject().toString());
            onData(createAdapterViewMatcher(context, adapterClass)).atPosition(position).check(createViewAssertion(assertion));
            return true;
        }
        throw new IllegalArgumentException("testAdapterView class : " + adapterClass + " with unkown atom !");
    }

    private boolean testRecyclerView(Context context, Identifier parentIdentifier, int position, ITestObject atom, TestResult testResult) throws Throwable {
        if (sendDelayAction(atom, testResult) || ViewTestUtil.sendActivityKeyEvent(atom, testResult)) {
            return true;
        }
        if (atom instanceof Action) {
            Action action = (Action) atom;
            testResult.writeActionInfo(TAG, "test recyclerView with id : " + parentIdentifier.getId() +
                    "; at child position : " + position + "; perform action : " + atom.getJsonObject().toString());
            onView(createViewMatcher(context, parentIdentifier)).perform(createRecyclerViewAction(position, action));
            return true;
        }
        if (atom instanceof Assertion) {
            Assertion assertion = (Assertion) atom;
            testResult.writeActionInfo(TAG, "test recyclerView with id : " + parentIdentifier.getId() +
                    "; at child position : " + position + "; check assertion : " + atom.getJsonObject().toString());
            onView(createViewMatcher(context, parentIdentifier)).check(createRecyclerViewAssertion(position, assertion));
            return true;
        }
        throw new IllegalArgumentException("testRecyclerView id : " + parentIdentifier.getId() + " with unkown atom !");
    }

    private boolean sendDelayAction(ITestObject atom, TestResult testResult) {
        if (atom instanceof Delay) {
            Delay delay = (Delay) atom;
            testResult.writeTestableInfo(TAG, "sendDelayAction time millis : " + delay.getDelayMillis());
            DelayUtil.delay(delay.getDelayMillis());
            return true;
        }
        return false;
    }
}
