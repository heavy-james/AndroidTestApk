package heavy.tool.test.test.model.action;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import heavy.tool.test.activity.TestEntrance;
import heavy.tool.test.util.SystemUtil;

import static android.support.test.espresso.action.ViewActions.typeText;

/**
 * Created by heavy on 2018/2/25.
 */

public class TypeTextAction implements ViewAction {

    private static final String TAG = "TypeTextAction";

    ViewAction mAction;

    public TypeTextAction(String content) {
        mAction = typeText(content);
    }


    @Override
    public Matcher<View> getConstraints() {
        return mAction.getConstraints();
    }

    @Override
    public String getDescription() {
        return mAction.getDescription();
    }

    @Override
    public void perform(UiController uiController, View view) {
        mAction.perform(uiController, view);
        if (SystemUtil.isKeyboardVisible(TestEntrance.getInstance().getActivity())) {
            SystemUtil.hideKeyBoard(TestEntrance.getInstance().getActivity(), view.getWindowToken());
        }
    }
}
