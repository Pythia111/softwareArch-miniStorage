package com.example.fs;

import java.util.HashSet;
import java.util.Set;

public class SizeContext {
    private Set<Node> visited = new HashSet<>();

    public boolean isVisited(Node node) {
        return visited.contains(node);
    }

    public void markVisited(Node node) {
        visited.add(node);
    }
}
