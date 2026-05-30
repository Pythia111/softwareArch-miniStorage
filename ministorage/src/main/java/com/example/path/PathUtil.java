package com.example.path;

import java.util.ArrayList;
import java.util.List;

/**
 * 路径工具类：负责绝对路径的解析与规范化。
 * 迭代二支持冗余/、.、..的自动规范化。
 */
public class PathUtil {

    public static boolean isAbsolutePath(String path) {
        return path != null && !path.isEmpty() && path.startsWith("/");
    }

    /**
     * 规范化绝对路径：处理冗余/、.、..、尾随/。
     * 非绝对路径（null、空串、不以/开头）返回null。
     */
    public static String normalize(String absPath) {
        if (absPath == null || !absPath.startsWith("/")) {
            return null;
        }

        // 分割路径，连续/视为一个分隔符
        String[] parts = absPath.split("/+");

        // 使用栈处理 . 和 ..
        List<String> stack = new ArrayList<>();
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            } else if (part.equals("..")) {
                if (!stack.isEmpty()) {
                    stack.remove(stack.size() - 1);
                }
            } else {
                stack.add(part);
            }
        }

        if (stack.isEmpty()) return "/";
        return "/" + String.join("/", stack);
    }

    /**
     * 将已规范化的绝对路径拆分为路径组件数组。
     * 输入必须是normalize()的输出。
     * 例：/a/b/c → ["a", "b", "c"]
     * 根路径 / → []
     */
    public static String[] split(String normalizedPath) {
        if (normalizedPath.equals("/")) return new String[0];
        return normalizedPath.substring(1).split("/");
    }

    /**
     * 获取父路径。
     * 输入必须是normalize()的输出。
     * 例：/a/b/c → /a/b，/a → /，/ → null
     */
    public static String getParentPath(String normalizedPath) {
        if (normalizedPath.equals("/")) return null;
        int lastSlash = normalizedPath.lastIndexOf('/');
        if (lastSlash == 0) return "/";
        return normalizedPath.substring(0, lastSlash);
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
     * 先规范化，再拆分。非法路径返回null。
     */
    public static PathInfo parse(String absPath) {
        String normalized = normalize(absPath);
        if (normalized == null) {
            return null;
        }

        return new PathInfo(
            normalized,
            split(normalized),
            getParentPath(normalized),
            getBaseName(normalized)
        );
    }

    public static PathInfo parseNonRoot(String absPath) {
        PathInfo pathInfo = parse(absPath);
        if (pathInfo == null || pathInfo.isRoot()) {
            return null;
        }
        return pathInfo;
    }
}
