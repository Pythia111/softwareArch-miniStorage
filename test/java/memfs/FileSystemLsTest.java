package memfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemLsTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void lsRootWithMixedChildren() {
        fs.mkdir("/usr");
        fs.touch("/readme.md", 50);
        assertEquals("readme.md\nusr", fs.ls("/"));
    }

    @Test void lsOnFileReturnsFileName() {
        fs.touch("/readme.md", 50);
        assertEquals("readme.md", fs.ls("/readme.md"));
    }

    @Test void lsEmptyDirectoryReturnsEmptyString() {
        fs.mkdir("/empty");
        assertEquals("", fs.ls("/empty"));
    }

    @Test void lsAlphabeticalOrder() {
        fs.touch("/z.txt", 1);
        fs.touch("/a.txt", 1);
        fs.touch("/m.txt", 1);
        assertEquals("a.txt\nm.txt\nz.txt", fs.ls("/"));
    }

    @Test void lsRootAlone() {
        assertEquals("", fs.ls("/"));
    }

    @Test void lsInvalidPathReturnsNull() {
        assertNull(fs.ls("/a//b"));
    }
}
