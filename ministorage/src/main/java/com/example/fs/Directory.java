package com.example.fs;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Directory extends Node {
    private final TreeMap<String, Node> children;

    public Directory(String name) {
        super(name);
        this.children = new TreeMap<>();
    }

    @Override
    public NodeType type() {
        return NodeType.DIRECTORY;
    }

    public void putChild(String name, Node node) {
        if (name == null || name.isEmpty() || node == null) {
            return;
        }
        children.put(name, node);
    }

    public Node getChild(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return children.get(name);
    }

    public void removeChild(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        children.remove(name);
    }

    public Collection<String> listChildren() {
        if (children.isEmpty()) {
            return Collections.emptyList();
        }
        return children.navigableKeySet();
    }

    public Collection<Node> getChildrenValues() {
        return children.values();
    }

    @Override
    public long size(SizeContext ctx) {
        long total = 0;
        for (Node child : children.values()) {
            Node realNode = (child instanceof Link)
                ? ((Link)child).getTarget()
                : child;

            if (!ctx.isVisited(realNode)) {
                ctx.markVisited(realNode);
                total += child.size(ctx);
            }
        }
        return total;
    }
}
