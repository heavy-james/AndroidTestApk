package heavy.tool.test.test.wrapper;

import android.content.Intent;
import android.text.TextUtils;

import com.ktcp.test.model.data.Extra;
import com.ktcp.test.model.data.IntentData;

/**
 * Created by heavy on 2017/5/20.
 */

public class IntentWrapper {

    private static final String TAG = "IntentWrapper";
    IntentData mIntentData;

    public IntentWrapper(IntentData intentData) {
        mIntentData = intentData;
    }

    public Intent buildIntent() {
        if (null != mIntentData && mIntentData.getJsonObject().length() > 0) {
            Intent intent = new Intent();
            if (!TextUtils.isEmpty(mIntentData.getAction())) {
                intent.setAction(mIntentData.getAction());
            }
            if (!TextUtils.isEmpty(mIntentData.getClassName()) && !TextUtils.isEmpty(mIntentData.getPackageName())) {
                intent.setClassName(mIntentData.getPackageName(), mIntentData.getClassName());
            }
            if (!TextUtils.isEmpty(mIntentData.getCategory())) {
                intent.addCategory(mIntentData.getCategory());
            }
            if (mIntentData.getFlag() > 0) {
                intent.addFlags(mIntentData.getFlag());
            }
            for (Extra extra : mIntentData.getExtras()) {
                if (extra.getType().equals("string")) {
                    intent.putExtra(extra.getKey(), extra.getString());
                    continue;
                }
                if (extra.getType().equals("int")) {
                    intent.putExtra(extra.getKey(), extra.getInt());
                    continue;
                }
                if (extra.getType().equals("boolean")) {
                    intent.putExtra(extra.getKey(), extra.getBoolean());
                    continue;
                }
                if (extra.getType().equals("float")) {
                    intent.putExtra(extra.getKey(), extra.getFloat());
                    continue;
                }
                if (extra.getType().equals("double")) {
                    intent.putExtra(extra.getKey(), extra.getDouble());
                    continue;
                }
            }
            return intent;
        }
        return null;
    }

}
