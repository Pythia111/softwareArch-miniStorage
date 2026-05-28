package memfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemInfoTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void infoFile() {
        fs.touch("/readme.md", 50);
        assertEquals("50", fs.info("/readme.md"));
    }

    @Test void infoDirectoryRecursive() {
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        fs.touch("/usr/local/test.txt", 100);
        fs.touch("/readme.md", 50);
        assertEquals("150", fs.info("/"));
    }

    @Test void infoSubdirectory() {
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        fs.touch("/usr/local/test.txt", 100);
        fs.touch("/readme.md", 50);
        assertEquals("100", fs.info("/usr"));
    }

    @Test void infoEmptyDirectory() {
        fs.mkdir("/empty");
        assertEquals("0", fs.info("/empty"));
    }

    @Test void infoRootEmpty() {
        assertEquals("0", fs.info("/"));
    }

    @Test void infoInvalidPathReturnsNull() {
        assertNull(fs.info("/a//b"));
    }
}
