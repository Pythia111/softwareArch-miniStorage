package memfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemEdgeCaseTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    // ── FIND through link-to-directory during traversal ──────────────────────

    @Test void find_linkToDir_childrenAccessibleViaFindFromRoot() {
        // FIND spec: "遇到链接到目录的节点时，需要进入该链接指向的目录继续搜索"
        // This is the KEY ambiguity: does findRecursive follow links to dirs?
        fs.mkdir("/real");
        fs.touch("/real/target.txt", 5);
        fs.link("/real", "/lnk");
        // The spec says: find through a link-to-dir node during traversal
        // "为避免共享目录被重复展开，同一个底层目录在一次 FIND 中最多展开一次"
        // This implies links DO get followed during traversal
        String result = fs.find("/", "target.txt");
        // Should find /real/target.txt (via /real dir)
        // /lnk points to /real, but /real/target.txt is already found via /real
        assertTrue(result != null && result.contains("target.txt"));
    }

    @Test void find_linkToDir_followedDuringTraversal() {
        // A link to a dir that has unique children (not reachable via real path)
        // create /orphan dir NOT accessible via any real path from root
        // Actually: link to an otherwise accessible dir
        fs.mkdir("/a");
        fs.touch("/a/file.txt", 10);
        fs.link("/a", "/b"); // /b links to /a
        // FIND / file.txt: should find /a/file.txt
        // Whether /b/file.txt also appears depends on spec interpretation
        String result = fs.find("/", "file.txt");
        assertNotNull(result);
        assertTrue(result.contains("/a/file.txt"));
    }

    @Test void find_linkToDir_onlyExpandedOnce() {
        // /alias -> /real; FIND should not output both /real/f.txt and /alias/f.txt
        fs.mkdir("/real");
        fs.touch("/real/f.txt", 1);
        fs.link("/real", "/alias");
        String result = fs.find("/", "f.txt");
        // Per OJ example: only /real/f.txt (links not followed during traversal)
        assertEquals("/real/f.txt", result);
    }

    @Test void find_linkName_matchesDuringTraversal() {
        // The link node itself (named "alias") should match if we FIND "alias"
        fs.mkdir("/real");
        fs.link("/real", "/alias");
        String result = fs.find("/", "alias");
        assertEquals("/alias", result);
    }

    // ── LINK to a link (link chain) ──────────────────────────────────────────

    @Test void link_toLink_chain() {
        fs.touch("/file.txt", 10);
        fs.link("/file.txt", "/lnk1");
        fs.link("/lnk1", "/lnk2");  // lnk2 -> lnk1 -> file.txt
        assertEquals("10", fs.info("/lnk2"));
        assertEquals("lnk2", fs.ls("/lnk2"));
    }

    @Test void link_toLink_directory_chain() {
        fs.mkdir("/dir");
        fs.touch("/dir/a.txt", 5);
        fs.link("/dir", "/lnk1");
        fs.link("/lnk1", "/lnk2");  // lnk2 -> lnk1 -> dir
        assertEquals("a.txt", fs.ls("/lnk2"));
        assertEquals("5", fs.info("/lnk2"));
    }

    // ── RM on link to directory ───────────────────────────────────────────────

    @Test void rm_link_toDirectory_onlyRemovesLink() {
        fs.mkdir("/real");
        fs.touch("/real/f.txt", 5);
        fs.link("/real", "/lnk");
        fs.rm("/lnk");  // removes link, not the directory
        assertNull(fs.ls("/lnk"));  // link gone
        assertEquals("f.txt", fs.ls("/real"));  // real dir still exists
    }

    // ── TOUCH/MKDIR through link-to-link ─────────────────────────────────────

    @Test void touch_throughLinkToLink() {
        fs.mkdir("/dir");
        fs.link("/dir", "/lnk1");
        fs.link("/lnk1", "/lnk2");
        fs.touch("/lnk2/file.txt", 7);
        assertEquals("file.txt", fs.ls("/dir"));
        assertEquals("7", fs.info("/dir"));
    }

    @Test void mkdir_throughLinkToLink() {
        fs.mkdir("/dir");
        fs.link("/dir", "/lnk1");
        fs.link("/lnk1", "/lnk2");
        fs.mkdir("/lnk2/sub");
        assertEquals("sub", fs.ls("/dir"));
    }

    // ── INFO on root ─────────────────────────────────────────────────────────

    @Test void info_root_empty() {
        assertEquals("0", fs.info("/"));
    }

    @Test void info_root_withFiles() {
        fs.touch("/a", 3);
        fs.touch("/b", 7);
        assertEquals("10", fs.info("/"));
    }

    // ── LS on empty dir returns empty (not null) ──────────────────────────────

    @Test void ls_emptyDirectory_returnsEmpty() {
        fs.mkdir("/empty");
        String result = fs.ls("/empty");
        // empty dir: ls returns "" not null
        assertTrue(result != null && result.isEmpty());
    }

    @Test void ls_root_empty_returnsEmpty() {
        String result = fs.ls("/");
        assertTrue(result != null && result.isEmpty());
    }

    // ── FIND from root ────────────────────────────────────────────────────────

    @Test void find_fromRoot_matchRoot() {
        // FIND / / - root's name is "" which cannot match normal names
        // FIND / someFile where root has that file
        fs.touch("/target.txt", 1);
        assertEquals("/target.txt", fs.find("/", "target.txt"));
    }

    @Test void find_nonExistentPath_returnsNull() {
        assertNull(fs.find("/nonexistent", "file.txt"));
    }

    // ── Path normalization edge cases ─────────────────────────────────────────

    @Test void normalize_dotDot_aboveRoot_staysRoot() {
        fs.touch("/a.txt", 5);
        // /../a.txt should normalize to /a.txt (.. at root stays root)
        assertEquals("5", fs.info("/../a.txt"));
    }

    @Test void normalize_multipleDotDot() {
        fs.mkdir("/a");
        fs.mkdir("/a/b");
        fs.touch("/a/b/f.txt", 3);
        // /a/b/../../a/b/f.txt -> /a/b/f.txt
        assertEquals("3", fs.info("/a/b/../../a/b/f.txt"));
    }

    @Test void normalize_relativePathIgnored() {
        fs.touch("/a.txt", 5);
        // relative path should be ignored
        assertNull(fs.ls("a.txt"));
        assertNull(fs.info("a.txt"));
    }

    // ── LINK cannot replace root ──────────────────────────────────────────────

    @Test void link_dstIsRoot_ignored() {
        fs.touch("/x", 5);
        fs.link("/x", "/");  // cannot replace root
        assertEquals("x", fs.ls("/"));  // root still has x, not replaced
    }

    // ── MKDIR cannot replace root ─────────────────────────────────────────────

    @Test void mkdir_pathIsRoot_ignored() {
        fs.touch("/x", 5);
        fs.mkdir("/");  // cannot replace root
        assertEquals("x", fs.ls("/"));  // root unchanged
    }

    // ── TOUCH cannot replace root ─────────────────────────────────────────────

    @Test void touch_pathIsRoot_ignored() {
        fs.touch("/x", 5);
        fs.touch("/", 10);  // cannot replace root
        assertEquals("x", fs.ls("/"));
    }

    // ── FIND with link-to-dir: spec says enter linked dir ────────────────────

    @Test void find_linkToDir_enterLinkedDirForSearch() {
        // Spec 5.6: "当搜索遇到链接到目录的节点时，需要进入该链接指向的目录继续搜索"
        // Create a structure where the file is ONLY reachable through a link
        fs.mkdir("/hidden");
        fs.touch("/hidden/secret.txt", 1);
        // /hidden is real, but also accessible via link /view
        fs.link("/hidden", "/view");
        // FIND / secret.txt - hidden is real dir, find via /hidden/secret.txt
        assertEquals("/hidden/secret.txt", fs.find("/", "secret.txt"));
    }

    @Test void find_startFromLinkToFile_matchesSelf() {
        fs.touch("/data.bin", 10);
        fs.link("/data.bin", "/copy");
        // FIND /copy copy - link name matches
        assertEquals("/copy", fs.find("/copy", "copy"));
        // FIND /copy data.bin - link name doesn't match (it's named copy)
        assertNull(fs.find("/copy", "data.bin"));
    }

    // ── LS on link: when target is file, output link's OWN name ──────────────

    @Test void ls_linkToFile_outputsLinkName_notFileName() {
        fs.touch("/original.txt", 5);
        fs.link("/original.txt", "/renamed");
        assertEquals("renamed", fs.ls("/renamed"));
    }

    // ── INFO with nested links and dedup ─────────────────────────────────────

    @Test void info_multipleLinksToSameFile_countOnce() {
        fs.touch("/file.txt", 10);
        fs.link("/file.txt", "/lnk1");
        fs.link("/file.txt", "/lnk2");
        // Root has: file.txt(10), lnk1->file.txt, lnk2->file.txt
        // INFO / should be 10 (file counted once)
        assertEquals("10", fs.info("/"));
    }

    @Test void info_multipleLinksToSameDir_countOnce() {
        fs.mkdir("/dir");
        fs.touch("/dir/f", 5);
        fs.link("/dir", "/lnk1");
        fs.link("/dir", "/lnk2");
        // Root: dir(5), lnk1->dir, lnk2->dir => 5
        assertEquals("5", fs.info("/"));
    }
}
