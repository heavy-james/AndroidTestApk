package heavy.tool.test.test.util;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.EspressoKey;
import android.support.test.espresso.action.KeyEventAction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import heavy.test.plugin.model.data.Action;
import heavy.test.plugin.model.data.Assertion;
import heavy.test.plugin.model.data.Identifier;
import heavy.test.plugin.model.data.TestObject;
import heavy.test.plugin.model.data.action.view.Check;
import heavy.test.plugin.model.data.action.view.Click;
import heavy.test.plugin.model.data.action.view.Focus;
import heavy.test.plugin.model.data.action.view.KeyEvent;
import heavy.test.plugin.model.data.action.view.KeyPress;
import heavy.test.plugin.model.data.action.view.SetText;
import heavy.test.plugin.model.data.action.view.TypeText;
import heavy.test.plugin.model.data.assertion.view.Display;
import heavy.test.plugin.model.data.assertion.view.FullScreen;
import heavy.test.plugin.model.data.assertion.view.HasFocus;
import heavy.test.plugin.model.data.assertion.view.WithText;
import heavy.test.plugin.model.data.result.RecordResult;
import heavy.tool.test.test.model.action.TypeTextAction;
import heavy.tool.test.util.LogUtil;
import heavy.tool.test.util.ResHelper;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.isA;


/**
 * Created by heavy on 2017/5/19.
 */

public class ViewTestUtil {

    public static final String TAG = "ViewTestUtil";

    static ArrayList mRoots;

    static Field mViewField;

    public static ViewAction setChecked(final boolean checked) {
        LogUtil.d(TAG, "setChecked : " + checked);
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new Matcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {
                    }

                    @Override
                    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }

    public static ViewAction setFocused(final boolean focused) {
        LogUtil.d(TAG, "setFocused : " + focused);
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new Matcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return item instanceof View;
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {
                    }

                    @Override
                    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (focused) {
                    view.setFocusableInTouchMode(true);
                    if (view.getVisibility() != View.VISIBLE) {
                        throw new RuntimeException("can not focus view : " + view.getResources().getResourceName(view.getId()) + ", witch is not visible");

                    }
                    if (!view.isFocusable()) {
                        throw new RuntimeException("can not focus view : " + view.getResources().getResourceName(view.getId()) + ", witch is not focusable");
                    }
                    view.requestFocus();
                } else {
                    view.clearFocus();
                }
            }
        };
    }

    public static Matcher<View> recyclerChildAtPosition(final int position) {

        LogUtil.d(TAG, "recyclerChildAtPosition : " + position);

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
            }

            @Override
            protected boolean matchesSafely(RecyclerView recyclerView) {
                final RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForPosition(position);
                return viewHolder != null;
            }
        };
    }


    public static Matcher<View> createViewMatcher(Context context, Identifier identifier) {
        if (null != identifier) {
            LogUtil.d(TAG, "createViewMatcher : " + identifier.getId());
            List<Matcher<? super View>> matchers = new ArrayList<>();
            if (!TextUtils.isEmpty(identifier.getId())) {
                matchers.add(withId(ResHelper.getIdResIDByName(context, identifier.getId())));
            }
            if (!TextUtils.isEmpty(identifier.getDescription())) {
                matchers.add(withContentDescription(identifier.getDescription()));
            }
            Identifier parent = identifier.getParentIdentifier();
            if (null != parent) {
                if (!TextUtils.isEmpty(parent.getId())) {
                    matchers.add(withParent(withId(ResHelper.getIdResIDByName(context, parent.getId()))));
                }
                if (!TextUtils.isEmpty(parent.getDescription())) {
                    matchers.add(withParent(withContentDescription(parent.getDescription())));
                }
            }
            return allOf(matchers);
        }
        throw new IllegalArgumentException("identifier should not be null");
    }

    public static Matcher createAdapterViewMatcher(Context context, String className) throws Throwable {
        LogUtil.v(TAG, "createAdapterViewMatcher adapter class name : " + className);
        Class<?> dataClass = context.getClass().getClassLoader().loadClass(className);
        return is(instanceOf(dataClass));
    }

    public static ViewAction createViewAction(Action atom) {
        LogUtil.i(TAG, "createViewAction : " + atom);
        if (atom instanceof Check) {
            return ViewTestUtil.setChecked(Boolean.valueOf(atom.getDescription()));
        }
        if (atom instanceof Click) {
            return click();
        }
        if (atom instanceof Focus) {
            Focus focus = (Focus) atom;
            return ViewTestUtil.setFocused(focus.hasFocus());
        }
        if (atom instanceof KeyEvent) {
            KeyEvent event = (KeyEvent) atom;
            return new KeyEventAction(new EspressoKey.Builder().withKeyCode(event.getKeyCode()).build());

        }
        if (atom instanceof KeyPress) {
            KeyPress press = (KeyPress) atom;
            return pressKey(press.getKeyCode());
        }

        if (atom instanceof SetText) {
            SetText setText = (SetText) atom;
            return replaceText(setText.getContent());
        }

        if (atom instanceof TypeText) {
            TypeText setText = (TypeText) atom;
            return new TypeTextAction(setText.getContent());
        }

        return null;
    }

    public static ViewAssertion createViewAssertion(Assertion assertion) throws Throwable {
        Matcher matcher = createViewMatcher(assertion);
        if (null != matcher) {
            LogUtil.i(TAG, "createViewAssertion assertion : " + assertion + "; data : " + assertion.toString());
            return matches(createViewMatcher(assertion));
        }
        return null;
    }

    public static Matcher<View> createViewMatcher(Assertion assertion) throws Throwable {
        if (assertion instanceof Display) {
            Display display = (Display) assertion;
            if (display.isDisplayed()) {
                return isDisplayed();
            } else {
                return not(isDisplayed());
            }
        }
        if (assertion instanceof WithText) {
            return withText(assertion.getExtra().getValue().toString());
        }

        if (assertion instanceof HasFocus) {
            HasFocus focus = (HasFocus) assertion;
            if (focus.isHasFocus()) {
                return hasFocus();
            } else {
                return not(hasFocus());
            }
        }

        if (assertion instanceof FullScreen) {
            //todo
        }

        throw new IllegalArgumentException("unknown assertion type, data : " + assertion.toString());
    }

    public static ViewAction createRecyclerViewAction(int position, Action action) throws Throwable {
        LogUtil.v(TAG, "createRecyclerViewAction : " + position);
        return RecyclerViewActions.actionOnItemAtPosition(position, createViewAction(action));
    }

    public static ViewAssertion createRecyclerViewAssertion(int position, Assertion assertion) throws Throwable {
        LogUtil.v(TAG, "createRecyclerViewAction : " + position);
        return matches(allOf(ViewTestUtil.recyclerChildAtPosition(position), createViewMatcher(assertion)));
    }

    public static TestObject sendActivityKeyEvent(TestObject atom) {
        int keyCode = -1;
        if (atom instanceof KeyPress) {
            keyCode = ((KeyPress) atom).getKeyCode();
        }
        if (atom instanceof KeyEvent) {
            keyCode = ((KeyEvent) atom).getKeyCode();
        }
        if (keyCode > 0) {
            LogUtil.d(TAG, "sendActivityKeyEvent keyCode : " + keyCode);
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(keyCode);
            return new RecordResult().setInfo("sendActivityKeyEvent keyCode : " + keyCode);
        }
        return null;
    }

    //todo you can not specify the root now, which may contains same view matches your condition but not really the one you want.
    public static View getViewFromDevice(Matcher<View> viewMatcher) throws Throwable {


        LogUtil.d(TAG, "getViewFromDevice matcher : " + viewMatcher.toString());

        if (mRoots == null) {
            Class<?> classWindowManagerGlobal = Class.forName("android.view.WindowManagerGlobal");

            Method methodGetInstance = classWindowManagerGlobal.getDeclaredMethod("getInstance");
            methodGetInstance.setAccessible(true);
            Object windowManagerGlobalInstance = methodGetInstance.invoke(classWindowManagerGlobal);

            Field filedRoots = classWindowManagerGlobal.getDeclaredField("mRoots");
            filedRoots.setAccessible(true);
            Object mRootsObject = filedRoots.get(windowManagerGlobalInstance);

            mRoots = (ArrayList) mRootsObject;
        }

        if (mViewField == null) {
            Class<?> ViewRootImpl = Class.forName("android.view.ViewRootImpl");
            mViewField = ViewRootImpl.getDeclaredField("mView");
            mViewField.setAccessible(true);
        }

        for (Object o : mRoots) {

            View view = (View) mViewField.get(o);

            View result = searchViewRecursively(view, viewMatcher);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static View searchViewRecursively(View root, Matcher<View> viewMatcher) {

        if (root == null) {
            return null;
        }

        if (viewMatcher.matches(root)) {
            return root;
        }

        if (root instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) root;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View result = searchViewRecursively(viewGroup.getChildAt(i), viewMatcher);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
