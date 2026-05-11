package com.example;

/**
 * 虚拟文件系统中所有节点的抽象基类。
 * 统一文件（File）、目录（Directory）以及未来可能存在的链接节点。
 */
public abstract class Node {
    protected String name;

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 获取当前节点的类型
     */
    public abstract NodeType type();

    /**
     * 判断当前节点是否为目录（作为辅助方法保留，满足明确分工）
     */
    public abstract boolean isDirectory();

    /**
     * 计算当前节点大小。
     * 供命令层直接调用，内部自动创建统计上下文。
     */
    public final long size() {
        return size(new SizeContext());
    }

    /**
     * 计算当前节点大小。携带 SizeContext 上下文以去重或防环。
     */
    public abstract long size(SizeContext ctx);
}
