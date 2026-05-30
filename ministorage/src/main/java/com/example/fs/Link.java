package com.example.fs;

public class Link extends Node {
    private Node target;

    public Link(String name, Node target) {
        super(name);
        this.target = target;
    }

    public Node getTarget() {
        return target;
    }

    @Override
    public NodeType type() {
        return NodeType.LINK;
    }

    @Override
    public long size(SizeContext ctx) {
        return target.size(ctx);
    }
}
