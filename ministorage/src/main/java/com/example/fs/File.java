package com.example.fs;

public class File extends Node {
    private long size;

    public File(String name, long size) {
        super(name);
        this.size = size;
    }

    @Override
    public NodeType type() {
        return NodeType.FILE;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public long size(SizeContext ctx) {
        return size;
    }
}
