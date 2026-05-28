package memfs;

import memfs.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemMkdirTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void mkdirCreatesDirectoryUnderRoot() {
        fs.mkdir("/usr");
        Node node = fs.getRoot().getChild("usr");
        assertNotNull(node);
        assertEquals(NodeType.DIRECTORY, node.type());
    }

    @Test void mkdirNestedCreatesChildDirectory() {
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        DirectoryNode usr = (DirectoryNode) fs.getRoot().getChild("usr");
        assertNotNull(usr.getChild("local"));
    }

    @Test void mkdirIgnoredWhenParentMissing() {
        fs.mkdir("/a/b/c");
        assertNull(fs.getRoot().getChild("a"));
    }

    @Test void mkdirOnExistingDirectoryIsNoOp() {
        fs.mkdir("/usr");
        fs.mkdir("/usr");
        assertEquals(NodeType.DIRECTORY, fs.getRoot().getChild("usr").type());
    }

    @Test void mkdirReplacesFileWithDirectory() {
        fs.touch("/foo", 10);
        fs.mkdir("/foo");
        assertEquals(NodeType.DIRECTORY, fs.getRoot().getChild("foo").type());
    }

    @Test void mkdirNormalizesDoubleSlash() {
        fs.mkdir("/usr");
        fs.mkdir("//usr///local");
        DirectoryNode usr = (DirectoryNode) fs.getRoot().getChild("usr");
        assertNotNull(usr.getChild("local"));
    }

    @Test void mkdirNormalizesTrailingSlash() {
        fs.mkdir("/tmp/");
        assertNotNull(fs.getRoot().getChild("tmp"));
    }

    @Test void mkdirNormalizesDotDot() {
        fs.mkdir("/usr");
        fs.mkdir("/usr/local/../bin");
        DirectoryNode usr = (DirectoryNode) fs.getRoot().getChild("usr");
        assertNotNull(usr.getChild("bin"));
        assertNull(usr.getChild("local"));
    }
}
