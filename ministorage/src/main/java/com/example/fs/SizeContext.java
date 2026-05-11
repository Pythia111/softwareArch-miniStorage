package com.example.fs;

import java.util.HashSet;
import java.util.Set;

/**
 * 访问上下文，用于在计算大小时递归遍历节点，预留处理迭代二中可能出现的防无限循环机制。
 */
public class SizeContext {
    private Set<Node> visited;
    private boolean followLinks;

    public SizeContext() {
        this.visited = new HashSet<>();
        this.followLinks = false; // 迭代一默认不处理链接
    }

    public void addVisited(Node n) {
        visited.add(n);
    }

    public boolean isVisited(Node n) {
        return visited.contains(n);
    }

    public boolean shouldFollowLinks() {
        return followLinks;
    }

    public void setFollowLinks(boolean followLinks) {
        this.followLinks = followLinks;
    }
}
