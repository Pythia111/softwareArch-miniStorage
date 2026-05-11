package com.example.command;

import com.example.fs.Directory;
import com.example.fs.File;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

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
        PathInfo pathInfo = PathUtil.parseNonRoot(absPath);
        if (pathInfo == null) {
            return;
        }

        // 定位父目录
        Directory parentDir = NodeResolver.resolveParentDirectory(root, pathInfo);
        if (parentDir == null) {
            return;
        }

        // 创建新文件，或覆盖任何同名节点（包括目录）
        parentDir.putChild(pathInfo.getBaseName(), new File(pathInfo.getBaseName(), size));
    }
}
