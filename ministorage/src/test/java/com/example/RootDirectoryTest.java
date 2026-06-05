package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * 测试根目录的特殊情况
 */
public class RootDirectoryTest {

    @Test
    void testInfoOnEmptyRoot() {
        MemFs fs = new MemFs();
        // 空根目录INFO应该返回0
        assertEquals(0L, fs.info("/"));
    }

    @Test
    void testLsOnEmptyRoot() {
        MemFs fs = new MemFs();
        // 空根目录LS应该返回空列表
        assertTrue(fs.ls("/").isEmpty());
    }

    @Test
    void testFindFromRoot() {
        MemFs fs = new MemFs();
        fs.touch("/file.txt", 10);
        
        // 从根目录查找
        List<String> result = fs.find("/", "file.txt");
        assertEquals(1, result.size());
        assertEquals("/file.txt", result.get(0));
    }

    @Test
    void testFindRootName() {
        MemFs fs = new MemFs();
        // 根目录名称是空字符串
        List<String> result = fs.find("/", "");
        // 应该匹配根目录自己
        assertEquals(1, result.size());
        assertEquals("/", result.get(0));
    }

    @Test
    void testCannotDeleteRoot() {
        MemFs fs = new MemFs();
        fs.rm("/");
        // 根目录不应该被删除，应该还能用
        assertEquals(0L, fs.info("/"));
    }

    @Test
    void testCannotReplaceRootWithMkdir() {
        MemFs fs = new MemFs();
        fs.touch("/file", 5);
        fs.mkdir("/");
        // 根目录不应该被替换，file应该还在
        assertEquals(5L, fs.info("/file"));
    }

    @Test
    void testCannotReplaceRootWithTouch() {
        MemFs fs = new MemFs();
        fs.touch("/file", 5);
        fs.touch("/", 10);
        // 根目录不应该被替换，file应该还在
        assertEquals(5L, fs.info("/file"));
        assertEquals(5L, fs.info("/"));
    }

    @Test
    void testCannotReplaceRootWithLink() {
        MemFs fs = new MemFs();
        fs.touch("/file", 5);
        fs.link("/file", "/");
        // 根目录不应该被替换，应该还是目录
        assertEquals(5L, fs.info("/file"));
        assertTrue(fs.ls("/").contains("file"));
    }
}
