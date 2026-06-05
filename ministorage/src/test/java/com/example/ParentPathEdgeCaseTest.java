package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试父路径的边界情况
 */
public class ParentPathEdgeCaseTest {

    @Test
    void testMkdirWhenParentIsFile() {
        // 需求：如果父路径解析后不是目录，忽略该命令
        MemFs fs = new MemFs();
        fs.touch("/file", 10);
        
        // /file是文件，不是目录，MKDIR应该被忽略
        fs.mkdir("/file/subdir");
        
        // file应该还是文件，没有被替换
        assertEquals(10L, fs.info("/file"));
        
        // subdir不应该被创建
        assertNull(fs.info("/file/subdir"));
    }

    @Test
    void testTouchWhenParentIsFile() {
        // 需求：如果父路径解析后不是目录，忽略该命令
        MemFs fs = new MemFs();
        fs.touch("/file", 10);
        
        // /file是文件，不是目录，TOUCH应该被忽略
        fs.touch("/file/newfile", 5);
        
        // file应该还是文件，大小不变
        assertEquals(10L, fs.info("/file"));
        
        // newfile不应该被创建
        assertNull(fs.info("/file/newfile"));
    }

    @Test
    void testLinkWhenParentIsFile() {
        // 需求：如果父路径解析后不是目录，忽略该命令
        MemFs fs = new MemFs();
        fs.touch("/target", 10);
        fs.touch("/file", 5);
        
        // /file是文件，不是目录，LINK应该被忽略
        fs.link("/target", "/file/link");
        
        // file应该还是文件
        assertEquals(5L, fs.info("/file"));
        
        // link不应该被创建
        assertNull(fs.info("/file/link"));
    }

    @Test
    void testRmWhenParentIsFile() {
        // RM命令：需要能定位到目标节点才能删除
        // 如果父路径是文件，则无法定位，应该静默忽略
        MemFs fs = new MemFs();
        fs.touch("/file", 10);
        
        // /file是文件，/file/something无法定位
        fs.rm("/file/something");
        
        // file应该还在
        assertEquals(10L, fs.info("/file"));
    }

    @Test
    void testParentIsLinkToFile() {
        // 如果父路径是链接到文件，解析后仍然是文件，应该忽略
        MemFs fs = new MemFs();
        fs.touch("/realfile", 10);
        fs.link("/realfile", "/linktofile");
        
        // /linktofile链接到文件，MKDIR应该被忽略
        fs.mkdir("/linktofile/subdir");
        
        // linktofile应该还是链接到文件
        assertEquals(10L, fs.info("/linktofile"));
        assertNull(fs.info("/linktofile/subdir"));
    }
}
