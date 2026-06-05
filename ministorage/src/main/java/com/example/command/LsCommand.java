package com.example.command;

import com.example.fs.Directory;
import com.example.fs.Link;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 处理 LS 命令的逻辑。
 * 如果目标是文件或链接到文件，输出链接自身名称。
 * 如果目标是目录或链接到目录，输出子节点列表。
 */
public class LsCommand {

    /**
     * 执行 LS 命令并返回应输出的每一行。
     * - 若目标是文件或链接到文件：返回链接自身名称
     * - 若目标是目录或链接到目录：返回其直接子节点名称（字母序）
     * - 若路径不存在：返回空列表
     *
     * @param root    根目录节点
     * @param absPath 绝对路径
     * @return 输出行列表
     */
    public static List<String> execute(Node root, String absPath) {
        PathInfo pathInfo = PathUtil.parse(absPath);
        if (pathInfo == null) {
            return new ArrayList<>();
        }

        Node node = NodeResolver.resolve(root, pathInfo);
        if (node == null) {
            return new ArrayList<>();
        }

        // 跟随链接到目标节点
        Node target = node;
        if (node instanceof Link) {
            target = ((Link) node).getTarget();
        }

        // 如果目标是文件，返回原节点（可能是链接）的名称
        if (target.isFile()) {
            List<String> out = new ArrayList<>();
            out.add(node.getName());
            return out;
        }

        // 如果目标是目录，返回目录的子节点列表
        if (target.isDirectory()) {
            Directory dir = (Directory) target;
            Collection<String> children = dir.listChildren();
            return new ArrayList<>(children);
        }

        return new ArrayList<>();
    }
}
