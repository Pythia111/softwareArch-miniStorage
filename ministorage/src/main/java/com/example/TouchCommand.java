package com.example;

/**
 * 处理 TOUCH 命令的逻辑。
 * 创建文件：若父目录存在且为目录，则在其下创建新文件；若同名文件已存在则覆盖。
 */
public class TouchCommand {

    /**
     * 执行 TOUCH 命令。
     *
     * @param root    根目录节点
     * @param absPath 要创建的文件的绝对路径
     * @param size    文件大小
     */
    public static void execute(Node root, String absPath, long size) {
        if (!PathUtil.isAbsolutePath(absPath)) {
            return;
        }

        String normalized = PathUtil.normalize(absPath);

        // 不能在根目录创建文件（路径必须包含文件名）
        if (normalized.equals("/")) {
            return;
        }

        String parentPath = PathUtil.getParentPath(normalized);
        String fileName = PathUtil.getBaseName(normalized);

        // 定位父目录
        Node parent = findNode(root, PathUtil.split(parentPath));
        if (parent == null || !parent.isDirectory()) {
            return;
        }

        Directory parentDir = (Directory) parent;
        // 无条件创建文件（覆盖任何同名节点：文件或目录）
        parentDir.putChild(fileName, new File(fileName, size));
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
