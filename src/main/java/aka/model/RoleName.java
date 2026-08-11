package aka.model;

public enum RoleName {
    ROLE_TEACHER,
    ROLE_ADMIN;

    /**
     * Safely parse RoleName from String with fallback default.
     */
    public static RoleName of(String name, RoleName fallback) {
        return (name == null || name.isBlank()) ? fallback : parse(name, fallback);
    }

    private static RoleName parse(String name, RoleName fallback) {
        try {
            return RoleName.valueOf(name.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
