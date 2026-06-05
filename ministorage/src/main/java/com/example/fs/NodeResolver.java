package com.example.fs;

import com.example.path.PathInfo;
import com.example.path.PathUtil;

/**
 * 文件树节点解析器。
 * 集中处理从根目录沿路径定位节点的逻辑，避免命令层重复实现。
 */
public class NodeResolver {

    public static Node resolve(Node root, String absPath) {
        PathInfo pathInfo = PathUtil.parse(absPath);
        if (pathInfo == null) {
            return null;
        }
        return resolve(root, pathInfo);
    }

    /**
     * 解析路径到节点，路径解析过程中跟随链接
     *
     * @param root 根节点
     * @param pathComponents 路径组件数组
     * @return 解析到的节点，如果路径不存在返回null
     */
    public static Node resolve(Node root, String[] pathComponents) {
        Node current = root;
        if (pathComponents == null) {
            return current;
        }

        for (String component : pathComponents) {
            if (component == null || component.isEmpty()) {
                return null;
            }

            // 递归解析链接链：确保current是目录或链接到目录
            while (current instanceof Link) {
                current = ((Link) current).getTarget();
            }

            if (!current.isDirectory()) {
                return null;
            }

            Directory dir = (Directory) current;
            current = dir.getChild(component);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * 解析路径到节点，但不跟随路径中间的链接（用于FIND命令遍历）
     *
     * @param root 根节点
     * @param pathComponents 路径组件数组
     * @return 解析到的节点，如果路径不存在返回null
     */
    public static Node resolveWithoutFollowingLinks(Node root, String[] pathComponents) {
        Node current = root;
        if (pathComponents == null) {
            return current;
        }

        for (String component : pathComponents) {
            if (component == null || component.isEmpty()) {
                return null;
            }

            // 不跟随链接，直接检查是否为目录
            if (!current.isDirectory()) {
                return null;
            }

            Directory dir = (Directory) current;
            current = dir.getChild(component);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    public static Node resolve(Node root, PathInfo pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        return resolve(root, pathInfo.getComponents());
    }

    public static Directory resolveParentDirectory(Node root, PathInfo pathInfo) {
        if (pathInfo == null) {
            return null;
        }

        Node parent = resolve(root, PathUtil.split(pathInfo.getParentPath()));
        if (parent == null) {
            return null;
        }

        // 递归解析链接链
        while (parent instanceof Link) {
            parent = ((Link) parent).getTarget();
        }

        if (!parent.isDirectory()) {
            return null;
        }
        return (Directory) parent;
    }

    public static Directory resolveParentDirectory(Node root, String absPath) {
        PathInfo pathInfo = PathUtil.parseNonRoot(absPath);
        if (pathInfo == null) {
            return null;
        }
        return resolveParentDirectory(root, pathInfo);
    }
}