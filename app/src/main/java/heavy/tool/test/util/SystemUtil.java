package heavy.tool.test.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Method;

/**
 * Created by feng on 2017/11/14.
 */

public class SystemUtil {

    private static String sSystemVersion = "";
    private static String sRomVersion = "";
    private static String sHardwareVersion = "";
    private static String sBuildDate = "";

    /**
     * 获取app版本号名
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        String version = "";
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取app版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        int version = 0;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取当前手机Android版本号
     *
     * @return
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前设备系统版本号
     *
     * @return
     */
    public static String getSystemVersion() {
        if (TextUtils.isEmpty(sSystemVersion)) {
            sSystemVersion = getSystemProperty("ro.topband.sw.version", "unknown");
        }
        return sSystemVersion;
    }

    /**
     * 获取当前设备系统版本号
     *
     * @return
     */
    public static String getRomVersion() {
        if (TextUtils.isEmpty(sRomVersion)) {
            sRomVersion = getSystemProperty("ro.topband.rom.version", "unknown");
        }
        return sRomVersion;
    }

    /**
     * 获取当前设备硬件版本号
     *
     * @return
     */
    public static String getHardwareVersion() {
        if (TextUtils.isEmpty(sHardwareVersion)) {
            sHardwareVersion = getSystemProperty("ro.topband.hw.version", "unknown");
        }
        return sHardwareVersion;
    }

    /**
     * 获取当前设备构建时间
     *
     * @return
     */
    public static String getBuildDate() {
        if (TextUtils.isEmpty(sBuildDate)) {
            sBuildDate = getSystemProperty("ro.build.date", "unknown");
        }
        return sBuildDate;
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取mac地址
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * 获取系统属性值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    /**
     * 设置系统属性值
     *
     * @param key
     * @param value
     */
    public static void setSystemProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determine if keyboard is visible
     *
     * @param activity Activity
     * @return Whether keyboard is visible or not
     */
    public static boolean isKeyboardVisible(Activity activity) {

        if (activity == null) {
            return false;
        }

        Rect r = new Rect();

        View activityRoot = getActivityRoot(activity);

        int visibleThreshold = Math.round(DisplayUtil.dip2px(activity, KEYBOARD_VISIBLE_THRESHOLD_DP));

        activityRoot.getWindowVisibleDisplayFrame(r);

        int heightDiff = activityRoot.getRootView().getHeight() - r.height();

        return heightDiff > visibleThreshold;
    }

    private static final int KEYBOARD_VISIBLE_THRESHOLD_DP = 100;


    private static View getActivityRoot(Activity activity) {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    public static void hideKeyBoard(Context context, IBinder windowToken) {
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}
