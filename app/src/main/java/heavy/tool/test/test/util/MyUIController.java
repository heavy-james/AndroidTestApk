package heavy.tool.test.test.util;

import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by heavy on 2018/2/8.
 */

public class MyUIController implements UiController {

    @Override
    public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
        return false;
    }

    @Override
    public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
        return false;
    }

    @Override
    public boolean injectString(String str) throws InjectEventSecurityException {
        return false;
    }

    @Override
    public void loopMainThreadUntilIdle() {

    }

    @Override
    public void loopMainThreadForAtLeast(long millisDelay) {

    }
}
