package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试通过链接进行各种文件系统操作
 */
public class LinkOperationTest {

    private MemFs fs;

    @BeforeEach
    void setUp() {
        fs = new MemFs();
    }

    @Test
    void testTouchThroughLinkToDirectory() {
        // 通过链接在目录中创建文件
        fs.mkdir("/real");
        fs.link("/real", "/link");
        fs.touch("/link/file.txt", 10);
        
        // 文件应该出现在真实目录中
        assertEquals(1, fs.ls("/real").size());
        assertTrue(fs.ls("/real").contains("file.txt"));
        assertEquals(10L, fs.info("/real"));
    }

    @Test
    void testMkdirThroughLinkToDirectory() {
        // 通过链接在目录中创建子目录
        fs.mkdir("/real");
        fs.link("/real", "/link");
        fs.mkdir("/link/sub");
        
        // 子目录应该出现在真实目录中
        assertTrue(fs.ls("/real").contains("sub"));
    }

    @Test
    void testLinkThroughLinkToDirectory() {
        // 通过链接在目录中创建链接
        fs.mkdir("/real");
        fs.touch("/target", 5);
        fs.link("/real", "/link");
        fs.link("/target", "/link/alias");
        
        // 链接应该出现在真实目录中
        assertTrue(fs.ls("/real").contains("alias"));
        assertEquals(5L, fs.info("/real/alias"));
    }

    @Test
    void testRmThroughLinkToDirectory() {
        // 通过链接删除目录中的文件
        fs.mkdir("/real");
        fs.touch("/real/file.txt", 10);
        fs.link("/real", "/link");
        fs.rm("/link/file.txt");
        
        // 文件应该从真实目录中删除
        assertTrue(fs.ls("/real").isEmpty());
    }

    @Test
    void testLinkChain() {
        // 链接链：lnk2 -> lnk1 -> file
        fs.touch("/file.txt", 7);
        fs.link("/file.txt", "/lnk1");
        fs.link("/lnk1", "/lnk2");
        
        // 通过链接链访问
        assertEquals(7L, fs.info("/lnk2"));
        assertEquals("lnk2", fs.ls("/lnk2").get(0));
    }

    @Test
    void testLinkChainToDirectory() {
        // 链接链到目录：lnk2 -> lnk1 -> dir
        fs.mkdir("/dir");
        fs.touch("/dir/file.txt", 7);
        fs.link("/dir", "/lnk1");
        fs.link("/lnk1", "/lnk2");
        
        // 通过链接链访问目录内容
        assertEquals("file.txt", fs.ls("/lnk2").get(0));
        assertEquals(7L, fs.info("/lnk2"));
        
        // 通过链接链创建文件
        fs.touch("/lnk2/new.txt", 3);
        assertTrue(fs.ls("/dir").contains("new.txt"));
    }

    @Test
    void testRmLinkToDirectory() {
        // 删除指向目录的链接，不应该删除目录
        fs.mkdir("/real");
        fs.touch("/real/file.txt", 5);
        fs.link("/real", "/link");
        
        fs.rm("/link");
        
        // 链接被删除，但目录仍存在
        assertTrue(fs.ls("/").contains("real"));
        assertFalse(fs.ls("/").contains("link"));
        assertEquals(5L, fs.info("/real"));
    }

    @Test
    void testMultipleLinksToSameFile() {
        // 多个链接指向同一个文件
        fs.touch("/file.txt", 10);
        fs.link("/file.txt", "/lnk1");
        fs.link("/file.txt", "/lnk2");
        
        // INFO应该只计算一次
        assertEquals(10L, fs.info("/"));
    }

    @Test
    void testMultipleLinksToSameDirectory() {
        // 多个链接指向同一个目录
        fs.mkdir("/dir");
        fs.touch("/dir/file.txt", 5);
        fs.link("/dir", "/lnk1");
        fs.link("/dir", "/lnk2");
        
        // INFO应该只计算一次
        assertEquals(5L, fs.info("/"));
    }
}
