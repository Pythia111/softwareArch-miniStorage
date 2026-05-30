package com.example.fs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Member1Test {
    
    @Test
    public void testLinkCreationAndGetTarget() {
        File targetFile = new File("target", 100);
        Link link = new Link("mylink", targetFile);
        
        assertEquals("mylink", link.getName());
        assertEquals(targetFile, link.getTarget());
        assertTrue(link.isLink());
        assertFalse(link.isFile());
        assertFalse(link.isDirectory());
        assertEquals(NodeType.LINK, link.type());
    }
    
    @Test
    public void testNodeTypes() {
        File f = new File("f", 10);
        Directory d = new Directory("d");
        assertTrue(f.isFile());
        assertTrue(d.isDirectory());
        assertEquals(NodeType.FILE, f.type());
        assertEquals(NodeType.DIRECTORY, d.type());
    }
    
    @Test
    public void testFileSetSize() {
        File f = new File("f", 10);
        assertEquals(10, f.size());
        f.setSize(20);
        assertEquals(20, f.size());
    }
    
    @Test
    public void testSizeContextVisited() {
        SizeContext ctx = new SizeContext();
        Node dummy = new File("dummy", 10);
        
        assertFalse(ctx.isVisited(dummy));
        ctx.markVisited(dummy);
        assertTrue(ctx.isVisited(dummy));
    }
    
    @Test
    public void testDirectorySizeWithMultipleLinksToSameFile() {
        Directory root = new Directory("/");
        File sharedFile = new File("shared", 50);
        
        root.putChild("f1", sharedFile);
        root.putChild("link1", new Link("link1", sharedFile));
        root.putChild("link2", new Link("link2", sharedFile));
        
        // 由于 File sharedFile 只应被累加一次：50
        assertEquals(50, root.size(new SizeContext()));
    }
    
    @Test
    public void testDirectorySizeWithLinkDeduplication() {
        Directory root = new Directory("/");
        Directory data = new Directory("data");
        File a = new File("a", 10);
        File b = new File("b", 20);
        
        data.putChild("a", a);
        data.putChild("b", b);
        
        Link alias = new Link("alias", data);
        
        root.putChild("data", data);
        root.putChild("alias", alias);
        
        // 期望大小: a(10) + b(20) = 30
        assertEquals(30, root.size(new SizeContext()));
        assertEquals(30, alias.size(new SizeContext()));
    }
    
    @Test
    public void testDirectorySizeEmpty() {
        Directory root = new Directory("/");
        assertEquals(0, root.size(new SizeContext()));
    }
}
