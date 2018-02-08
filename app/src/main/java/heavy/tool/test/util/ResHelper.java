package heavy.tool.test.util;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class ResHelper {
    private static ConcurrentHashMap<String, Integer> mResourcesMap = new ConcurrentHashMap<String, Integer>();

    private static int getResIDByNameImp(Context context, String resType, String name) {
        String key = name + resType;
        if (mResourcesMap.containsKey(key)) {
            return mResourcesMap.get(key);
        }

        int resourceId = context.getResources().getIdentifier(name, resType, context.getPackageName());
        mResourcesMap.put(key, resourceId);
        return resourceId;
    }

    public static int getLayoutResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "layout", name);
    }

    public static int getIdResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "id", name);
    }

    public static int getStringResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "string", name);
    }

    public static int getDrawableResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "drawable", name);
    }

    public static int getRawResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "raw", name);
    }

    public static int getDimenResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "dimen", name);
    }

    public static int getColorResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "color", name);
    }

    public static int getAnimResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "anim", name);
    }

    public static int getStyleResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "style", name);
    }

    public static int getAttrResIdByName(Context context, String name) {
        return getResIDByNameImp(context, "attr", name);
    }

    public static int getSytleableResIDByName(Context context, String name) {
        return getResIDByNameImp(context, "styleable", name);
    }

    public static int[] getStyleableIntArrayByName(Context context, String name) {
        try {
            Field[] fields2 = Class.forName(context.getPackageName() + ".R$styleable").getFields();
            for (Field f : fields2) {
                if (f.getName().equals(name)) {
                    int[] ret = (int[]) f.get(null);
                    return ret;
                }
            }
        } catch (Throwable t) {
        }

        return null;
    }
}
