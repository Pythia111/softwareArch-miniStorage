package com.example;

/**
 * 处理 MKDIR 命令的逻辑。
 * 创建目录：若父目录存在且为目录，则在其下创建新子目录；否则静默忽略。
 */
public class MkdirCommand {

    /**
     * 执行 MKDIR 命令。
     *
     * @param root    根目录节点
     * @param absPath 要创建的目录的绝对路径
     */
    public static void execute(Node root, String absPath) {
        if (!PathUtil.isAbsolutePath(absPath)) {
            return;
        }

        String normalized = PathUtil.normalize(absPath);

        // 不能创建根目录
        if (normalized.equals("/")) {
            return;
        }

        String parentPath = PathUtil.getParentPath(normalized);
        String dirName = PathUtil.getBaseName(normalized);

        // 定位父目录
        Node parent = findNode(root, PathUtil.split(parentPath));
        if (parent == null || !parent.isDirectory()) {
            return;
        }

        Directory parentDir = (Directory) parent;
        // 若同名子节点已存在，静默忽略
        if (parentDir.getChild(dirName) != null) {
            return;
        }

        parentDir.putChild(dirName, new Directory(dirName));
    }

    /**
     * 根据路径组件从根节点定位目标节点。
     */
    private static Node findNode(Node root, String[] pathComponents) {
        Node current = root;
        if (pathComponents == null) {
            return current;
        }

        for (String component : pathComponents) {
            if (component == null || component.isEmpty()) {
                continue;
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
}
