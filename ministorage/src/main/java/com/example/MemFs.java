package com.example;

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
    public void touch(String absPath, int size) {
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
     * 输出该节点的大小。
     * 如果是目录，需递归计算总大小。
     *
     * @param absPath 绝对路径
     */
    public void info(String absPath) {
        InfoCommand.execute(root, absPath);
    }
}
