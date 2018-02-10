package heavy.tool.test.test.util;

import android.app.Activity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import heavy.test.plugin.model.data.reflection.MethodData;
import heavy.test.plugin.model.data.reflection.ObjectData;
import heavy.test.plugin.model.data.reflection.RuntimeValue;
import heavy.test.plugin.util.TextUtil;
import heavy.tool.test.util.LogUtil;


/**
 * Created by v_hfzhan on 2017/5/16.
 */

public class ReflectionUtil {

    private static final String TAG = "TestReflectionUtil";
    public static Map<String, Object> containerMap = new HashMap<>();

    public static <T> T getFieldObj(Object owner, String fieldName, Class<T> type) throws Exception {
        LogUtil.i(TAG, "getFieldObj owner->" + owner + " ;fieldName-->" + fieldName);
        Field field = owner.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(owner);
    }

    public static Object getObjectFromData(Activity activity, ObjectData data) throws Throwable {

        if (data == null) {
            LogUtil.i(TAG, "getObjectFromData object data  is null");
            return null;
        }
        LogUtil.i(TAG, "getObjectFromData data : " + data.getJsonObject().toString());
        Object container = null;
        if (ObjectData.FROM_ACTIVITY.equals(data.getContainerName())) {
            container = activity;
        }
        if (container == null) {
            container = containerMap.get(data.getContainerName());
            if (container != null) {
                LogUtil.i(TAG, "getContainer from cache");
            }
        }
        if (container == null) {
            container = getObjectFromData(activity, data.getContainer());
        }
        if (!TextUtil.isEmpty(data.getContainerName()) && !TextUtil.equals(ObjectData.FROM_ACTIVITY, data.getContainerName()) && !isPrimitiveData(container)) {
            containerMap.put(data.getContainerName(), container);
            LogUtil.i(TAG, "put container : " + container.getClass().getName() + "into cache");
        }
        Class<?> clazz = container == null ? Class.forName(data.getContainerClassName()) : container.getClass();
        Field field = clazz.getDeclaredField(data.getObjectName());
        field.setAccessible(true);
        Object result = field.get(container);
        LogUtil.i(TAG, "getObjectFromData data : " + data.getObjectName() + "; result : " + result);
        return result;
    }

    public static Object getObjectFromMethodCall(Activity activity, MethodData methodData) throws Throwable {
        if (methodData == null) {
            LogUtil.i(TAG, "getObjectFromMethodCall method data is null");
            return null;
        }
        Object container = null;
        if (ObjectData.FROM_ACTIVITY.equals(methodData.getContainerName())) {
            container = activity;
        }
        if (container == null) {
            container = getObjectFromData(activity, methodData.getContainer());
        }
        Class<?> clazz = container == null ? Class.forName(methodData.getContainerClassName()) : container.getClass();
        Class<?>[] argTypeList = null;
        Object[] argList = null;
        if (methodData.getArgList() != null && methodData.getArgList().size() > 0) {
            argList = new Object[methodData.getArgList().size()];
            argTypeList = new Class<?>[methodData.getArgList().size()];
            for (int i = 0; i < methodData.getArgList().size(); i++) {
                Object arg = getRuntimeValueObject(activity, methodData.getArgList().get(i));
                argList[i] = arg;
                argTypeList[i] = arg.getClass();
            }
        }
        LogUtil.i(TAG, "getObjectFromMethodCall method name :" + methodData.getMethodName()
                + "; container : " + container + "; argList : " + argList);

        if (methodData.isConstructor()) {
            Constructor constructor = clazz.getConstructor(argTypeList);
            constructor.setAccessible(true);
            return constructor.newInstance(argList);
        }
        Method method = clazz.getDeclaredMethod(methodData.getMethodName(), argTypeList);
        method.setAccessible(true);
        if (argList != null) {
            return method.invoke(container, argList);
        }
        return method.invoke(container);
    }

    public static Object getRuntimeValueObject(Activity activity, RuntimeValue runtimeValue) throws Throwable {
        if (runtimeValue == null) {
            return null;
        }
        if (runtimeValue.getObjectData() == null && runtimeValue.getMethodData() == null) {
            return runtimeValue.getValue();
        }
        if (runtimeValue.isFromMethod()) {
            return getObjectFromMethodCall(activity, runtimeValue.getMethodData());
        } else {
            return getObjectFromData(activity, runtimeValue.getObjectData());
        }
    }

    private static Boolean isPrimitiveData(Object object) {
        if (object instanceof String) {
            return true;
        }
        if (object instanceof Boolean) {
            return true;
        }
        if (object instanceof Integer) {
            return true;
        }
        if (object instanceof Long) {
            return true;
        }
        if (object instanceof Float) {
            return true;
        }
        if (object instanceof Double) {
            return true;
        }
        if (object instanceof Byte) {
            return true;
        }
        return false;
    }

}
