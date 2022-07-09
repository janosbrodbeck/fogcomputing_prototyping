package fogcomputing.util;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static void assignValueToField(Object instance, Field field, String value) {
        boolean originalAccessible = true;

        if (!field.isAccessible()) {
            field.setAccessible(true);
            originalAccessible = false;
        }

        try {
            field.set(instance, coerceValue(value, field.getType()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException();
        }

        if (!originalAccessible) {
            field.setAccessible(false);
        }

    }

    // todo add additional types as needed. Maybe with a registry even. (or at this point just use Spring Framework..)
    public static Object coerceValue(String value, Class<?> coerceTo) throws IllegalArgumentException {
        if (String.class.equals(coerceTo)) {
            return value;
        } else if (Integer.class.equals(coerceTo) || int.class.equals(coerceTo)) {
            return Integer.valueOf(value);
        } else if (Boolean.class.equals(coerceTo) || boolean.class.equals(coerceTo)) {
            return Boolean.valueOf(value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + coerceTo.getName());
        }
    }

}
