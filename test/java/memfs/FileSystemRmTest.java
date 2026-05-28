package memfs;

import memfs.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemRmTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void rmFile() {
        fs.touch("/a.txt", 10);
        fs.rm("/a.txt");
        assertNull(fs.getRoot().getChild("a.txt"));
    }

    @Test void rmEmptyDirectory() {
        fs.mkdir("/tmp");
        fs.rm("/tmp");
        assertNull(fs.getRoot().getChild("tmp"));
    }

    @Test void rmNonEmptyDirectoryIgnored() {
        fs.mkdir("/tmp");
        fs.touch("/tmp/a.bin", 7);
        fs.rm("/tmp");
        assertNotNull(fs.getRoot().getChild("tmp"));
    }

    @Test void rmLink_onlyRemovesLink() {
        fs.touch("/data.bin", 12);
        fs.link("/data.bin", "/copy");
        fs.rm("/copy");
        assertNull(fs.getRoot().getChild("copy"));
        assertNotNull(fs.getRoot().getChild("data.bin"));  // 原始文件不受影响
    }

    @Test void rmNonExistentIgnored() {
        fs.rm("/nope");  // 无异常
    }

    @Test void rmRootIgnored() {
        fs.rm("/");
        assertEquals(NodeType.DIRECTORY, fs.getRoot().type());
    }

    @Test void rmSpecExample() {
        // 规格示例：删文件后目录变空可再删
        fs.mkdir("/tmp");
        fs.touch("/tmp/a.bin", 7);
        assertEquals("a.bin", fs.ls("/tmp"));
        fs.rm("/tmp/a.bin");
        assertEquals("", fs.ls("/tmp"));  // 空目录
        fs.rm("/tmp");
        assertNull(fs.getRoot().getChild("tmp"));
        assertEquals("", fs.ls("/"));   // 根也为空
    }

    @Test void rmNonEmptyDirSpecExample() {
        // 规格示例：非空目录不能删
        fs.mkdir("/tmp");
        fs.mkdir("/tmp/cache");
        fs.touch("/tmp/cache/a.bin", 7);
        fs.rm("/tmp/cache");  // 非空，忽略
        assertEquals("cache", fs.ls("/tmp"));
    }
}
