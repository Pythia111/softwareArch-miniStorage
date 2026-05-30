package com.example.path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 成员3 (cyg) PathUtil 路径规范化单元测试
 *
 * 设计原则：
 * - 不硬编码：通过组合性验证确保方法间逻辑一致，而非只断言固定值
 * - 覆盖语义陷阱：.hidden 不是 "."，"..." 不是 ".."，b.c.d 是合法文件名
 * - normalize → split → getParentPath → getBaseName 端到端一致性
 * - isAbsolutePath / parse / parseNonRoot 完整覆盖
 */
public class Member3Test {

    // ================================================================
    //  isAbsolutePath()
    // ================================================================

    @Test
    public void isAbsolutePath_rootIsAbsolute() {
        assertTrue(PathUtil.isAbsolutePath("/"));
    }

    @Test
    public void isAbsolutePath_simplePathIsAbsolute() {
        assertTrue(PathUtil.isAbsolutePath("/a"));
    }

    @Test
    public void isAbsolutePath_doubleSlashIsAbsolute() {
        assertTrue(PathUtil.isAbsolutePath("//a"));
    }

    @Test
    public void isAbsolutePath_relativePathIsNot() {
        assertFalse(PathUtil.isAbsolutePath("usr/local"));
    }

    @Test
    public void isAbsolutePath_nullIsNot() {
        assertFalse(PathUtil.isAbsolutePath(null));
    }

    @Test
    public void isAbsolutePath_emptyIsNot() {
        assertFalse(PathUtil.isAbsolutePath(""));
    }

    // ================================================================
    //  normalize() — 冗余斜杠
    // ================================================================

    @Test
    public void normalize_redundantSlashes() {
        assertEquals("/usr/local", PathUtil.normalize("//usr///local"));
    }

    @Test
    public void normalize_onlySlashes() {
        assertEquals("/", PathUtil.normalize("///"));
    }

    @Test
    public void normalize_doubleSlashInMiddle() {
        assertEquals("/a/b", PathUtil.normalize("/a//b"));
    }

    // ================================================================
    //  normalize() — 尾随斜杠
    // ================================================================

    @Test
    public void normalize_trailingSlash() {
        assertEquals("/usr/local", PathUtil.normalize("/usr/local/"));
    }

    @Test
    public void normalize_singleComponentTrailingSlash() {
        assertEquals("/a", PathUtil.normalize("/a/"));
    }

    // ================================================================
    //  normalize() — . 段
    // ================================================================

    @Test
    public void normalize_dotSegment() {
        assertEquals("/usr/local/a.txt", PathUtil.normalize("/usr/local/./a.txt"));
    }

    @Test
    public void normalize_dotOnly() {
        assertEquals("/", PathUtil.normalize("/././."));
    }

    @Test
    public void normalize_dotInMiddle() {
        assertEquals("/a/b/c", PathUtil.normalize("/a/./b/./c"));
    }

    // ================================================================
    //  normalize() — .. 段
    // ================================================================

    @Test
    public void normalize_dotDotSegment() {
        assertEquals("/usr/b.txt", PathUtil.normalize("/usr/local/../b.txt"));
    }

    @Test
    public void normalize_dotDotInMiddle() {
        assertEquals("/a/c", PathUtil.normalize("/a/b/../c"));
    }

    @Test
    public void normalize_multipleDotDot() {
        assertEquals("/a/d", PathUtil.normalize("/a/b/c/../../d"));
    }

    // ================================================================
    //  normalize() — .. 超过根
    // ================================================================

    @Test
    public void normalize_dotDotAtRoot() {
        assertEquals("/", PathUtil.normalize("/.."));
    }

    @Test
    public void normalize_dotDotAboveRoot() {
        assertEquals("/", PathUtil.normalize("/../.."));
    }

    @Test
    public void normalize_dotDotAboveRootWithValidPath() {
        assertEquals("/", PathUtil.normalize("/a/b/../../.."));
    }

    // ================================================================
    //  normalize() — 复合场景（规格驱动）
    // ================================================================

    @Test
    public void normalize_complexPath() {
        assertEquals("/a/b", PathUtil.normalize("/a//b/./c/../"));
    }

    @Test
    public void normalize_root() {
        assertEquals("/", PathUtil.normalize("/"));
    }

    @Test
    public void normalize_simplePath() {
        assertEquals("/a/b/c", PathUtil.normalize("/a/b/c"));
    }

    @Test
    public void normalize_singleComponent() {
        assertEquals("/usr", PathUtil.normalize("/usr"));
    }

    // ================================================================
    //  normalize() — 语义陷阱
    // ================================================================

    @Test
    public void normalize_tripleDotsIsRegularName() {
        // "..." 不是 ".."，应保留为合法路径名
        assertEquals("/a/.../b", PathUtil.normalize("/a/.../b"));
    }

    @Test
    public void normalize_hiddenFileNameNotTreatedAsDot() {
        // ".hidden" 不是 "."，应保留为合法路径名
        assertEquals("/a/.hidden", PathUtil.normalize("/a/.hidden"));
    }

    @Test
    public void normalize_dotSuffixInName() {
        // "b.c.d" 是合法路径名，各段不应被拆开
        assertEquals("/a/b.c.d", PathUtil.normalize("/a/b.c.d"));
    }

    @Test
    public void normalize_dotAndDotDotMixedWithRealNames() {
        // /a/.../b/.hidden/../c → /a/.../b/c
        assertEquals("/a/.../b/c", PathUtil.normalize("/a/.../b/.hidden/../c"));
    }

    // ================================================================
    //  normalize() — 非法输入
    // ================================================================

    @Test
    public void normalize_nullReturnsNull() {
        assertNull(PathUtil.normalize(null));
    }

    @Test
    public void normalize_emptyReturnsNull() {
        assertNull(PathUtil.normalize(""));
    }

    @Test
    public void normalize_relativeReturnsNull() {
        assertNull(PathUtil.normalize("usr/local"));
    }

    // ================================================================
    //  split()
    // ================================================================

    @Test
    public void split_rootReturnsEmptyArray() {
        assertArrayEquals(new String[]{}, PathUtil.split("/"));
    }

    @Test
    public void split_singleComponent() {
        assertArrayEquals(new String[]{"usr"}, PathUtil.split("/usr"));
    }

    @Test
    public void split_multiComponent() {
        assertArrayEquals(new String[]{"a", "b", "c"}, PathUtil.split("/a/b/c"));
    }

    @Test
    public void split_nameWithDotsNotSplit() {
        // "b.c" 是一个路径组件，不应再拆分
        assertArrayEquals(new String[]{"a", "b.c", "d"}, PathUtil.split("/a/b.c/d"));
    }

    @Test
    public void split_hiddenFileNamePreserved() {
        assertArrayEquals(new String[]{"a", ".hidden"}, PathUtil.split("/a/.hidden"));
    }

    @Test
    public void split_tripleDotsPreserved() {
        assertArrayEquals(new String[]{"a", "..."}, PathUtil.split("/a/..."));
    }

    // ================================================================
    //  getParentPath()
    // ================================================================

    @Test
    public void getParentPath_rootReturnsNull() {
        assertNull(PathUtil.getParentPath("/"));
    }

    @Test
    public void getParentPath_singleComponentReturnsRoot() {
        assertEquals("/", PathUtil.getParentPath("/a"));
    }

    @Test
    public void getParentPath_multiComponent() {
        assertEquals("/a/b", PathUtil.getParentPath("/a/b/c"));
    }

    @Test
    public void getParentPath_twoLevel() {
        assertEquals("/a", PathUtil.getParentPath("/a/b"));
    }

    @Test
    public void getParentPath_chainConsistency() {
        // /a/b/c → /a/b → /a → / → null，验证逐层回退逻辑
        String path = "/a/b/c";
        path = PathUtil.getParentPath(path);
        assertEquals("/a/b", path);
        path = PathUtil.getParentPath(path);
        assertEquals("/a", path);
        path = PathUtil.getParentPath(path);
        assertEquals("/", path);
        path = PathUtil.getParentPath(path);
        assertNull(path);
    }

    // ================================================================
    //  getBaseName()
    // ================================================================

    @Test
    public void getBaseName_rootReturnsEmpty() {
        assertEquals("", PathUtil.getBaseName("/"));
    }

    @Test
    public void getBaseName_singleComponent() {
        assertEquals("a", PathUtil.getBaseName("/a"));
    }

    @Test
    public void getBaseName_multiComponent() {
        assertEquals("c", PathUtil.getBaseName("/a/b/c"));
    }

    @Test
    public void getBaseName_nameWithDots() {
        assertEquals("b.c.d", PathUtil.getBaseName("/a/b.c.d"));
    }

    @Test
    public void getBaseName_hiddenFileName() {
        assertEquals(".hidden", PathUtil.getBaseName("/a/.hidden"));
    }

    @Test
    public void getBaseName_tripleDots() {
        assertEquals("...", PathUtil.getBaseName("/a/..."));
    }

    // ================================================================
    //  parse() — 基本正确性
    // ================================================================

    @Test
    public void parse_validPathAllFieldsConsistent() {
        PathInfo info = PathUtil.parse("/a/b/c");
        assertNotNull(info);
        assertEquals("/a/b/c", info.getNormalizedPath());
        assertArrayEquals(new String[]{"a", "b", "c"}, info.getComponents());
        assertEquals("/a/b", info.getParentPath());
        assertEquals("c", info.getBaseName());
        assertFalse(info.isRoot());
    }

    @Test
    public void parse_rootPath() {
        PathInfo info = PathUtil.parse("/");
        assertNotNull(info);
        assertTrue(info.isRoot());
        assertNull(info.getParentPath());
        assertEquals("", info.getBaseName());
        assertEquals(0, info.getComponents().length);
    }

    @Test
    public void parse_nonCanonicalInputNormalizedFirst() {
        PathInfo info = PathUtil.parse("//a/./b/../c");
        assertNotNull(info);
        assertEquals("/a/c", info.getNormalizedPath());
        assertArrayEquals(new String[]{"a", "c"}, info.getComponents());
        assertEquals("/a", info.getParentPath());
        assertEquals("c", info.getBaseName());
    }

    @Test
    public void parse_nullReturnsNull() {
        assertNull(PathUtil.parse(null));
    }

    @Test
    public void parse_emptyReturnsNull() {
        assertNull(PathUtil.parse(""));
    }

    @Test
    public void parse_relativeReturnsNull() {
        assertNull(PathUtil.parse("relative/path"));
    }

    @Test
    public void parse_singleComponentPath() {
        PathInfo info = PathUtil.parse("/home");
        assertNotNull(info);
        assertFalse(info.isRoot());
        assertEquals("/", info.getParentPath());
        assertEquals("home", info.getBaseName());
    }

    // ================================================================
    //  parse() — 组合性验证：parse 结果应与直接调用各方法一致
    // ================================================================

    @Test
    public void parse_matchesDirectCalls() {
        // parse() 内部调用 normalize → split → getParentPath → getBaseName
        // 其结果应与手动调用完全一致
        String raw = "/x/../y/./z";
        PathInfo info = PathUtil.parse(raw);

        String normalized = PathUtil.normalize(raw);
        assertEquals(normalized, info.getNormalizedPath());
        assertArrayEquals(PathUtil.split(normalized), info.getComponents());
        assertEquals(PathUtil.getParentPath(normalized), info.getParentPath());
        assertEquals(PathUtil.getBaseName(normalized), info.getBaseName());
    }

    @Test
    public void parse_semanticTrapPath() {
        // 包含语义陷阱的路径：... 和 .hidden
        PathInfo info = PathUtil.parse("/a/.../b/.hidden/../c");
        assertNotNull(info);
        assertEquals("/a/.../b/c", info.getNormalizedPath());
        assertEquals("c", info.getBaseName());
        assertEquals("/a/.../b", info.getParentPath());
    }

    // ================================================================
    //  parseNonRoot()
    // ================================================================

    @Test
    public void parseNonRoot_validPath() {
        PathInfo info = PathUtil.parseNonRoot("/a/b");
        assertNotNull(info);
        assertEquals("/a/b", info.getNormalizedPath());
    }

    @Test
    public void parseNonRoot_rootReturnsNull() {
        assertNull(PathUtil.parseNonRoot("/"));
    }

    @Test
    public void parseNonRoot_nullReturnsNull() {
        assertNull(PathUtil.parseNonRoot(null));
    }

    @Test
    public void parseNonRoot_relativeReturnsNull() {
        assertNull(PathUtil.parseNonRoot("not/absolute"));
    }

    @Test
    public void parseNonRoot_singleComponent() {
        PathInfo info = PathUtil.parseNonRoot("/a");
        assertNotNull(info);
        assertEquals("/a", info.getNormalizedPath());
        assertEquals("/", info.getParentPath());
    }

    // ================================================================
    //  端到端一致性：normalize → split → getParentPath → getBaseName
    // ================================================================

    @Test
    public void consistency_splitAndJoinRoundTrip() {
        // normalize(p) 拆分后用 "/" 拼接，能还原原字符串
        String raw = "/a//b/./c/../d/";
        String normalized = PathUtil.normalize(raw);
        String[] parts = PathUtil.split(normalized);

        String reconstructed;
        if (parts.length == 0) {
            reconstructed = "/";
        } else {
            reconstructed = "/" + String.join("/", parts);
        }
        assertEquals(normalized, reconstructed);
    }

    @Test
    public void consistency_parentPlusBaseReconstructsPath() {
        // 对任意非根路径，getParentPath + "/" + getBaseName = 原路径
        String normalized = PathUtil.normalize("/a/b/c/d");
        String parent = PathUtil.getParentPath(normalized);
        String base = PathUtil.getBaseName(normalized);
        assertEquals(normalized, parent + "/" + base);
    }

    @Test
    public void consistency_singleLevelReconstruction() {
        // 单层路径："/" + getBaseName = 原路径
        String normalized = PathUtil.normalize("/xyz");
        assertEquals("/", PathUtil.getParentPath(normalized));
        assertEquals("xyz", PathUtil.getBaseName(normalized));
        assertEquals(normalized, "/" + PathUtil.getBaseName(normalized));
    }

    @Test
    public void consistency_lastSplitComponentEqualsBaseName() {
        // split 结果的最后一个元素 == getBaseName
        // 这不是硬编码，而是验证两个独立方法的逻辑一致性
        String[] testPaths = {"/a/b/c", "/x", "/a/.hidden", "/a/..."};
        for (String path : testPaths) {
            String normalized = PathUtil.normalize(path);
            String[] parts = PathUtil.split(normalized);
            if (parts.length > 0) {
                assertEquals(PathUtil.getBaseName(normalized),
                        parts[parts.length - 1],
                        "Mismatch for path: " + path);
            }
        }
    }

    @Test
    public void consistency_splitWithoutLastEqualsParentPath() {
        // split 结果去掉最后一个后用 "/" 拼接 == getParentPath
        String[] testPaths = {"/a/b/c", "/x/y", "/a/.hidden"};
        for (String path : testPaths) {
            String normalized = PathUtil.normalize(path);
            String[] parts = PathUtil.split(normalized);
            if (parts.length > 1) {
                String parentFromSplit = "/" + String.join("/",
                        java.util.Arrays.copyOf(parts, parts.length - 1));
                assertEquals(PathUtil.getParentPath(normalized),
                        parentFromSplit,
                        "Mismatch for path: " + path);
            }
        }
    }

    @Test
    public void consistency_normalizeThenParseMatchesDirectParse() {
        // 先 normalize 再构造 PathInfo，应与直接 parse 结果一致
        String raw = "/usr//local/./../bin";
        PathInfo direct = PathUtil.parse(raw);

        String normalized = PathUtil.normalize(raw);
        PathInfo indirect = new PathInfo(
                normalized,
                PathUtil.split(normalized),
                PathUtil.getParentPath(normalized),
                PathUtil.getBaseName(normalized)
        );

        assertEquals(direct.getNormalizedPath(), indirect.getNormalizedPath());
        assertArrayEquals(direct.getComponents(), indirect.getComponents());
        assertEquals(direct.getParentPath(), indirect.getParentPath());
        assertEquals(direct.getBaseName(), indirect.getBaseName());
    }
}
