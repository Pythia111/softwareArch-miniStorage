package com.example;

import com.example.fs.Directory;
import com.example.fs.File;
import com.example.fs.Link;
import com.example.fs.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 成员2 (cx) 单元测试：LS命令 + FIND命令
 * 测试LS对文件、目录、链接的处理
 * 测试FIND的递归搜索和字典序排序
 * 测试FIND的防重复展开(链接到同一目录)
 */
public class Member2Test {

    private MemFs memFs;
    private Directory root;

    @BeforeEach
    public void setUp() throws Exception {
        memFs = new MemFs();
        Field rootField = MemFs.class.getDeclaredField("root");
        rootField.setAccessible(true);
        root = (Directory) rootField.get(memFs);
    }

    // ==================== LS 测试 ====================

    @Test
    public void testLsRootEmpty() {
        List<String> result = memFs.ls("/");
        assertTrue(result.isEmpty(), "空根目录应该返回空列表");
    }

    @Test
    public void testLsRootWithMixedChildren() {
        memFs.mkdir("/usr");
        memFs.touch("/readme.md", 50);
        List<String> result = memFs.ls("/");
        assertEquals(2, result.size());
        assertEquals("readme.md", result.get(0));
        assertEquals("usr", result.get(1));
    }

    @Test
    public void testLsDirectoryWithChildren() {
        memFs.mkdir("/dir");
        memFs.touch("/dir/file1", 10);
        memFs.touch("/dir/file2", 20);
        memFs.mkdir("/dir/subdir");
        List<String> result = memFs.ls("/dir");
        assertEquals(3, result.size());
        assertEquals("file1", result.get(0));
        assertEquals("file2", result.get(1));
        assertEquals("subdir", result.get(2));
    }

    @Test
    public void testLsEmptyDirectory() {
        memFs.mkdir("/empty");
        List<String> result = memFs.ls("/empty");
        assertTrue(result.isEmpty(), "空目录应该返回空列表");
    }

    @Test
    public void testLsOnFile() {
        memFs.touch("/readme.md", 50);
        List<String> result = memFs.ls("/readme.md");
        assertEquals(1, result.size());
        assertEquals("readme.md", result.get(0));
    }

    @Test
    public void testLsNonExistentPath() {
        List<String> result = memFs.ls("/non/existent");
        assertTrue(result.isEmpty(), "不存在的路径应该返回空列表");
    }

    @Test
    public void testLsLinkToFile() {
        // LS对链接到文件的处理：返回链接自身名称
        memFs.touch("/data.bin", 12);
        root.putChild("copy", new Link("copy", root.getChild("data.bin")));
        List<String> result = memFs.ls("/copy");
        assertEquals(1, result.size());
        assertEquals("copy", result.get(0), "链接到文件时，应返回链接自身名称");
    }

    @Test
    public void testLsLinkToDirectory() {
        // LS对链接到目录的处理：返回目标目录的子节点列表
        Directory real = new Directory("real");
        root.putChild("real", real);
        real.putChild("a.txt", new File("a.txt", 10));
        real.putChild("b.txt", new File("b.txt", 20));

        root.putChild("view", new Link("view", real));

        List<String> result = memFs.ls("/view");
        assertEquals(2, result.size());
        assertEquals("a.txt", result.get(0));
        assertEquals("b.txt", result.get(1));
    }

    @Test
    public void testLsAlphabeticalOrder() {
        memFs.touch("/z.txt", 1);
        memFs.touch("/a.txt", 1);
        memFs.touch("/m.txt", 1);
        List<String> result = memFs.ls("/");
        assertEquals(3, result.size());
        assertEquals("a.txt", result.get(0));
        assertEquals("m.txt", result.get(1));
        assertEquals("z.txt", result.get(2));
    }

    // ==================== FIND 测试 ====================

    @Test
    public void testFindInSubtree() {
        // 需求文档示例1
        memFs.mkdir("/src");
        memFs.mkdir("/src/main");
        memFs.mkdir("/src/test");
        memFs.touch("/src/main/App.java", 10);
        memFs.touch("/src/test/App.java", 20);
        memFs.touch("/src/test/Helper.java", 5);

        List<String> result = memFs.find("/src", "App.java");
        assertEquals(2, result.size());
        assertEquals("/src/main/App.java", result.get(0));
        assertEquals("/src/test/App.java", result.get(1));
    }

    @Test
    public void testFindFromFileStartPoint_Match() {
        // 需求文档示例2：起点是文件，且名称匹配
        memFs.touch("/readme.md", 50);
        List<String> result = memFs.find("/readme.md", "readme.md");
        assertEquals(1, result.size());
        assertEquals("/readme.md", result.get(0));
    }

    @Test
    public void testFindFromFileStartPoint_NoMatch() {
        // 起点是文件，名称不匹配
        memFs.touch("/readme.md", 50);
        List<String> result = memFs.find("/readme.md", "other.md");
        assertTrue(result.isEmpty(), "不匹配时应返回空列表");
    }

    @Test
    public void testFindNoMatch() {
        memFs.mkdir("/a");
        List<String> result = memFs.find("/a", "nope.txt");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindNonExistentPath() {
        List<String> result = memFs.find("/non/existent", "anything");
        assertTrue(result.isEmpty(), "不存在的路径应返回空列表");
    }

    @Test
    public void testFindLinkNodeItself() {
        // 链接节点本身也可以作为匹配结果，匹配的是链接名称
        memFs.touch("/data.bin", 10);
        root.putChild("copy", new Link("copy", root.getChild("data.bin")));

        List<String> result = memFs.find("/", "copy");
        assertEquals(1, result.size());
        assertEquals("/copy", result.get(0));
    }

    @Test
    public void testFindFromLinkStartPoint() {
        // 起点是链接到目录时，递归搜索目标目录子树
        Directory real = new Directory("real");
        root.putChild("real", real);
        real.putChild("file.txt", new File("file.txt", 5));

        root.putChild("alias", new Link("alias", real));

        List<String> result = memFs.find("/alias", "file.txt");
        assertEquals(1, result.size());
        assertEquals("/alias/file.txt", result.get(0));
    }

    @Test
    public void testFindLinkToDirectoryDuringTraversal() {
        // 搜索过程中遇到链接到目录的节点，进入链接指向的目录继续搜索
        memFs.mkdir("/usr");
        memFs.mkdir("/usr/local");
        memFs.touch("/usr/local/a.txt", 10);
        memFs.touch("/usr/b.txt", 5);
        root.putChild("alias", new Link("alias", root.getChild("usr").isDirectory()
                ? ((Directory) root.getChild("usr")).getChild("local") : null));

        List<String> result = memFs.find("/", "a.txt");
        // alias < usr alphabetically, link is followed, so /alias/a.txt should appear
        assertFalse(result.isEmpty(), "应该找到a.txt");
        // Either /alias/a.txt or /usr/local/a.txt (or both depending on dedup)
        assertTrue(result.contains("/usr/local/a.txt") || result.contains("/alias/a.txt"));
    }

    @Test
    public void testFindAlphabeticalOrder() {
        memFs.mkdir("/b");
        memFs.mkdir("/a");
        memFs.touch("/b/f.txt", 1);
        memFs.touch("/a/f.txt", 1);

        List<String> result = memFs.find("/", "f.txt");
        assertEquals(2, result.size());
        assertEquals("/a/f.txt", result.get(0));
        assertEquals("/b/f.txt", result.get(1));
    }

    @Test
    public void testFindDeeplyNested() {
        memFs.mkdir("/a");
        memFs.mkdir("/a/b");
        memFs.mkdir("/a/b/c");
        memFs.mkdir("/a/b/c/d");
        memFs.touch("/a/b/c/d/target.txt", 10);

        List<String> result = memFs.find("/a", "target.txt");
        assertEquals(1, result.size());
        assertEquals("/a/b/c/d/target.txt", result.get(0));
    }

    @Test
    public void testFindMultipleMatchesAtDifferentDepths() {
        memFs.mkdir("/root");
        memFs.touch("/root/same.txt", 10);
        memFs.mkdir("/root/sub");
        memFs.touch("/root/sub/same.txt", 20);
        memFs.mkdir("/root/sub/deep");
        memFs.touch("/root/sub/deep/same.txt", 30);

        List<String> result = memFs.find("/root", "same.txt");
        assertEquals(3, result.size());
        assertEquals("/root/same.txt", result.get(0));
        assertEquals("/root/sub/deep/same.txt", result.get(1));
        assertEquals("/root/sub/same.txt", result.get(2));
    }

    // ==================== FIND 防重复展开测试 ====================

    @Test
    public void testFindDedupTwoLinksToSameDir() {
        // 遍历时会跟随链接：container下有两个链接，FIND会进入链接指向的目录
        // 但同一底层目录只展开一次
        Directory real = new Directory("real");
        root.putChild("real", real);
        real.putChild("file.txt", new File("file.txt", 1));

        Directory container = new Directory("container");
        root.putChild("container", container);
        container.putChild("lnk1", new Link("lnk1", real));
        container.putChild("lnk2", new Link("lnk2", real));

        List<String> result = memFs.find("/container", "file.txt");
        // 遍历时跟随链接，lnk1和lnk2都指向same real目录
        // 按字典序处理：lnk1先，找到/container/lnk1/file.txt，标记real为已访问
        // lnk2后处理，real已访问，不重复展开
        assertEquals(1, result.size(), "应该通过lnk1找到file.txt一次");
        assertEquals("/container/lnk1/file.txt", result.get(0));
    }

    @Test
    public void testFindThroughSameLinkMultipleTimesPrevented() {
        // 遍历时会跟随链接：linkA和linkB是链接节点，会被展开
        // 但优先处理非链接子节点，且同一底层目录只展开一次
        Directory real = new Directory("real");
        root.putChild("real", real);
        real.putChild("f.txt", new File("f.txt", 1));

        root.putChild("linkA", new Link("linkA", real));
        root.putChild("linkB", new Link("linkB", real));

        List<String> result = memFs.find("/", "f.txt");
        // linkA < linkB < real 按字典序，但优先处理非链接
        // 处理顺序：real(非链接)先，找到/real/f.txt，标记real已访问
        // 然后处理linkA和linkB(链接)，但real已访问，不重复展开
        assertEquals(1, result.size(), "应该只找到/real/f.txt一次");
        assertEquals("/real/f.txt", result.get(0));
    }
}