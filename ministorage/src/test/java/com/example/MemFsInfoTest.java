package com.example;

import com.example.fs.Directory;
import com.example.fs.File;
import com.example.fs.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

public class MemFsInfoTest {

    private MemFs memFs;
    private Directory root;

    @BeforeEach
    public void setUp() throws Exception {
        memFs = new MemFs();
        Field rootField = MemFs.class.getDeclaredField("root");
        rootField.setAccessible(true);
        root = (Directory) rootField.get(memFs);
    }

    @Test
    public void testInfoFileNotFound() {
        assertNull(memFs.info("/non_existent_file"));
    }

    @Test
    public void testInfoRootDirectoryEmpty() {
        assertEquals(0L, memFs.info("/"));
    }

    @Test
    public void testInfoSingleFile() {
        memFs.touch("/fileA", 500L);
        assertEquals(500L, memFs.info("/fileA"));
    }

    @Test
    public void testInfoDirectoryWithFiles() {
        memFs.mkdir("/dir");
        memFs.touch("/dir/file1", 100L);
        memFs.touch("/dir/file2", 200L);
        assertNull(memFs.info("/dir/file3")); // not exist
        assertEquals(300L, memFs.info("/dir"));
        assertEquals(300L, memFs.info("/"));
    }

    @Test
    public void testInfoWithLinkedFile() {
        File file1 = new File("file1", 1000L);
        root.putChild("file1", file1);
        Link link1 = new Link("link1", file1);
        root.putChild("link1", link1);

        // testing info directly to the link pointing to a file
        assertEquals(1000L, memFs.info("/link1"));
        // root total size should only count file1 once
        assertEquals(1000L, memFs.info("/"));
    }

    @Test
    public void testInfoWithLinkedDirectory() {
        Directory subDir = new Directory("subDir");
        root.putChild("subDir", subDir);
        File file1 = new File("f1", 400L);
        subDir.putChild("f1", file1);

        Link linkDir = new Link("linkDir", subDir);
        root.putChild("linkDir", linkDir);

        // the size of the link that points to a directory
        assertEquals(400L, memFs.info("/linkDir"));
        // root deduplicates
        assertEquals(400L, memFs.info("/"));
    }

    @Test
    public void testInfoInvalidPath() {
        assertNull(memFs.info("/invalid/path"));
        assertNull(memFs.info("invalid_prefix/file")); // not an absolute path?
    }
}
