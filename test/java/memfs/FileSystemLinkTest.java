package memfs;

import memfs.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemLinkTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void linkToFile_lsOutputsLinkName() {
        fs.touch("/data.bin", 12);
        fs.link("/data.bin", "/copy");
        // 链接指向文件 -> 输出链接自身名称
        assertEquals("copy", fs.ls("/copy"));
    }

    @Test void linkToFile_infoReturnsTargetSize() {
        fs.touch("/data.bin", 12);
        fs.link("/data.bin", "/copy");
        assertEquals("12", fs.info("/copy"));
    }

    @Test void linkToDirectory_lsOutputsTargetChildren() {
        fs.mkdir("/real");
        fs.touch("/real/a.txt", 10);
        fs.link("/real", "/view");
        assertEquals("a.txt", fs.ls("/view"));
    }

    @Test void linkToDirectory_touchThroughLink() {
        fs.mkdir("/real");
        fs.touch("/real/a.txt", 10);
        fs.link("/real", "/view");
        fs.touch("/view/b.txt", 5);
        // /real 实际包含两个文件
        assertEquals("a.txt\nb.txt", fs.ls("/real"));
    }

    @Test void linkReplacesExistingNode() {
        fs.touch("/x", 5);
        fs.touch("/z", 7);
        fs.link("/x", "/y");
        assertEquals(NodeType.LINK, fs.getRoot().getChild("y").type());
        fs.link("/z", "/y");
        assertEquals("7", fs.info("/y"));
    }

    @Test void linkSrcMustExist() {
        fs.link("/nonexistent", "/copy");
        assertNull(fs.getRoot().getChild("copy"));
    }

    @Test void linkDstParentMustExist() {
        fs.touch("/x", 5);
        fs.link("/x", "/missing/copy");
        assertNull(fs.getRoot().getChild("missing"));
    }

    @Test void linkCannotReplaceRoot() {
        fs.touch("/x", 5);
        fs.link("/x", "/");
        assertEquals(NodeType.DIRECTORY, fs.getRoot().type());
    }

    @Test void lsSpecExample() {
        // 规格示例：LINK /data.bin /copy; LS /; LS /copy
        fs.touch("/data.bin", 12);
        fs.link("/data.bin", "/copy");
        assertEquals("copy\ndata.bin", fs.ls("/"));
        assertEquals("copy", fs.ls("/copy"));
        assertEquals("12", fs.info("/copy"));
    }

    @Test void linkToDirectoryInfoDedup() {
        // 规格示例：INFO 去重
        fs.mkdir("/data");
        fs.touch("/data/a", 10);
        fs.touch("/data/b", 20);
        fs.link("/data", "/alias");
        assertEquals("30", fs.info("/data"));
        assertEquals("30", fs.info("/alias"));
        assertEquals("30", fs.info("/"));  // /data 和 /alias 共享同一底层目录
    }

    @Test void overwriteSemanticExample() {
        // 规格覆盖语义示例
        fs.touch("/x", 5);
        fs.link("/x", "/y");
        assertEquals("5", fs.info("/"));  // /x 和 /y 共享，只计一次

        fs.touch("/y", 30);  // 用新文件替换 /y 这个目录项
        assertEquals("35", fs.info("/"));  // /x=5, /y=30，各自独立

        fs.mkdir("/y");  // 再用空目录替换 /y
        assertEquals("x\ny", fs.ls("/"));
        assertEquals("5", fs.info("/"));  // 只剩 /x 的 5
    }
}
