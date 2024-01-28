package com.davofredo.util.map;

public class TypeCastException extends RuntimeException {
    private static final String TEMPLATE_DEFAULT = "Field \"%s\" was expected to be an instance of %s, but got %s when trying to %s a value %s \"%s\"";
    public TypeCastException(String message) {
        super(message);
    }

    TypeCastException(MapAttribute attribute, Object mismatchObject, Class<?> expectedType, boolean isAttemptToWrite) {
        super(String.format(TEMPLATE_DEFAULT,
                attribute.getName(),
                expectedType.getName(),
                mismatchObject.getClass().getName(),
                isAttemptToWrite ? "write" : "read",
                isAttemptToWrite ? "into" : "from",
                attribute.getPath()));
    }

    public TypeCastException(String attributeName, String targetPath, Object mismatchObject, Class<?> expectedType, boolean isAttemptToWrite) {
        super(String.format(TEMPLATE_DEFAULT,
                attributeName,
                expectedType.getName(),
                mismatchObject.getClass().getName(),
                isAttemptToWrite ? "write" : "read",
                isAttemptToWrite ? "into" : "from",
                targetPath));
    }
}
