package com.example.command;

import com.example.fs.Link;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.fs.SizeContext;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

/**
 * 处理 INFO 命令的逻辑。
 * 输出节点大小：文件返回大小，目录返回子树文件大小之和（防重复计数）。
 */
public class InfoCommand {

    /**
     * 执行 INFO 命令。
     *
     * @param root    根目录节点
     * @param absPath 绝对路径
     * @return 节点大小，如果路径不存在返回null
     */
    public static Long execute(Node root, String absPath) {
        PathInfo pathInfo = PathUtil.parse(absPath);
        if (pathInfo == null) {
            return null;
        }

        // 定位目标节点
        Node node = NodeResolver.resolve(root, pathInfo);
        if (node == null) {
            return null;
        }

        // 跟随链接
        Node target = node;
        if (node instanceof Link) {
            target = ((Link) node).getTarget();
        }

        return target.size(new SizeContext());
    }
}
