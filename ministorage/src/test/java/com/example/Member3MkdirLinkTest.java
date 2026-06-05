package com.example;

import com.example.fs.Directory;
import com.example.fs.File;
import com.example.fs.Link;
import com.example.fs.Node;
import com.example.fs.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 成员3 (cyg) 单元测试：MKDIR命令 + LINK命令
 *
 * 覆盖范围：
 * - MKDIR：基本创建、嵌套创建、父目录缺失、已有目录不变、覆盖文件/链接、路径规范化
 * - LINK：链接到文件/目录、源必须存在、目标父目录必须存在、不可替换根、覆盖语义
 * - MKDIR+LINK 与 PathUtil 的集成（路径中含 . / .. / 多余斜杠）
 * - 覆盖语义综合场景（规格文档示例）
 */
public class Member3MkdirLinkTest {

    private MemFs memFs;
    private Directory root;

    @BeforeEach
    public void setUp() throws Exception {
        memFs = new MemFs();
        Field rootField = MemFs.class.getDeclaredField("root");
        rootField.setAccessible(true);
        root = (Directory) rootField.get(memFs);
    }

    // ================================================================
    //  MKDIR — 基本创建
    // ================================================================

    @Test
    public void testMkdirCreatesDirectoryUnderRoot() {
        memFs.mkdir("/usr");
        Node node = root.getChild("usr");
        assertNotNull(node, "应在根目录下创建 usr 目录");
        assertEquals(NodeType.DIRECTORY, node.type());
    }

    @Test
    public void testMkdirNestedCreatesChildDirectory() {
        memFs.mkdir("/usr");
        memFs.mkdir("/usr/local");
        Directory usr = (Directory) root.getChild("usr");
        assertNotNull(usr.getChild("local"), "应在 /usr 下创建 local 目录");
        assertEquals(NodeType.DIRECTORY, usr.getChild("local").type());
    }

    @Test
    public void testMkdirDeeplyNested() {
        memFs.mkdir("/a");
        memFs.mkdir("/a/b");
        memFs.mkdir("/a/b/c");
        memFs.mkdir("/a/b/c/d");
        Directory a = (Directory) root.getChild("a");
        Directory b = (Directory) a.getChild("b");
        Directory c = (Directory) b.getChild("c");
        assertNotNull(c.getChild("d"));
        assertEquals(NodeType.DIRECTORY, c.getChild("d").type());
    }

    // ================================================================
    //  MKDIR — 父目录缺失时静默忽略
    // ================================================================

    @Test
    public void testMkdirIgnoredWhenParentMissing() {
        memFs.mkdir("/a/b/c");
        assertNull(root.getChild("a"), "父目录不存在时应静默忽略，不创建任何节点");
    }

    @Test
    public void testMkdirIgnoredWhenParentIsFile() {
        memFs.touch("/file.txt", 10);
        memFs.mkdir("/file.txt/subdir");
        Node file = root.getChild("file.txt");
        assertEquals(NodeType.FILE, file.type(), "父节点是文件时不应创建子目录");
    }

    // ================================================================
    //  MKDIR — 已有目录不变（幂等性）
    // ================================================================

    @Test
    public void testMkdirOnExistingDirectoryIsNoOp() {
        memFs.mkdir("/usr");
        memFs.mkdir("/usr");
        Node node = root.getChild("usr");
        assertNotNull(node);
        assertEquals(NodeType.DIRECTORY, node.type());
    }

    @Test
    public void testMkdirOnExistingDirectoryPreservesChildren() {
        memFs.mkdir("/usr");
        memFs.touch("/usr/file.txt", 10);
        memFs.mkdir("/usr");
        Directory usr = (Directory) root.getChild("usr");
        assertNotNull(usr.getChild("file.txt"), "重复 mkdir 不应丢失子节点");
    }

    // ================================================================
    //  MKDIR — 覆盖语义（替换文件和链接）
    // ================================================================

    @Test
    public void testMkdirReplacesFileWithDirectory() {
        memFs.touch("/foo", 10);
        assertEquals(NodeType.FILE, root.getChild("foo").type());

        memFs.mkdir("/foo");
        assertEquals(NodeType.DIRECTORY, root.getChild("foo").type(), "mkdir 应覆盖同名文件");
    }

    @Test
    public void testMkdirReplacesLinkWithDirectory() {
        memFs.touch("/target", 5);
        memFs.link("/target", "/mylink");
        assertEquals(NodeType.LINK, root.getChild("mylink").type());

        memFs.mkdir("/mylink");
        assertEquals(NodeType.DIRECTORY, root.getChild("mylink").type(), "mkdir 应覆盖同名链接");
    }

    // ================================================================
    //  MKDIR — 路径规范化
    // ================================================================

    @Test
    public void testMkdirNormalizesDoubleSlash() {
        memFs.mkdir("/usr");
        memFs.mkdir("//usr///local");
        Directory usr = (Directory) root.getChild("usr");
        assertNotNull(usr.getChild("local"), "多余斜杠应被规范化");
    }

    @Test
    public void testMkdirNormalizesTrailingSlash() {
        memFs.mkdir("/tmp/");
        assertNotNull(root.getChild("tmp"), "尾随斜杠应被规范化");
    }

    @Test
    public void testMkdirNormalizesDot() {
        memFs.mkdir("/usr");
        memFs.mkdir("/./usr/./local");
        Directory usr = (Directory) root.getChild("usr");
        assertNotNull(usr.getChild("local"), ". 段应被规范化");
    }

    @Test
    public void testMkdirNormalizesDotDot() {
        memFs.mkdir("/usr");
        memFs.mkdir("/usr/local/../bin");
        Directory usr = (Directory) root.getChild("usr");
        assertNotNull(usr.getChild("bin"), ".. 段应被规范化");
        assertNull(usr.getChild("local"), ".. 应回退，local 不应被创建");
    }

    @Test
    public void testMkdirNormalizesComplexPath() {
        memFs.mkdir("/a");
        memFs.mkdir("/a/b");
        memFs.mkdir("/a//b/./c/../d");
        Directory a = (Directory) root.getChild("a");
        Directory b = (Directory) a.getChild("b");
        assertNotNull(b, "/a/b 应已存在");
        assertNotNull(b.getChild("d"), "复合路径 /a//b/./c/../d 应规范化为 /a/b/d");
        assertNull(b.getChild("c"), "c 不应存在");
    }

    // ================================================================
    //  MKDIR — 边界情况
    // ================================================================

    @Test
    public void testMkdirRootIsNoOp() {
        memFs.mkdir("/");
        // 根目录始终存在，不应抛异常
        assertEquals(NodeType.DIRECTORY, root.type());
    }

    @Test
    public void testMkdirNullPathIsIgnored() {
        memFs.mkdir(null);
        assertTrue(root.listChildren().isEmpty(), "null 路径应被静默忽略");
    }

    @Test
    public void testMkdirRelativePathIsIgnored() {
        memFs.mkdir("usr/local");
        assertTrue(root.listChildren().isEmpty(), "相对路径应被静默忽略");
    }

    @Test
    public void testMkdirDotDotAboveRoot() {
        memFs.mkdir("/../foo");
        // .. 超过根目录时回到根，等价于 mkdir /foo
        assertNotNull(root.getChild("foo"), "/../foo 应等价于 /foo");
    }

    // ================================================================
    //  LINK — 链接到文件
    // ================================================================

    @Test
    public void testLinkToFile() {
        memFs.touch("/data.bin", 12);
        memFs.link("/data.bin", "/copy");

        Node copy = root.getChild("copy");
        assertNotNull(copy, "应创建链接节点");
        assertEquals(NodeType.LINK, copy.type());
    }

    @Test
    public void testLinkToFileLsReturnsLinkName() {
        memFs.touch("/data.bin", 12);
        memFs.link("/data.bin", "/copy");

        List<String> result = memFs.ls("/copy");
        assertEquals(1, result.size());
        assertEquals("copy", result.get(0), "链接到文件时 LS 应返回链接自身名称");
    }

    @Test
    public void testLinkToFileInfoReturnsTargetSize() {
        memFs.touch("/data.bin", 12);
        memFs.link("/data.bin", "/copy");

        assertEquals(12L, memFs.info("/copy"), "链接的 info 应返回目标文件大小");
    }

    // ================================================================
    //  LINK — 链接到目录
    // ================================================================

    @Test
    public void testLinkToDirectory() {
        memFs.mkdir("/real");
        memFs.touch("/real/a.txt", 10);
        memFs.link("/real", "/view");

        Node view = root.getChild("view");
        assertNotNull(view);
        assertEquals(NodeType.LINK, view.type());
    }

    @Test
    public void testLinkToDirectoryLsReturnsTargetChildren() {
        memFs.mkdir("/real");
        memFs.touch("/real/a.txt", 10);
        memFs.touch("/real/b.txt", 20);
        memFs.link("/real", "/view");

        List<String> result = memFs.ls("/view");
        assertEquals(2, result.size());
        assertEquals("a.txt", result.get(0));
        assertEquals("b.txt", result.get(1));
    }

    @Test
    public void testLinkToDirectoryTouchThroughLink() {
        memFs.mkdir("/real");
        memFs.touch("/real/a.txt", 10);
        memFs.link("/real", "/view");

        // NodeResolver 不跟踪链接路径，touch 通过链接路径会静默忽略
        memFs.touch("/view/b.txt", 5);
        List<String> realChildren = memFs.ls("/real");
        assertEquals(1, realChildren.size(), "touch 通过链接路径不会跟踪链接，仅原路径可操作");
        assertTrue(realChildren.contains("a.txt"));

        // 但 ls 和 info 可以通过链接路径正常工作（MemFs.ls/info 使用 resolveLink）
        List<String> viewChildren = memFs.ls("/view");
        assertEquals(realChildren, viewChildren, "ls 通过链接路径应返回目标目录的子节点");
    }

    @Test
    public void testLinkToDirectoryInfoDedup() {
        memFs.mkdir("/data");
        memFs.touch("/data/a", 10);
        memFs.touch("/data/b", 20);
        memFs.link("/data", "/alias");

        assertEquals(30L, memFs.info("/data"));
        assertEquals(30L, memFs.info("/alias"));
        assertEquals(30L, memFs.info("/"), "根目录 info 应去重，/data 和 /alias 共享同一底层目录");
    }

    // ================================================================
    //  LINK — 源必须存在
    // ================================================================

    @Test
    public void testLinkSrcMustExist() {
        memFs.link("/nonexistent", "/copy");
        assertNull(root.getChild("copy"), "源路径不存在时不应创建链接");
    }

    @Test
    public void testLinkSrcNonExistentNestedPath() {
        memFs.link("/a/b/c", "/link");
        assertNull(root.getChild("link"), "嵌套不存在的源路径不应创建链接");
    }

    // ================================================================
    //  LINK — 目标父目录必须存在
    // ================================================================

    @Test
    public void testLinkDstParentMustExist() {
        memFs.touch("/x", 5);
        memFs.link("/x", "/missing/copy");
        assertNull(root.getChild("missing"), "目标父目录不存在时不应创建任何节点");
    }

    @Test
    public void testLinkDstParentIsFile() {
        memFs.touch("/x", 5);
        memFs.touch("/parent", 10);
        memFs.link("/x", "/parent/child");
        // 父节点是文件，不是目录，应静默忽略
        assertEquals(NodeType.FILE, root.getChild("parent").type());
    }

    // ================================================================
    //  LINK — 不可替换根
    // ================================================================

    @Test
    public void testLinkCannotReplaceRoot() {
        memFs.touch("/x", 5);
        memFs.link("/x", "/");
        assertEquals(NodeType.DIRECTORY, root.type(), "链接不应替换根目录");
    }

    // ================================================================
    //  LINK — 覆盖语义
    // ================================================================

    @Test
    public void testLinkReplacesExistingFile() {
        memFs.touch("/target", 5);
        memFs.touch("/existing", 99);
        memFs.link("/target", "/existing");

        Node node = root.getChild("existing");
        assertEquals(NodeType.LINK, node.type(), "link 应覆盖同名文件");
    }

    @Test
    public void testLinkReplacesExistingDirectory() {
        memFs.mkdir("/target");
        memFs.mkdir("/existing");
        memFs.link("/target", "/existing");

        Node node = root.getChild("existing");
        assertEquals(NodeType.LINK, node.type(), "link 应覆盖同名空目录");
    }

    @Test
    public void testLinkReplacesExistingLink() {
        memFs.touch("/x", 5);
        memFs.touch("/z", 7);
        memFs.link("/x", "/y");
        assertEquals(5L, memFs.info("/y"));

        memFs.link("/z", "/y");
        assertEquals(7L, memFs.info("/y"), "新链接应覆盖旧链接");
    }

    // ================================================================
    //  LINK — 路径规范化
    // ================================================================

    @Test
    public void testLinkNormalizesSrcPath() {
        memFs.touch("/data.bin", 12);
        memFs.link("//data.bin", "/copy");
        assertNotNull(root.getChild("copy"), "源路径多余斜杠应被规范化");
    }

    @Test
    public void testLinkNormalizesDstPath() {
        memFs.touch("/data.bin", 12);
        memFs.link("/data.bin", "//copy");
        assertNotNull(root.getChild("copy"), "目标路径多余斜杠应被规范化");
    }

    @Test
    public void testLinkNormalizesDotDot() {
        memFs.mkdir("/dir");
        memFs.touch("/file.txt", 10);
        memFs.link("/file.txt", "/dir/../link.txt");
        assertNotNull(root.getChild("link.txt"), "目标路径 .. 应被规范化");
    }

    // ================================================================
    //  LINK — 边界情况
    // ================================================================

    @Test
    public void testLinkNullSrcIsIgnored() {
        memFs.link(null, "/copy");
        assertNull(root.getChild("copy"));
    }

    @Test
    public void testLinkNullDstIsIgnored() {
        memFs.touch("/x", 5);
        memFs.link("/x", null);
        // 不应抛异常
        assertEquals(1, root.listChildren().size());
    }

    @Test
    public void testLinkRelativeSrcIsIgnored() {
        memFs.link("relative/path", "/copy");
        assertNull(root.getChild("copy"), "相对路径源应被静默忽略");
    }

    @Test
    public void testLinkRelativeDstIsIgnored() {
        memFs.touch("/x", 5);
        memFs.link("/x", "relative/copy");
        assertNull(root.getChild("copy"), "相对路径目标应被静默忽略");
    }

    // ================================================================
    //  LINK — 链接到链接（间接链接）
    // ================================================================

    @Test
    public void testLinkToLink() {
        memFs.touch("/original", 100);
        memFs.link("/original", "/link1");
        memFs.link("/link1", "/link2");

        // link2 -> link1，resolveLink 只解析一层
        Node link2 = root.getChild("link2");
        assertNotNull(link2);
        assertEquals(NodeType.LINK, link2.type());
    }

    // ================================================================
    //  MKDIR + LINK 综合场景（规格文档示例）
    // ================================================================

    @Test
    public void testSpecLsExample() {
        // LINK /data.bin /copy; LS /; LS /copy
        memFs.touch("/data.bin", 12);
        memFs.link("/data.bin", "/copy");

        List<String> rootLs = memFs.ls("/");
        assertEquals(2, rootLs.size());
        assertEquals("copy", rootLs.get(0));
        assertEquals("data.bin", rootLs.get(1));

        List<String> copyLs = memFs.ls("/copy");
        assertEquals(1, copyLs.size());
        assertEquals("copy", copyLs.get(0));

        assertEquals(12L, memFs.info("/copy"));
    }

    @Test
    public void testSpecOverwriteSemanticExample() {
        // 规格覆盖语义示例
        memFs.touch("/x", 5);
        memFs.link("/x", "/y");
        assertEquals(5L, memFs.info("/"), "/x 和 /y 共享，只计一次");

        memFs.touch("/y", 30);  // 用新文件替换 /y
        assertEquals(35L, memFs.info("/"), "/x=5, /y=30，各自独立");

        memFs.mkdir("/y");  // 用空目录替换 /y
        List<String> ls = memFs.ls("/");
        assertEquals(2, ls.size());
        assertTrue(ls.contains("x"));
        assertTrue(ls.contains("y"));
        assertEquals(5L, memFs.info("/"), "只剩 /x 的 5");
    }

    @Test
    public void testMkdirAndLinkCollaboration() {
        // mkdir 创建目录，link 创建链接，验证 ls 和 info 协作
        memFs.mkdir("/data");
        memFs.touch("/data/a.txt", 100);
        memFs.touch("/data/b.txt", 200);
        memFs.link("/data", "/alias");

        // 通过链接 ls 看到同样的子节点
        List<String> aliasLs = memFs.ls("/alias");
        List<String> dataLs = memFs.ls("/data");
        assertEquals(dataLs, aliasLs);

        // info 去重
        assertEquals(300L, memFs.info("/data"));
        assertEquals(300L, memFs.info("/alias"));
        assertEquals(300L, memFs.info("/"));
    }

    @Test
    public void testMkdirLinkMkdirOverwriteSequence() {
        // 测试 mkdir -> link -> mkdir 覆盖链
        memFs.mkdir("/dir");
        memFs.touch("/dir/file.txt", 50);
        memFs.link("/dir", "/sym");

        // mkdir 覆盖链接
        memFs.mkdir("/sym");
        Node sym = root.getChild("sym");
        assertEquals(NodeType.DIRECTORY, sym.type());
        assertTrue(((Directory) sym).listChildren().isEmpty(), "新建的空目录不应有子节点");

        // 原目录不受影响
        Directory dir = (Directory) root.getChild("dir");
        assertNotNull(dir.getChild("file.txt"));
    }

    @Test
    public void testLinkToDirectoryCreatedByMkdir() {
        memFs.mkdir("/src");
        memFs.mkdir("/src/main");
        memFs.touch("/src/main/App.java", 100);
        memFs.link("/src", "/src_link");

        // 通过链接 find 子节点
        List<String> found = memFs.find("/src_link", "App.java");
        assertFalse(found.isEmpty(), "应通过链接找到目标目录子树中的文件");
    }

    @Test
    public void testMkdirOnLinkToDirectoryPath() {
        // NodeResolver 不跟踪链接路径，mkdir 通过链接路径会静默忽略
        memFs.mkdir("/real");
        memFs.link("/real", "/view");
        memFs.mkdir("/view/subdir");

        // mkdir 无法通过链接路径操作
        Directory real = (Directory) root.getChild("real");
        assertNull(real.getChild("subdir"), "mkdir 不跟踪链接路径，subdir 不应被创建");
    }

    @Test
    public void testMultipleLinksAndMkdir() {
        memFs.mkdir("/shared");
        memFs.touch("/shared/data.txt", 50);
        memFs.link("/shared", "/link1");
        memFs.link("/shared", "/link2");

        // 所有链接和原目录共享同一底层目录
        assertEquals(50L, memFs.info("/shared"));
        assertEquals(50L, memFs.info("/link1"));
        assertEquals(50L, memFs.info("/link2"));
        assertEquals(50L, memFs.info("/"), "根目录 info 去重，只计算一次 /shared 的内容");
    }
}
