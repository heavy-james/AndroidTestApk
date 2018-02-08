package heavy.tool.test.test.model;

/**
 * Created by heavy on 2017/6/1.
 */

public class AbortTestException extends Exception {

    String message;

    public AbortTestException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
