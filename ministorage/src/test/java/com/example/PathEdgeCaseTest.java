package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试路径处理的边界情况
 */
public class PathEdgeCaseTest {

    @Test
    void testDoubleSlashInMiddle() {
        MemFs fs = new MemFs();
        fs.mkdir("/a");
        // /a//b 应该规范化为 /a/b
        fs.touch("/a//b", 5);
        // 应该能创建成功
        Long size = fs.info("/a/b");
        assertEquals(5L, size);
    }

    @Test
    void testNegativeFileSize() {
        MemFs fs = new MemFs();
        // 负数大小应该被忽略
        fs.touch("/file", -1);
        assertNull(fs.info("/file"));
    }

    @Test
    void testZeroFileSize() {
        MemFs fs = new MemFs();
        // 零大小应该可以创建
        fs.touch("/empty", 0);
        assertEquals(0L, fs.info("/empty"));
    }

    @Test
    void testEmptyRootInfo() {
        MemFs fs = new MemFs();
        // 空根目录大小应该为0
        assertEquals(0L, fs.info("/"));
    }

    @Test
    void testRelativePathIgnored() {
        MemFs fs = new MemFs();
        fs.touch("relative/path", 10);
        // 相对路径应该被忽略
        assertNull(fs.info("/relative"));
    }
}
