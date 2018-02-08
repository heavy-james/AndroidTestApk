package heavy.tool.test.test.util;

import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by heavy on 2017/5/31.
 */

public class MatcherUtil {

    public static Matcher<View> hasFocus(final boolean hasFocus) {
        return new HasFocus(hasFocus);
    }

    public static class HasFocus extends TypeSafeMatcher<View> {

        private boolean hasFocus;

        public HasFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }

        @Override
        protected boolean matchesSafely(View item) {
            return hasFocus == item.hasFocus();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("focus state not matched, required : " + hasFocus);
        }
    }
}
