package fogcomputing.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static fogcomputing.util.ReflectionUtils.assignValueToField;
import static fogcomputing.util.PropertyUtils.readProperty;

/**
 * Utility to get Properties from configuration files, environment variables or system properties {@code -Dname="value"}.
 * Priority in that order.
 */
public class ConfigUtils {

    public static <T> T getProperties(String propertyPrefix, Class<T> propertiesClass) {


        try {
            Constructor<T> constructor = propertiesClass.getConstructor();
            T properties = constructor.newInstance();

            Field[] fields = propertiesClass.getDeclaredFields();

            for (Field field : fields) {
                // primitive implementation: try to set all fields
                // possible improvement: only set via constructor with all final fields as param only

                Optional<String> property = readProperty(propertyPrefix, field.getName());
                property.ifPresent(p -> assignValueToField(properties, field, p));
            }

            return properties;

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            // TODO explicit error handling and explanations
            e.printStackTrace();
            throw new RuntimeException();
        }
    }




}
