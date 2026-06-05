package com.example;

import com.example.command.FindCommand;
import com.example.command.InfoCommand;
import com.example.command.LsCommand;
import com.example.command.LinkCommand;
import com.example.command.MkdirCommand;
import com.example.command.RmCommand;
import com.example.command.TouchCommand;
import com.example.fs.Directory;

import java.util.List;

/**
 * 微型内存文件系统外观类。
 * 持有根目录实例，对外提供统一的命令接口。
 */
public class MemFs {
    private final Directory root;

    public MemFs() {
        this.root = new Directory("");
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
     * 如果目标是文件或链接到文件，返回链接自身名称。
     * 如果目标是目录或链接到目录，返回子节点列表（字典序）。
     * 如果路径不存在，返回空列表。
     *
     * @param absPath 绝对路径
     * @return 子节点名称列表
     */
    public List<String> ls(String absPath) {
        return LsCommand.execute(root, absPath);
    }

    /**
     * 输出节点的大小。
     *
     * @param absPath 绝对路径
     * @return 节点大小，如果路径不存在返回null
     */
    public Long info(String absPath) {
        return InfoCommand.execute(root, absPath);
    }

    /**
     * 查找指定名称的节点。
     * 递归搜索，按字典序输出所有匹配路径。
     * 如果起点是文件或链接到文件，只检查该起点自身名称是否匹配。
     * 如果起点是目录或链接到目录，递归搜索该目录子树。
     * 链接节点本身也可以作为匹配结果，匹配的是链接名称。
     * 防重复：同一个底层目录在一次FIND中最多展开一次。
     *
     * @param absPath    起始路径
     * @param targetName 要查找的节点名称
     * @return 匹配的绝对路径列表（字典序升序）
     */
    public List<String> find(String absPath, String targetName) {
        return FindCommand.execute(root, absPath, targetName);
    }

    /**
     * 删除节点。
     * 只能删除文件、链接或空目录。
     *
     * @param absPath 绝对路径
     */
    public void rm(String absPath) {
        RmCommand.execute(root, absPath);
    }

    /**
     * 创建链接。
     * 将dstPath指向srcPath的节点。
     *
     * @param srcPath 源路径
     * @param dstPath 目标路径
     */
    public void link(String srcPath, String dstPath) {
        LinkCommand.execute(root, srcPath, dstPath);
    }
}
