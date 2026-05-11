package com.example;

/**
 * 文件类，模拟一个普通文件节点，包含文件的大小信息。
 */
public class File extends Node {
    private final long size;

    public File(String name, long size) {
        super(name);
        this.size = size;
    }

    @Override
    public NodeType type() {
        return NodeType.FILE;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public long size(SizeContext ctx) {
        // 如果该节点已经被访问过了，直接返回0，预留防环机制
        if (ctx.isVisited(this)) {
            return 0;
        }
        ctx.addVisited(this);
        return size;
    }
}
