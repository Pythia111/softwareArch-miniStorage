package com.example;

/**
 * 路径工具类：负责绝对路径的解析与规范化。
 * 迭代一仅处理去除多余斜杠，迭代二将扩展支持 . / .. 语义。
 */
public class PathUtil {

    /**
     * 规范化绝对路径：去除多余斜杠。
     * 例：///a//b → /a/b
     *
     * 迭代二扩展点：在此方法中增加对 "." 和 ".." 的处理。
     * "." 表示当前目录，直接跳过；
     * ".." 表示父目录，弹出栈顶（根目录的 ".." 仍为根目录）。
     */
    public static String normalize(String absPath) {
        if (absPath == null || absPath.isEmpty()) {
            return "/";
        }

        String[] parts = absPath.split("/");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            // 迭代二：在此处添加对 "." 和 ".." 的处理逻辑
            // if (part.equals(".")) continue;
            // if (part.equals("..")) { /* 弹出栈顶，根目录的 .. 仍为根目录 */ continue; }
            sb.append("/").append(part);
        }

        return sb.length() == 0 ? "/" : sb.toString();
    }

    /**
     * 将绝对路径拆分为路径组件数组。
     * 例：/a/b/c → ["a", "b", "c"]
     * 根路径 / → []
     */
    public static String[] split(String absPath) {
        String normalized = normalize(absPath);
        if (normalized.equals("/")) {
            return new String[0];
        }
        // 去掉开头的 /，再按 / 拆分
        String trimmed = normalized.substring(1);
        return trimmed.split("/");
    }

    /**
     * 获取父路径。
     * 例：/a/b/c → /a/b，/a → /，/ → /
     */
    public static String getParentPath(String absPath) {
        String normalized = normalize(absPath);
        if (normalized.equals("/")) {
            return "/";
        }
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash == 0) {
            return "/";
        }
        return normalized.substring(0, lastSlash);
    }

    /**
     * 获取路径末尾的名称部分。
     * 例：/a/b/c → c，/a → a，/ → ""
     */
    public static String getBaseName(String absPath) {
        String normalized = normalize(absPath);
        if (normalized.equals("/")) {
            return "";
        }
        int lastSlash = normalized.lastIndexOf('/');
        return normalized.substring(lastSlash + 1);
    }
}
