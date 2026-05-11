package com.example.fs;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;

/**
 * 目录类：作为唯一命名空间，使用 TreeMap 维护子节点，天然按字母序排序。
 */
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

    @Override
    public boolean isDirectory() {
        return true;
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

    /**
     * 返回已排序的子节点名称集合。
     */
    public Collection<String> listChildren() {
        if (children.isEmpty()) {
            return Collections.emptyList();
        }
        return children.navigableKeySet();
    }

    /**
     * 递归计算目录大小，累加所有子节点大小。
     * 注意：必须传递同一个 SizeContext 实例，以支持未来链接去重/防环。
     */
    @Override
    public long size(SizeContext ctx) {
        long total = 0;
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node == null) {
                continue;
            }

            if (node.isDirectory()) {
                if (ctx.isVisited(node)) {
                    continue;
                }
                ctx.addVisited(node);

                Directory dir = (Directory) node;
                for (Map.Entry<String, Node> entry : dir.children.entrySet()) {
                    Node child = entry.getValue();
                    if (child != null) {
                        stack.push(child);
                    }
                }
                continue;
            }

            total += node.size(ctx);
        }

        return total;
    }
}
