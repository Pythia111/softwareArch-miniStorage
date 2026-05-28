package memfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemInfoDedupTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void linkDoesNotDoubleCount() {
        fs.mkdir("/data");
        fs.touch("/data/a", 10);
        fs.touch("/data/b", 20);
        fs.link("/data", "/alias");
        assertEquals("30", fs.info("/data"));
        assertEquals("30", fs.info("/alias"));
        assertEquals("30", fs.info("/"));   // /data 和 /alias 指向同一底层目录，只计一次
    }

    @Test void infoLinkToFile() {
        fs.touch("/x", 12);
        fs.link("/x", "/copy");
        assertEquals("12", fs.info("/copy"));
        assertEquals("12", fs.info("/x"));
    }
}
