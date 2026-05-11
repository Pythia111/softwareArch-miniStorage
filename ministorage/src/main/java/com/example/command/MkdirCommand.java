package com.example.command;

import com.example.fs.Directory;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

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
        PathInfo pathInfo = PathUtil.parseNonRoot(absPath);
        if (pathInfo == null) {
            return;
        }

        // 定位父目录
        Directory parentDir = NodeResolver.resolveParentDirectory(root, pathInfo);
        if (parentDir == null) {
            return;
        }

        // 若同名节点是目录，静默忽略；若是文件，则覆盖
        Node existing = parentDir.getChild(pathInfo.getBaseName());
        if (existing != null && existing.isDirectory()) {
            return;
        }

        parentDir.putChild(pathInfo.getBaseName(), new Directory(pathInfo.getBaseName()));
    }
}
