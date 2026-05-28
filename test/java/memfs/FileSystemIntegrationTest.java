package memfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemIntegrationTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void ojSpecExample() {
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        fs.touch("/usr/local/test.txt", 100);
        fs.touch("/readme.md", 50);

        assertEquals("readme.md\nusr", fs.ls("/"));
        assertEquals("150", fs.info("/"));
        assertEquals("100", fs.info("/usr"));
    }

    @Test void touchReplacesDir_dirSizeDroppedFromParent() {
        fs.mkdir("/a");
        fs.touch("/a/big.bin", 1000);
        assertEquals("1000", fs.info("/a"));
        fs.touch("/a", 5);
        assertEquals("5", fs.info("/a"));
        assertEquals("5", fs.info("/"));
    }

    @Test void mkdirReplacesFile() {
        fs.touch("/foo", 99);
        assertEquals("99", fs.info("/"));
        fs.mkdir("/foo");
        assertEquals("0", fs.info("/"));
    }

    @Test void lsOnFile() {
        fs.touch("/readme.md", 50);
        assertEquals("readme.md", fs.ls("/readme.md"));
    }

    @Test void pathNormalizationWorks() {
        fs.mkdir("/usr");
        fs.mkdir("//usr///local");
        fs.touch("/usr/local/./a.txt", 10);
        fs.touch("/usr/local/../b.txt", 5);
        // /usr/local/./a.txt → /usr/local/a.txt
        // /usr/local/../b.txt → /usr/b.txt
        assertEquals("a.txt", fs.ls("/usr//local/"));  // → /usr/local
        assertEquals("15", fs.info("/usr/./local/../"));  // → /usr，含 a.txt(10)+b.txt(5)
    }

    @Test void iter2FullSpecExample() {
        fs.mkdir("/usr");
        fs.mkdir("//usr///local");
        fs.touch("/usr/local/./a.txt", 10);
        fs.touch("/usr/local/../b.txt", 5);
        fs.link("/usr/local", "/alias");

        assertEquals("alias\nusr", fs.ls("/"));
        assertEquals("15", fs.info("/"));
        // FIND 不跟随链接递归进入目标目录，只递归真实 DirectoryNode
        assertEquals("/usr/local/a.txt", fs.find("/", "a.txt"));
        fs.rm("/alias");
        assertEquals("usr", fs.ls("/"));
    }
}
