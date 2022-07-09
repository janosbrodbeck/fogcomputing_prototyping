package fogcomputing.util;


import java.util.Optional;

public class PropertyUtils {

    public static Optional<String> readProperty(String propertyPrefix, String propertyName) {

        String property = System.getProperty(toSystemKey(propertyPrefix, propertyName));

        if (property == null) {
            property = System.getenv(toEnvKey(propertyPrefix, propertyName));
        }

        // todo configuration files

        return Optional.ofNullable(property);
    }

    public static String toEnvKey(String prefix, String key) {
        StringBuilder stringBuilder = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            stringBuilder.append(prefix.toUpperCase());
            stringBuilder.append('_');
        }
        stringBuilder.append(key.toUpperCase());
        return stringBuilder.toString();
    }

    public static String toSystemKey(String prefix, String key) {
        StringBuilder stringBuilder = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            stringBuilder.append(prefix);
            stringBuilder.append('.');
        }
        stringBuilder.append(key);
        return stringBuilder.toString();
    }
}
