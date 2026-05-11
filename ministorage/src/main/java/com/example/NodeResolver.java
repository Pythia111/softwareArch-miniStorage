package com.example;

/**
 * 文件树节点解析器。
 * 集中处理从根目录沿路径定位节点的逻辑，避免命令层重复实现。
 */
public class NodeResolver {

    public static Node resolve(Node root, String[] pathComponents) {
        Node current = root;
        if (pathComponents == null) {
            return current;
        }

        for (String component : pathComponents) {
            if (component == null || component.isEmpty()) {
                return null;
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
        if (parent == null || !parent.isDirectory()) {
            return null;
        }
        return (Directory) parent;
    }
}