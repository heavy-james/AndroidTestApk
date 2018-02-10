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
import heavy.test.plugin.model.data.TestObject;
import heavy.test.plugin.model.data.action.global.Delay;
import heavy.test.plugin.model.data.result.RecordResult;
import heavy.test.plugin.model.data.testable.view.TestableAdapterView;
import heavy.test.plugin.model.data.testable.view.TestableRecyclerView;
import heavy.test.plugin.model.data.testable.view.TestableView;
import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.test.util.DelayUtil;
import heavy.tool.test.test.util.ViewTestUtil;
import heavy.tool.test.util.LogUtil;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static com.google.common.base.Preconditions.checkNotNull;
import static heavy.tool.test.test.util.ViewTestUtil.createAdapterViewMatcher;
import static heavy.tool.test.test.util.ViewTestUtil.createRecyclerViewAction;
import static heavy.tool.test.test.util.ViewTestUtil.createRecyclerViewAssertion;
import static heavy.tool.test.test.util.ViewTestUtil.createViewAction;
import static heavy.tool.test.test.util.ViewTestUtil.createViewAssertion;
import static heavy.tool.test.test.util.ViewTestUtil.createViewMatcher;
import static heavy.tool.test.test.util.ViewTestUtil.getViewFromDevice;


/**
 * Created by heavy on 2017/5/20.
 */

public class TestViewRunner implements ITestObjectRunner {

    public static final String TAG = "TestViewRunner";
    static UiController uiController;
    TestableView mTestableView;

    public TestViewRunner(TestableView testableView) {
        mTestableView = testableView;
    }

    @Override
    public TestObject runTest(Context context) throws Throwable {

        TestObject result = null;
        //the order of the instanceof check must be like what shows below, corresponding to the extension of ViewData classes
        if (mTestableView instanceof TestableRecyclerView) {
            TestableRecyclerView parentData = (TestableRecyclerView) mTestableView;
            LogUtil.d(TAG, "execute TestableRecyclerView with id : " + parentData.getIdentifier().getId());
            for (TestableView childData : parentData.getChildView()) {
                for (TestObject action : childData.getContentObjects()) {
                    result = testRecyclerView(context, parentData.getIdentifier(), childData.getPosition(), action);
                }
            }
        } else if (mTestableView instanceof TestableAdapterView) {
            TestableAdapterView parentData = (TestableAdapterView) mTestableView;
            LogUtil.d(TAG, "execute TestableAdapterView with id : " + parentData.getIdentifier().getId());
            for (TestableView childData : parentData.getChildView()) {
                for (TestObject atom : childData.getContentObjects()) {
                    result = testAdapterView(context, parentData.getClassName(), childData.getPosition(), atom);
                }
            }
        }
        //test all common view action and assertions in view data
        LogUtil.d(TAG, "execute TestableView with id : " + mTestableView.getIdentifier().getId());
        for (TestObject atom : mTestableView.getContentObjects()) {
            result = testView(context, mTestableView.getIdentifier(), atom);
        }

        return result;
    }

    private TestObject testView(Context context, Identifier identifier, final TestObject atom) throws Throwable {

        LogUtil.d(TAG, "testView id : " + identifier.getId() + "; atom : " + atom);

        TestObject testObject = sendDelayAction(atom);

        if (testObject != null) {
            return testObject;
        }

        testObject = ViewTestUtil.sendActivityKeyEvent(atom);

        if (testObject != null) {
            return testObject;
        }

        final View targetView = getViewFromDevice(createViewMatcher(context, identifier));

        checkNotNull(targetView, "find no view with identifier : " + identifier.toString());

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

            checkNotNull(viewAction, "un supported view action : " + atom.toString());

            TestEntrance.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createViewAction((Action) atom).perform(uiController, targetView);
                }
            });

            String message = "test view id : " + identifier.getId() + "; perform action : " + atom.toString();

            LogUtil.d(TAG, message);

            return new RecordResult().setInfo(message);

        } else if (atom instanceof Assertion) {

            ViewAssertion assertion = createViewAssertion((Assertion) atom);

            checkNotNull(assertion, "unsupported assertion : " + atom.toString());

            assertion.check(targetView, null);

            String message = "test view id : " + identifier.getId() + "; check assertion : " + atom.toString();

            LogUtil.d(TAG, message);
            return new RecordResult().setInfo(message);
        }

        throw new IllegalArgumentException("testView id : " + identifier.getId() + " with unknown atom !");
    }

    private TestObject testAdapterView(Context context, String adapterClass, int position, TestObject atom) throws Throwable {

        TestObject testObject = sendDelayAction(atom);

        if (testObject != null) {
            return testObject;
        }

        testObject = ViewTestUtil.sendActivityKeyEvent(atom);

        if (testObject != null) {
            return testObject;
        }

        if (atom instanceof Action) {
            Action action = (Action) atom;
            String message = "test adapterView with class : " + adapterClass +
                    "at child position : " + position + "; perform action : " + action.toString();
            LogUtil.d(TAG, message);
            onData(createAdapterViewMatcher(context, adapterClass)).atPosition(position).perform(createViewAction(action));
            return new RecordResult().setInfo(message);
        } else if (atom instanceof Assertion) {
            Assertion assertion = (Assertion) atom;
            String message = "test adapterView with class : " + adapterClass +
                    "at child position : " + position + "; check assertion : " + assertion.toString();
            onData(createAdapterViewMatcher(context, adapterClass)).atPosition(position).check(createViewAssertion(assertion));
            return new RecordResult().setInfo(message);
        }
        throw new IllegalArgumentException("testAdapterView class : " + adapterClass + " with unkown atom !");
    }

    private TestObject testRecyclerView(Context context, Identifier parentIdentifier, int position, TestObject atom) throws Throwable {

        TestObject testObject = sendDelayAction(atom);

        if (testObject != null) {
            return testObject;
        }

        testObject = ViewTestUtil.sendActivityKeyEvent(atom);

        if (testObject != null) {
            return testObject;
        }

        if (atom instanceof Action) {
            Action action = (Action) atom;
            String message = "test recyclerView with id : " + parentIdentifier.getId() +
                    "; at child position : " + position + "; perform action : " + atom.toString();
            LogUtil.d(TAG, message);
            onView(createViewMatcher(context, parentIdentifier)).perform(createRecyclerViewAction(position, action));
            return new RecordResult().setInfo(message);
        }
        if (atom instanceof Assertion) {
            Assertion assertion = (Assertion) atom;
            String message = "test recyclerView with id : " + parentIdentifier.getId() +
                    "; at child position : " + position + "; check assertion : " + atom.toString();
            LogUtil.d(TAG, message);
            onView(createViewMatcher(context, parentIdentifier)).check(createRecyclerViewAssertion(position, assertion));
            return new RecordResult().setInfo(message);
        }
        throw new IllegalArgumentException("testRecyclerView id : " + parentIdentifier.getId() + " with unkown atom !");
    }

    private TestObject sendDelayAction(TestObject atom) {
        if (atom instanceof Delay) {
            Delay delay = (Delay) atom;
            LogUtil.d(TAG, "sendDelayAction time millis : " + delay.getDelayMillis());
            DelayUtil.delay(delay.getDelayMillis());
            return new RecordResult().setInfo("sendDelayAction time millis : " + delay.getDelayMillis());
        }
        return null;
    }
}
