package memfs;

import org.junit.jupiter.api.Test;
import java.io.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * End-to-end I/O tests through Main to catch stdout formatting issues.
 */
class MainIoTest {

    private String run(String input) throws Exception {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            Main.main(new String[0]);
            return baos.toString()
                       .replace("\r\n", "\n")
                       .replace("\r", "\n")
                       .stripTrailing();
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    @Test void spec_section6_fullExample() throws Exception {
        String input = """
                MKDIR /usr
                MKDIR //usr///local
                TOUCH /usr/local/./a.txt 10
                TOUCH /usr/local/../b.txt 5
                LINK /usr/local /alias
                LS /
                INFO /
                FIND / a.txt
                RM /alias
                LS /
                """;
        assertEquals("alias\nusr\n15\n/usr/local/a.txt\nusr", run(input));
    }

    @Test void spec_link_example1() throws Exception {
        String input = """
                TOUCH /data.bin 12
                LINK /data.bin /copy
                LS /
                LS /copy
                INFO /copy
                """;
        assertEquals("copy\ndata.bin\ncopy\n12", run(input));
    }

    @Test void spec_link_example2() throws Exception {
        String input = """
                MKDIR /real
                TOUCH /real/a.txt 10
                LINK /real /view
                LS /view
                TOUCH /view/b.txt 5
                LS /real
                INFO /view
                """;
        assertEquals("a.txt\na.txt\nb.txt\n15", run(input));
    }

    @Test void spec_info_dedup() throws Exception {
        String input = """
                MKDIR /data
                TOUCH /data/a 10
                TOUCH /data/b 20
                LINK /data /alias
                INFO /data
                INFO /alias
                INFO /
                """;
        assertEquals("30\n30\n30", run(input));
    }

    @Test void spec_overwrite_semantics() throws Exception {
        String input = """
                TOUCH /x 5
                LINK /x /y
                INFO /
                TOUCH /y 30
                INFO /
                MKDIR /y
                LS /
                INFO /
                """;
        assertEquals("5\n35\nx\ny\n5", run(input));
    }

    @Test void spec_rm_example1() throws Exception {
        String input = """
                MKDIR /tmp
                TOUCH /tmp/a.bin 7
                LS /tmp
                RM /tmp/a.bin
                LS /tmp
                RM /tmp
                LS /
                """;
        // After RM /tmp/a.bin: /tmp is empty -> LS /tmp produces no output
        // After RM /tmp: root is empty -> LS / produces no output
        assertEquals("a.bin", run(input));
    }

    @Test void spec_rm_example2() throws Exception {
        String input = """
                MKDIR /tmp
                MKDIR /tmp/cache
                TOUCH /tmp/cache/a.bin 7
                RM /tmp/cache
                LS /tmp
                """;
        assertEquals("cache", run(input));
    }

    @Test void spec_find_example1() throws Exception {
        String input = """
                MKDIR /src
                MKDIR /src/main
                MKDIR /src/test
                TOUCH /src/main/App.java 10
                TOUCH /src/test/App.java 20
                TOUCH /src/test/Helper.java 5
                FIND /src App.java
                """;
        assertEquals("/src/main/App.java\n/src/test/App.java", run(input));
    }

    @Test void spec_find_example2() throws Exception {
        String input = """
                TOUCH /readme.md 50
                FIND /readme.md readme.md
                FIND /readme.md other.md
                """;
        assertEquals("/readme.md", run(input));
    }

    @Test void spec_path_normalization() throws Exception {
        String input = """
                MKDIR /usr
                MKDIR //usr///local
                TOUCH /usr/local/./a.txt 10
                TOUCH /usr/local/../b.txt 5
                LS /usr//local/
                INFO /usr/./local/../
                """;
        assertEquals("a.txt\n15", run(input));
    }

    @Test void find_link_in_subtree_matches_by_name() throws Exception {
        // Link node itself matches if its name equals search target
        String input = """
                TOUCH /data.bin 10
                LINK /data.bin /copy
                FIND / copy
                """;
        assertEquals("/copy", run(input));
    }

    @Test void find_from_link_start_to_directory() throws Exception {
        String input = """
                MKDIR /real
                TOUCH /real/file.txt 5
                LINK /real /alias
                FIND /alias file.txt
                """;
        assertEquals("/alias/file.txt", run(input));
    }

    @Test void info_root_zero_when_empty() throws Exception {
        assertEquals("0", run("INFO /\n"));
    }

    @Test void rm_link_to_dir_keeps_dir() throws Exception {
        String input = """
                MKDIR /real
                TOUCH /real/f.txt 5
                LINK /real /lnk
                RM /lnk
                LS /real
                """;
        assertEquals("f.txt", run(input));
    }

    @Test void touch_size_zero() throws Exception {
        String input = """
                TOUCH /empty 0
                INFO /empty
                """;
        assertEquals("0", run(input));
    }

    @Test void touch_negative_size_treated_as_zero() throws Exception {
        String input = """
                TOUCH /f -1
                INFO /f
                """;
        assertEquals("", run(input));
    }

    @Test void ls_emptyDirectory_noOutput() throws Exception {
        String input = """
                MKDIR /empty
                LS /empty
                """;
        assertEquals("", run(input));
    }

    @Test void ls_root_empty_noOutput() throws Exception {
        assertEquals("", run("LS /\n"));
    }

    @Test void link_chain_info() throws Exception {
        // lnk2 -> lnk1 -> file
        String input = """
                TOUCH /file.txt 7
                LINK /file.txt /lnk1
                LINK /lnk1 /lnk2
                INFO /lnk2
                LS /lnk2
                """;
        assertEquals("7\nlnk2", run(input));
    }

    @Test void find_link_not_followed_during_traversal() throws Exception {
        // OJ spec: traversal does not follow links to dirs
        String input = """
                MKDIR /usr
                MKDIR /usr/local
                TOUCH /usr/local/a.txt 10
                LINK /usr/local /alias
                FIND / a.txt
                """;
        // /alias is alphabetically before /usr, but links NOT followed during traversal
        assertEquals("/usr/local/a.txt", run(input));
    }

    @Test void multiple_links_same_file_info_dedup() throws Exception {
        String input = """
                TOUCH /file.txt 10
                LINK /file.txt /lnk1
                LINK /file.txt /lnk2
                INFO /
                """;
        // file.txt + lnk1 + lnk2 all point to same file -> counted once
        assertEquals("10", run(input));
    }

    @Test void mkdir_replaces_file() throws Exception {
        String input = """
                TOUCH /foo 99
                MKDIR /foo
                INFO /
                LS /
                """;
        assertEquals("0\nfoo", run(input));
    }

    @Test void touch_replaces_link() throws Exception {
        String input = """
                TOUCH /x 5
                LINK /x /y
                TOUCH /y 30
                INFO /
                """;
        // /y replaced by new file(30), /x unchanged(5) -> total 35
        assertEquals("35", run(input));
    }

    @Test void mkdir_replaces_link() throws Exception {
        String input = """
                TOUCH /x 5
                LINK /x /y
                MKDIR /y
                LS /
                INFO /
                """;
        // /y replaced by empty dir -> total = x(5)
        assertEquals("x\ny\n5", run(input));
    }
}
