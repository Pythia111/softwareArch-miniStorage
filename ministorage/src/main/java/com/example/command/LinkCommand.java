package com.example.command;

import com.example.fs.Directory;
import com.example.fs.Link;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

/**
 * 处理 LINK 命令的逻辑。
 * 创建链接：将dstPath指向srcPath的节点。
 * 源路径必须存在，目标父目录必须存在，不允许替换根目录。
 */
public class LinkCommand {

    /**
     * 执行 LINK 命令。
     *
     * @param root    根目录节点
     * @param srcPath 源路径（链接指向的节点）
     * @param dstPath 目标路径（链接创建的位置）
     */
    public static void execute(Node root, String srcPath, String dstPath) {
        // 目标路径不能是根目录
        PathInfo dstInfo = PathUtil.parseNonRoot(dstPath);
        if (dstInfo == null) {
            return;
        }

        // 源路径必须存在
        Node source = NodeResolver.resolve(root, srcPath);
        if (source == null) {
            return;
        }

        // 目标父目录必须存在
        Directory dstParent = NodeResolver.resolveParentDirectory(root, dstInfo);
        if (dstParent == null) {
            return;
        }

        // 创建链接（TreeMap会自动覆盖已有节点）
        dstParent.putChild(dstInfo.getBaseName(), new Link(dstInfo.getBaseName(), source));
    }
}
