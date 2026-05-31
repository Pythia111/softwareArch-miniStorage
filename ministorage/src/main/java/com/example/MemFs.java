package com.example;

import com.example.command.LsCommand;
import com.example.command.MkdirCommand;
import com.example.command.TouchCommand;
import com.example.fs.Directory;
import com.example.fs.Link;
import com.example.fs.Node;
import com.example.path.PathUtil;

/**
 * 微型内存文件系统外观类。
 * 持有根目录实例，对外提供统一的命令接口。
 */
public class MemFs {
    private final Directory root;

    public MemFs() {
        this.root = new Directory("");
    }

    // ===== 核心私有方法 - 所有命令层都要使用 =====

    /**
     * 根据绝对路径定位节点
     * 
     * @param absPath 原始路径(未规范化)
     * @return 找到的节点,不存在返回null
     */
    private Node locateNode(String absPath) {
        String normalized = PathUtil.normalize(absPath);
        if (normalized == null)
            return null;
        if (normalized.equals("/"))
            return root;

        String[] parts = PathUtil.split(normalized);
        Node current = root;
        for (String part : parts) {
            if (!(current instanceof Directory))
                return null;
            current = ((Directory) current).getChild(part);
            if (current == null)
                return null;
        }
        return current;
    }

    /**
     * 获取父目录
     * 
     * @param absPath 原始路径(未规范化)
     * @return 父目录,不存在或父路径不是目录返回null
     */
    private Directory getParentDirectory(String absPath) {
        String normalized = PathUtil.normalize(absPath);
        if (normalized == null || normalized.equals("/"))
            return null;

        String parentPath = PathUtil.getParentPath(normalized);
        Node parent = locateNode(parentPath);
        return (parent instanceof Directory) ? (Directory) parent : null;
    }

    /**
     * 解析链接到最终节点
     * 
     * @param node 原始节点
     * @return 如果是Link返回target,否则返回自身
     */
    private Node resolveLink(Node node) {
        return (node instanceof Link) ? ((Link) node).getTarget() : node;
    }

    /**
     * 获取路径的最后一个组件
     * 
     * @param absPath 原始路径(未规范化)
     * @return 名称,根目录返回null
     */
    private String getBaseName(String absPath) {
        String normalized = PathUtil.normalize(absPath);
        if (normalized == null || normalized.equals("/"))
            return null;
        return PathUtil.getBaseName(normalized);
    }

    /**
     * 创建目录。
     * 如果父目录不存在，静默忽略。
     *
     * @param absPath 绝对路径
     */
    public void mkdir(String absPath) {
        MkdirCommand.execute(root, absPath);
    }

    /**
     * 创建文件并指定大小。
     * 如果同名文件已存在则覆盖。
     *
     * @param absPath 绝对路径
     * @param size    文件大小
     */
    public void touch(String absPath, long size) {
        TouchCommand.execute(root, absPath, size);
    }

    /**
     * 列出路径下的所有直接子节点。
     * 如果目标是文件，则只输出文件名。
     *
     * @param absPath 绝对路径
     */
    public void ls(String absPath) {
        LsCommand.execute(root, absPath);
    }

    /**
     * 输出节点的大小。
     *
     * @param absPath 绝对路径
     */
    public Long info(String absPath) {
        Node node = locateNode(absPath);
        if (node == null)
            return null;

        Node target = resolveLink(node);

        return target.size(new com.example.fs.SizeContext());
    }

    /**
     * 查找指定名称的节点。
     * 递归搜索，按字典序输出所有匹配路径。
     *
     * @param absPath    起始路径
     * @param targetName 要查找的节点名称
     */
    public void find(String absPath, String targetName) {
        // 阶段3由成员2实现
        throw new UnsupportedOperationException("FIND命令待实现");
    }

    /**
     * 删除节点。
     * 只能删除文件、链接或空目录。
     *
     * @param absPath 绝对路径
     */
    public void rm(String absPath) {
        // 阶段3由成员4实现
        throw new UnsupportedOperationException("RM命令待实现");
    }

    /**
     * 创建链接。
     * 将dstPath指向srcPath的节点。
     *
     * @param srcPath 源路径
     * @param dstPath 目标路径
     */
    public void link(String srcPath, String dstPath) {
        // 阶段3由成员3实现
        throw new UnsupportedOperationException("LINK命令待实现");
    }
}
