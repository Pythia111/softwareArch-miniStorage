package com.example.fs;

public abstract class Node {
    protected String name;

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract NodeType type();

    public boolean isFile() { 
        return this instanceof File; 
    }
    
    public boolean isDirectory() { 
        return this instanceof Directory; 
    }
    
    public boolean isLink() { 
        return this instanceof Link; 
    }

    public final long size() {
        return size(new SizeContext());
    }

    public abstract long size(SizeContext ctx);
}
