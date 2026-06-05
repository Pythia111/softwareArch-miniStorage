package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证需求文档中的所有示例
 */
public class RequirementSpecTest {

    private MemFs fs;

    @BeforeEach
    void setUp() {
        fs = new MemFs();
    }

    @Test
    void testSection6_StandardExample() {
        // 需求文档第6节标准示例
        fs.mkdir("/usr");
        fs.mkdir("//usr///local");
        fs.touch("/usr/local/./a.txt", 10);
        fs.touch("/usr/local/../b.txt", 5);
        fs.link("/usr/local", "/alias");

        // LS /
        List<String> ls1 = fs.ls("/");
        assertEquals(2, ls1.size());
        assertEquals("alias", ls1.get(0));
        assertEquals("usr", ls1.get(1));

        // INFO /
        assertEquals(15L, fs.info("/"));

        // FIND / a.txt
        List<String> find1 = fs.find("/", "a.txt");
        assertEquals(1, find1.size());
        assertEquals("/usr/local/a.txt", find1.get(0));

        // RM /alias
        fs.rm("/alias");

        // LS /
        List<String> ls2 = fs.ls("/");
        assertEquals(1, ls2.size());
        assertEquals("usr", ls2.get(0));
    }

    @Test
    void testLinkExample1() {
        // 需求文档 LINK示例1
        fs.touch("/data.bin", 12);
        fs.link("/data.bin", "/copy");

        List<String> ls1 = fs.ls("/");
        assertEquals(2, ls1.size());
        assertTrue(ls1.contains("copy"));
        assertTrue(ls1.contains("data.bin"));

        List<String> ls2 = fs.ls("/copy");
        assertEquals(1, ls2.size());
        assertEquals("copy", ls2.get(0));

        assertEquals(12L, fs.info("/copy"));
    }

    @Test
    void testLinkExample2() {
        // 需求文档 LINK示例2
        fs.mkdir("/real");
        fs.touch("/real/a.txt", 10);
        fs.link("/real", "/view");

        List<String> ls1 = fs.ls("/view");
        assertEquals(1, ls1.size());
        assertEquals("a.txt", ls1.get(0));

        fs.touch("/view/b.txt", 5);

        List<String> ls2 = fs.ls("/real");
        assertEquals(2, ls2.size());
        assertTrue(ls2.contains("a.txt"));
        assertTrue(ls2.contains("b.txt"));

        assertEquals(15L, fs.info("/view"));
    }

    @Test
    void testInfoDedup() {
        // 需求文档 INFO示例
        fs.mkdir("/data");
        fs.touch("/data/a", 10);
        fs.touch("/data/b", 20);
        fs.link("/data", "/alias");

        assertEquals(30L, fs.info("/data"));
        assertEquals(30L, fs.info("/alias"));
        assertEquals(30L, fs.info("/"));
    }

    @Test
    void testOverwriteSemantics() {
        // 需求文档覆盖语义示例
        fs.touch("/x", 5);
        fs.link("/x", "/y");
        assertEquals(5L, fs.info("/"));

        fs.touch("/y", 30);
        assertEquals(35L, fs.info("/"));

        fs.mkdir("/y");
        List<String> ls = fs.ls("/");
        assertTrue(ls.contains("x"));
        assertTrue(ls.contains("y"));
        assertEquals(5L, fs.info("/"));
    }

    @Test
    void testRmExample1() {
        // 需求文档 RM示例1
        fs.mkdir("/tmp");
        fs.touch("/tmp/a.bin", 7);

        List<String> ls1 = fs.ls("/tmp");
        assertEquals(1, ls1.size());
        assertEquals("a.bin", ls1.get(0));

        fs.rm("/tmp/a.bin");
        assertTrue(fs.ls("/tmp").isEmpty());

        fs.rm("/tmp");
        assertTrue(fs.ls("/").isEmpty());
    }

    @Test
    void testRmExample2() {
        // 需求文档 RM示例2
        fs.mkdir("/tmp");
        fs.mkdir("/tmp/cache");
        fs.touch("/tmp/cache/a.bin", 7);

        fs.rm("/tmp/cache"); // 非空，应该被忽略

        List<String> ls = fs.ls("/tmp");
        assertEquals(1, ls.size());
        assertEquals("cache", ls.get(0));
    }

    @Test
    void testFindExample1() {
        // 需求文档 FIND示例1
        fs.mkdir("/src");
        fs.mkdir("/src/main");
        fs.mkdir("/src/test");
        fs.touch("/src/main/App.java", 10);
        fs.touch("/src/test/App.java", 20);
        fs.touch("/src/test/Helper.java", 5);

        List<String> result = fs.find("/src", "App.java");
        assertEquals(2, result.size());
        assertEquals("/src/main/App.java", result.get(0));
        assertEquals("/src/test/App.java", result.get(1));
    }

    @Test
    void testFindExample2() {
        // 需求文档 FIND示例2
        fs.touch("/readme.md", 50);

        List<String> result1 = fs.find("/readme.md", "readme.md");
        assertEquals(1, result1.size());
        assertEquals("/readme.md", result1.get(0));

        List<String> result2 = fs.find("/readme.md", "other.md");
        assertTrue(result2.isEmpty());
    }

    @Test
    void testPathNormalization() {
        // 需求文档路径规范化示例
        fs.mkdir("/usr");
        fs.mkdir("//usr///local");
        fs.touch("/usr/local/./a.txt", 10);
        fs.touch("/usr/local/../b.txt", 5);

        List<String> ls = fs.ls("/usr//local/");
        assertEquals(1, ls.size());
        assertEquals("a.txt", ls.get(0));

        assertEquals(15L, fs.info("/usr/./local/../"));
    }
}
