package com.example.path;

/**
 * 规范化后的绝对路径值对象。
 * 迭代一只封装基础拆分结果，便于迭代二在此扩展路径语义而不改命令逻辑。
 */
public class PathInfo {
    private final String normalizedPath;
    private final String[] components;
    private final String parentPath;
    private final String baseName;

    public PathInfo(String normalizedPath, String[] components, String parentPath, String baseName) {
        this.normalizedPath = normalizedPath;
        this.components = components;
        this.parentPath = parentPath;
        this.baseName = baseName;
    }

    public String getNormalizedPath() {
        return normalizedPath;
    }

    public String[] getComponents() {
        return components;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getBaseName() {
        return baseName;
    }

    public boolean isRoot() {
        return "/".equals(normalizedPath);
    }
}