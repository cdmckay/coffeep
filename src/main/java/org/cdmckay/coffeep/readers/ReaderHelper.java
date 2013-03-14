package org.cdmckay.coffeep.readers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ReaderHelper {

    private ReaderHelper() {}

    public static String getString(Object object, String name, Object... parameters) {
        String value = "???";
        try {
            String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            List<Class<?>> parametersTypeList = new ArrayList<Class<?>>();
            for (Object parameter : parameters) {
                parametersTypeList.add(parameter.getClass());
            }
            Class<?>[] parameterTypes = parametersTypeList.toArray(new Class<?>[parametersTypeList.size()]);

            Method method;
            try {
                method = object.getClass().getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                // Try again, but this time with all boxed types as primitives.
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameterTypes[i] = primitivize(parameterTypes[i]);
                }
                method = object.getClass().getMethod(methodName, parameterTypes);
            }
            value = (String) method.invoke(object, parameters);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return value.replace('/', '.');
    }

    public static Class<?> primitivize(Class<?> type) {
        if (Boolean.class.equals(type)) {
            return Boolean.TYPE;
        }
        if (Character.class.equals(type)) {
            return Character.TYPE;
        }
        if (Byte.class.equals(type)) {
            return Byte.TYPE;
        }
        if (Short.class.equals(type)) {
            return Short.TYPE;
        }
        if (Integer.class.equals(type)) {
            return Integer.TYPE;
        }
        if (Long.class.equals(type)) {
            return Long.TYPE;
        }
        if (Float.class.equals(type)) {
            return Float.TYPE;
        }
        if (Double.class.equals(type)) {
            return Double.TYPE;
        }
        return type;
    }

}
