package heavy.tool.test.test.wrapper;

import android.content.Context;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewInteraction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.ktcp.test.model.data.Action;
import com.ktcp.test.model.data.Assertion;
import com.ktcp.test.model.data.Identifier;
import com.ktcp.test.model.data.action.global.Delay;
import com.ktcp.test.model.data.interf.ITestObject;
import com.ktcp.test.model.data.testable.view.TestableAdapterView;
import com.ktcp.test.model.data.testable.view.TestableRecyclerView;
import com.ktcp.test.model.data.testable.view.TestableView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.test.model.TestResult;
import heavy.tool.test.test.util.DelayUtil;
import heavy.tool.test.test.util.ViewTestUtil;
import heavy.tool.test.util.LogUtil;
import heavy.tool.test.util.ResHelper;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static heavy.tool.test.test.util.ViewTestUtil.createAdapterViewMatcher;
import static heavy.tool.test.test.util.ViewTestUtil.createRecyclerViewAction;
import static heavy.tool.test.test.util.ViewTestUtil.createRecyclerViewAssertion;
import static heavy.tool.test.test.util.ViewTestUtil.createViewAction;
import static heavy.tool.test.test.util.ViewTestUtil.createViewAssertion;
import static heavy.tool.test.test.util.ViewTestUtil.createViewMatcher;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;


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
        if (atom instanceof Action) {

            ViewInteraction viewInteraction = null;

//            if(!identifier.getId().equals("btn_close")){
//                viewInteraction = onView(createViewMatcher(context, identifier));
//            }

            if(viewInteraction == null){
                LogUtil.d(TAG, "view is not int activity, search it in root.");

                //viewInteraction = onView(createViewMatcher(context, identifier)).inRoot(withDecorView())

                Class<?> WindowManagerGlobal = Class.forName("android.view.WindowManagerGlobal");

                LogUtil.d(TAG, "reflect class WindowManagerGlobal success");

                Method getInstance = WindowManagerGlobal.getDeclaredMethod("getInstance");

                LogUtil.d(TAG, "reflect method WindowManagerGlobal  getInstance() success");

                getInstance.setAccessible(true);


                Object WindowManagerGlobalInstance = getInstance.invoke(WindowManagerGlobal);
                LogUtil.d(TAG, "call method WindowManagerGlobal getInstance() success");


                Field mRoots = WindowManagerGlobal.getDeclaredField("mRoots");
                LogUtil.d(TAG, "reflect field mRoots success");

                mRoots.setAccessible(true);

                Object mRootsObject = mRoots.get(WindowManagerGlobalInstance);
                LogUtil.d(TAG, "get field mRoots success");

                ArrayList mRootsList = (ArrayList) mRootsObject;

                LogUtil.d(TAG, "cast mRoots to list success, size : " + mRootsList.size());

                Class<?> ViewRootImpl = Class.forName("android.view.ViewRootImpl");

                LogUtil.d(TAG, "reflect class ViewRootImpl success ");

                Field mViewField = ViewRootImpl.getDeclaredField("mView");

                LogUtil.d(TAG, "reflect field mViewField success ");

                mViewField.setAccessible(true);

                View targetView = null;

                for(Object o : mRootsList){
                    View view = (View) mViewField.get(o);

                    View child = view.findViewById(ResHelper.getIdResIDByName(context, identifier.getId()));

                    if(child != null) {
                        targetView = child;
                        LogUtil.d(TAG, "find view with id name : " + identifier.getId());
                    }

                }

                if(targetView != null){

                    if(uiController == null){

                        Class<?> ViewInteraction = Class.forName("android.support.test.espresso.ViewInteraction");

                        LogUtil.d(TAG, "reflect class ViewInteraction success");

                        Field uiControllerFiled = ViewInteraction.getDeclaredField("uiController");

                        LogUtil.d(TAG, "reflect field uiController success");

                        uiControllerFiled.setAccessible(true);

                        ViewInteraction container = onView(createViewMatcher(context, identifier));
                        LogUtil.d(TAG, "create ViewInteraction instance success");

                        uiController = (UiController) uiControllerFiled.get(container);
                        LogUtil.d(TAG, "get UiController instance success");
                    }

                    if(uiController == null){
                        throw new IllegalArgumentException("can't get instance of ui controller");
                    }

                    final View finalTargetView = targetView;

                    TestEntrance.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createViewAction((Action) atom).perform(uiController, finalTargetView);
                        }
                    });

                    return true;

                }

            }else {

                LogUtil.d(TAG, "view is activity.");
            }

            if (viewInteraction == null) {

                throw new IllegalArgumentException("with identifier :" + identifier.getJsonObject().toString() + " matches no view!");
            }
            testResult.writeActionInfo(TAG, "test view id : " + identifier.getId() + "; perform action : " + atom.getJsonObject().toString());
            viewInteraction.perform(createViewAction((Action) atom));
            return true;
        }
        if (atom instanceof Assertion) {
            ViewInteraction viewInteraction = onView(createViewMatcher(context, identifier));
            if (viewInteraction == null) {
                throw new IllegalArgumentException("with identifier :" + identifier.getJsonObject().toString() + " matches no view!");
            }
            testResult.writeActionInfo(TAG, "test view id : " + identifier.getId() + "; check assertion : " + atom.getJsonObject().toString());
            viewInteraction.check(createViewAssertion((Assertion) atom));
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
