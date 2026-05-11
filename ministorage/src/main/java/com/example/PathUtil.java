package com.example;

/**
 * 路径工具类：负责绝对路径的解析。
 * 迭代一只接受规范的绝对路径，不对输入做自动规范化。
 */
public class PathUtil {

    public static boolean isAbsolutePath(String path) {
        return path != null && !path.isEmpty() && path.startsWith("/");
    }

    public static boolean isCanonicalAbsolutePath(String path) {
        if (!isAbsolutePath(path)) {
            return false;
        }
        if ("/".equals(path)) {
            return true;
        }
        return !path.endsWith("/") && !path.contains("//");
    }

    /**
     * 迭代一不对绝对路径做自动规范化，保留原始输入。
     * 迭代二若需要支持冗余 /、.、..，可在此处扩展。
     */
    public static String normalize(String absPath) {
        if (!isAbsolutePath(absPath)) {
            return "/";
        }
        return absPath;
    }

    /**
     * 将绝对路径拆分为路径组件数组。
     * 例：/a/b/c → ["a", "b", "c"]
     * 根路径 / → []
     */
    public static String[] split(String absPath) {
        if (!isAbsolutePath(absPath)) {
            return null;
        }
        if (absPath.equals("/")) {
            return new String[0];
        }
        // 保留空片段，交由上层判断路径是否合法。
        String trimmed = absPath.substring(1);
        return trimmed.split("/", -1);
    }

    /**
     * 获取父路径。
     * 例：/a/b/c → /a/b，/a → /，/ → /
     */
    public static String getParentPath(String absPath) {
        if (absPath.equals("/")) {
            return "/";
        }
        int lastSlash = absPath.lastIndexOf('/');
        if (lastSlash == 0) {
            return "/";
        }
        return absPath.substring(0, lastSlash);
    }

    /**
     * 获取路径末尾的名称部分。
     * 例：/a/b/c → c，/a → a，/ → ""
     */
    public static String getBaseName(String absPath) {
        if (absPath.equals("/")) {
            return "";
        }
        int lastSlash = absPath.lastIndexOf('/');
        return absPath.substring(lastSlash + 1);
    }

    /**
     * 解析绝对路径并返回基础路径元信息。
     * 迭代一只做最小规范化；迭代二可在本方法内部扩展 . / .. 语义。
     */
    public static PathInfo parse(String absPath) {
        if (!isCanonicalAbsolutePath(absPath)) {
            return null;
        }

        String normalized = normalize(absPath);
        return new PathInfo(
            normalized,
            split(normalized),
            getParentPath(normalized),
            getBaseName(normalized)
        );
    }
}
