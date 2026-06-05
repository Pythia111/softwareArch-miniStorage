package com.example.command;

import com.example.fs.Directory;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

/**
 * 处理 RM 命令的逻辑。
 * 删除文件、链接或空目录。非空目录不能删除，根目录不能删除。
 */
public class RmCommand {

    /**
     * 执行 RM 命令。
     * - 删除文件：直接删除
     * - 删除链接：只删除链接本身，不影响被链接目标
     * - 删除目录：只有目录为空时才允许删除
     * - 不允许删除根目录
     *
     * @param root    根目录节点
     * @param absPath 要删除的节点的绝对路径
     */
    public static void execute(Node root, String absPath) {
        // 规范化路径，检查是否为根目录
        PathInfo pathInfo = PathUtil.parseNonRoot(absPath);
        if (pathInfo == null) {
            // 根目录或非法路径，忽略
            return;
        }

        // 定位目标节点
        Node target = NodeResolver.resolve(root, pathInfo);
        if (target == null) {
            // 路径不存在，忽略
            return;
        }

        // 如果是目录，检查是否为空
        if (target.isDirectory()) {
            Directory dir = (Directory) target;
            if (!dir.listChildren().isEmpty()) {
                // 非空目录，不能删除
                return;
            }
        }

        // 定位父目录
        Directory parentDir = NodeResolver.resolveParentDirectory(root, pathInfo);
        if (parentDir == null) {
            // 父目录不存在（理论上不应该发生，因为目标节点已经存在）
            return;
        }

        // 删除节点（文件、链接或空目录）
        parentDir.removeChild(pathInfo.getBaseName());
    }
}
