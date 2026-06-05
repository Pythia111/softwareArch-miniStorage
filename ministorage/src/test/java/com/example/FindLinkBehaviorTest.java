package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试FIND命令与链接的正确行为
 * 关键规则：遍历时不跟随链接到目录，只有起点是链接时才跟随
 */
public class FindLinkBehaviorTest {

    private MemFs fs;

    @BeforeEach
    void setUp() {
        fs = new MemFs();
    }

    @Test
    void testFind_traversalDoesNotFollowLinks() {
        // OJ规则：遍历时不跟随链接到目录
        // /alias < /usr (字典序)，但遍历时不跟随/alias这个链接
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        fs.touch("/usr/local/a.txt", 10);
        fs.link("/usr/local", "/alias");

        List<String> result = fs.find("/", "a.txt");

        // 期望只找到 /usr/local/a.txt，不会通过/alias找到
        assertEquals(1, result.size());
        assertEquals("/usr/local/a.txt", result.get(0));
    }

    @Test
    void testFind_startFromLinkToDirectory_shouldFollow() {
        // 当起点是链接时，应该跟随链接
        fs.mkdir("/real");
        fs.touch("/real/file.txt", 5);
        fs.link("/real", "/alias");

        List<String> result = fs.find("/alias", "file.txt");

        // 从链接起点开始，应该跟随链接并报告 /alias/file.txt
        assertEquals(1, result.size());
        assertEquals("/alias/file.txt", result.get(0));
    }

    @Test
    void testFind_startFromLinkToFile_matchesSelf() {
        // 当起点是链接到文件时，只检查链接自身名称
        fs.touch("/data.bin", 10);
        fs.link("/data.bin", "/copy");

        // FIND /copy copy - 链接名称匹配
        List<String> result1 = fs.find("/copy", "copy");
        assertEquals(1, result1.size());
        assertEquals("/copy", result1.get(0));

        // FIND /copy data.bin - 链接名称不匹配（链接名为copy）
        List<String> result2 = fs.find("/copy", "data.bin");
        assertTrue(result2.isEmpty());
    }

    @Test
    void testFind_linkNameMatchesDuringTraversal() {
        // 链接节点本身的名称可以作为匹配结果
        fs.mkdir("/real");
        fs.link("/real", "/alias");

        List<String> result = fs.find("/", "alias");

        assertEquals(1, result.size());
        assertEquals("/alias", result.get(0));
    }

    @Test
    void testFind_multipleLinksToSameDir_onlyRealPathReturned() {
        // 多个链接指向同一个目录，遍历时不跟随链接，只返回真实路径
        fs.mkdir("/real");
        fs.touch("/real/f.txt", 1);
        fs.link("/real", "/link1");
        fs.link("/real", "/link2");

        List<String> result = fs.find("/", "f.txt");

        // 只通过真实目录/real找到文件，链接不会被跟随
        assertEquals(1, result.size());
        assertEquals("/real/f.txt", result.get(0));
    }

    @Test
    void testFind_linkToLink_startFromSecondLink() {
        // 链接链：lnk2 -> lnk1 -> dir
        fs.mkdir("/dir");
        fs.touch("/dir/file.txt", 7);
        fs.link("/dir", "/lnk1");
        fs.link("/lnk1", "/lnk2");

        // 从lnk2开始FIND，应该跟随链接链
        List<String> result = fs.find("/lnk2", "file.txt");

        assertEquals(1, result.size());
        assertEquals("/lnk2/file.txt", result.get(0));
    }

    @Test
    void testFind_dedupWithLinksAndRealDir() {
        // 有真实目录和链接指向同一个底层目录
        // 遍历时：先遍历真实目录，展开并标记为已访问
        // 再遇到链接时，虽然不跟随，但即使跟随也会被防重复机制阻止
        fs.mkdir("/a");
        fs.mkdir("/a/b");
        fs.touch("/a/b/target.txt", 5);
        fs.link("/a/b", "/link");

        List<String> result = fs.find("/a", "target.txt");

        // 只找到通过真实路径的结果
        assertEquals(1, result.size());
        assertEquals("/a/b/target.txt", result.get(0));
    }

    @Test
    void testFind_containerWithOnlyLinks() {
        // 容器目录只包含链接节点，遍历时会跟随链接
        fs.mkdir("/real");
        fs.touch("/real/file.txt", 1);

        fs.mkdir("/container");
        fs.link("/real", "/container/lnk1");
        fs.link("/real", "/container/lnk2");

        // 在container下找file.txt，会跟随链接进入/real
        // lnk1和lnk2都指向同一个/real目录
        // 由于优先处理非链接子节点，且visitedDirs去重，只会通过lnk1找到一次
        List<String> result = fs.find("/container", "file.txt");
        assertEquals(1, result.size());
        assertEquals("/container/lnk1/file.txt", result.get(0));

        // 但可以找到链接本身的名称
        List<String> linkResult = fs.find("/container", "lnk1");
        assertEquals(1, linkResult.size());
        assertEquals("/container/lnk1", linkResult.get(0));
    }

    @Test
    void testFind_mixedRealDirAndLinks() {
        // 混合真实目录和链接
        fs.mkdir("/real1");
        fs.touch("/real1/data.txt", 10);

        fs.mkdir("/real2");
        fs.touch("/real2/data.txt", 20);

        fs.link("/real1", "/link1");
        fs.link("/real2", "/link2");

        List<String> result = fs.find("/", "data.txt");

        // 只找到通过真实目录的路径
        assertEquals(2, result.size());
        assertTrue(result.contains("/real1/data.txt"));
        assertTrue(result.contains("/real2/data.txt"));
        assertFalse(result.contains("/link1/data.txt"));
        assertFalse(result.contains("/link2/data.txt"));
    }

    @Test
    void testFind_specSection6Example() {
        // 需求文档第6节的标准示例
        fs.mkdir("/usr");
        fs.mkdir("/usr/local");
        fs.touch("/usr/local/a.txt", 10);
        fs.touch("/usr/b.txt", 5);
        fs.link("/usr/local", "/alias");

        List<String> result = fs.find("/", "a.txt");

        // 期望输出 /usr/local/a.txt，不是 /alias/a.txt
        assertEquals(1, result.size());
        assertEquals("/usr/local/a.txt", result.get(0));
    }
}
