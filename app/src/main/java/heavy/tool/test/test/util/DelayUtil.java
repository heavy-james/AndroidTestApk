package heavy.tool.test.test.util;

/**
 * Created by v_hfzhan on 2017/5/16.
 */

public class DelayUtil {

    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
