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
import android.widget.Checkable;

import com.ktcp.test.model.data.Action;
import com.ktcp.test.model.data.Assertion;
import com.ktcp.test.model.data.Identifier;
import com.ktcp.test.model.data.action.view.Check;
import com.ktcp.test.model.data.action.view.Click;
import com.ktcp.test.model.data.action.view.Focus;
import com.ktcp.test.model.data.action.view.KeyEvent;
import com.ktcp.test.model.data.action.view.KeyPress;
import com.ktcp.test.model.data.assertion.view.Display;
import com.ktcp.test.model.data.assertion.view.FullScreen;
import com.ktcp.test.model.data.assertion.view.HasFocus;
import com.ktcp.test.model.data.assertion.view.WithText;
import com.ktcp.test.model.data.interf.ITestObject;
import com.ktcp.test.util.LogUtil;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import heavy.tool.test.test.model.TestResult;
import heavy.tool.test.util.ResHelper;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
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
        LogUtil.i(TAG, "createViewAction : " + atom.getJsonObject().toString());
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
        return null;
    }

    public static ViewAssertion createViewAssertion(Assertion assertion) throws Throwable {
        Matcher matcher = createViewMatcher(assertion);
        if (null != matcher) {
            LogUtil.i(TAG, "createViewAssertion assertion : " + assertion + "; data : " + assertion.getJsonObject().toString());
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

        throw new IllegalArgumentException("unknown assertion type, data : " + assertion.getJsonObject().toString());
    }

    public static ViewAction createRecyclerViewAction(int position, Action action) throws Throwable {
        LogUtil.v(TAG, "createRecyclerViewAction : " + position);
        return RecyclerViewActions.actionOnItemAtPosition(position, createViewAction(action));
    }

    public static ViewAssertion createRecyclerViewAssertion(int position, Assertion assertion) throws Throwable {
        LogUtil.v(TAG, "createRecyclerViewAction : " + position);
        return matches(allOf(ViewTestUtil.recyclerChildAtPosition(position), createViewMatcher(assertion)));
    }

    public static boolean sendActivityKeyEvent(ITestObject atom, TestResult testResult) {
        int keyCode = -1;
        if (atom instanceof KeyPress) {
            keyCode = ((KeyPress) atom).getKeyCode();
        }
        if (atom instanceof KeyEvent) {
            keyCode = ((KeyEvent) atom).getKeyCode();
        }
        if (keyCode > 0) {
            testResult.writeTestableInfo(TAG, "sendActivityKeyEvent keyCode : " + keyCode);
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(keyCode);
            return true;
        }
        return false;
    }
}
