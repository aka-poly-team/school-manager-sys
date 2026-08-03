package aka.util;

/**
 * Utility library for String operations.
 */
public class StringUtils {

    public static String mask(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0) + "****@" + domain;
        }
        return name.substring(0, 2) + "****@" + domain;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
