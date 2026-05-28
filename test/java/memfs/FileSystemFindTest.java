package memfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemFindTest {

    private FileSystem fs;

    @BeforeEach void setUp() { fs = new FileSystem(); }

    @Test void findInSubtree() {
        // 规格示例1
        fs.mkdir("/src");
        fs.mkdir("/src/main");
        fs.mkdir("/src/test");
        fs.touch("/src/main/App.java", 10);
        fs.touch("/src/test/App.java", 20);
        fs.touch("/src/test/Helper.java", 5);
        assertEquals("/src/main/App.java\n/src/test/App.java", fs.find("/src", "App.java"));
    }

    @Test void findFromFileStartPoint() {
        // 规格示例2：起点是文件
        fs.touch("/readme.md", 50);
        assertEquals("/readme.md", fs.find("/readme.md", "readme.md"));
        assertNull(fs.find("/readme.md", "other.md"));
    }

    @Test void findNoMatch() {
        fs.mkdir("/a");
        assertNull(fs.find("/a", "nope.txt"));
    }

    @Test void findLinkNodeItself() {
        // 链接节点本身可作为匹配结果
        fs.touch("/data.bin", 10);
        fs.link("/data.bin", "/copy");
        assertEquals("/copy", fs.find("/", "copy"));
    }

    @Test void findFromLinkStartPoint() {
        // 起点是链接到目录时，递归搜索目标目录子树
        fs.mkdir("/real");
        fs.touch("/real/file.txt", 5);
        fs.link("/real", "/alias");
        assertEquals("/alias/file.txt", fs.find("/alias", "file.txt"));
    }

    @Test void findLinkNotFollowedDuringTraversal() {
        // 遍历子节点时不跟随链接：OJ 核心场景
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        fs.touch("/usr/local/a.txt", 10);
        fs.link("/usr/local", "/alias");
        // alias 字典序先于 usr，但 FIND 不跟随链接，输出真实路径
        assertEquals("/usr/local/a.txt", fs.find("/", "a.txt"));
    }

    @Test void findAlphabeticalOrder() {
        fs.mkdir("/b");
        fs.mkdir("/a");
        fs.touch("/b/f.txt", 1);
        fs.touch("/a/f.txt", 1);
        assertEquals("/a/f.txt\n/b/f.txt", fs.find("/", "f.txt"));
    }
}
