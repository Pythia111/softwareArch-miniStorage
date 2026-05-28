package memfs;

import memfs.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemTouchTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void touchCreatesFileWithSize() {
        fs.touch("/readme.md", 50);
        Node node = fs.getRoot().getChild("readme.md");
        assertNotNull(node);
        assertEquals(NodeType.FILE, node.type());
        assertEquals(50, node.size(null));
    }

    @Test void touchOverwritesExistingFileSize() {
        fs.touch("/readme.md", 50);
        fs.touch("/readme.md", 99);
        assertEquals(99, fs.getRoot().getChild("readme.md").size(null));
    }

    @Test void touchReplacesDirectoryWithFile() {
        fs.mkdir("/foo");
        fs.touch("/foo", 42);
        Node node = fs.getRoot().getChild("foo");
        assertEquals(NodeType.FILE, node.type());
        assertEquals(42, node.size(null));
    }

    @Test void touchIgnoredWhenParentMissing() {
        fs.touch("/missing/file.txt", 10);
        assertNull(fs.getRoot().getChild("missing"));
    }

    @Test void touchIgnoredOnInvalidPath() {
        fs.touch("/a//b", 5);
        assertNull(fs.getRoot().getChild("a"));
    }

    @Test void touchCreatesNestedFile() {
        fs.mkdir("/usr");
        fs.touch("/usr/hello.txt", 77);
        DirectoryNode usr = (DirectoryNode) fs.getRoot().getChild("usr");
        assertEquals(77, usr.getChild("hello.txt").size(null));
    }
}
