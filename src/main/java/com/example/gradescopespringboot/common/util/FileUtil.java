package com.example.gradescopespringboot.common.util;

import java.util.Locale;
import java.util.Set;

/**
 * File name and type utilities.
 */
public final class FileUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt",
            "zip", "rar", "7z", "png", "jpg", "jpeg", "gif", "csv", "md"
    );

    private FileUtil() {
    }

    /**
     * Extract the lower-case file extension from a file name.
     *
     * @param fileName original file name
     * @return extension without dot, or empty string
     */
    public static String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Check whether the file extension is on the upload whitelist.
     *
     * @param fileName original file name
     * @return true if allowed
     */
    public static boolean isAllowedExtension(String fileName) {
        return ALLOWED_EXTENSIONS.contains(getExtension(fileName));
    }

    /**
     * Reduce an untrusted file name to its simple base name, stripping any
     * directory components and path traversal sequences.
     *
     * @param fileName untrusted original file name
     * @return sanitized base file name
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unnamed";
        }
        String normalized = fileName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String base = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return base.replace("..", "_").replaceAll("[\\r\\n]", "_");
    }
}
