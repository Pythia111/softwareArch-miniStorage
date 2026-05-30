package memfs;

import memfs.path.PathNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 成员3 (cyg) PathNormalizer 路径规范化单元测试
 *
 * 设计原则：
 * - 不硬编码：通过组合性验证确保方法间逻辑一致
 * - 覆盖语义陷阱：.hidden、...、混合..等
 * - normalize → split → getParentPath → getBaseName 端到端一致性
 *
 * 注意：PathNormalizer 对应开发版 com.example.path.PathUtil
 *       API 签名：normalize(String) → String（不是 String[]）
 */
class PathNormalizerTest {

    // ===== normalize() — 冗余斜杠 =====

    @Test void normalize_redundantSlashes() {
        assertEquals("/usr/local", PathNormalizer.normalize("//usr///local"));
    }

    @Test void normalize_onlySlashes() {
        assertEquals("/", PathNormalizer.normalize("///"));
    }

    @Test void normalize_doubleSlashInMiddle() {
        assertEquals("/a/b", PathNormalizer.normalize("/a//b"));
    }

    // ===== normalize() — 尾随斜杠 =====

    @Test void normalize_trailingSlash() {
        assertEquals("/usr/local", PathNormalizer.normalize("/usr/local/"));
    }

    @Test void normalize_rootTrailingSlash() {
        assertEquals("/", PathNormalizer.normalize("/"));
    }

    // ===== normalize() — . 段 =====

    @Test void normalize_dotSegment() {
        assertEquals("/usr/local", PathNormalizer.normalize("/usr/./local"));
    }

    @Test void normalize_multipleDots() {
        assertEquals("/", PathNormalizer.normalize("/././."));
    }

    @Test void normalize_dotInMiddle() {
        assertEquals("/a/b/c", PathNormalizer.normalize("/a/./b/./c"));
    }

    // ===== normalize() — .. 段 =====

    @Test void normalize_dotDotSegment() {
        assertEquals("/usr", PathNormalizer.normalize("/usr/local/.."));
    }

    @Test void normalize_dotDotInMiddle() {
        assertEquals("/a/c", PathNormalizer.normalize("/a/b/../c"));
    }

    @Test void normalize_multipleDotDot() {
        assertEquals("/a/d", PathNormalizer.normalize("/a/b/c/../../d"));
    }

    // ===== normalize() — .. 超过根 =====

    @Test void normalize_dotDotAtRoot() {
        assertEquals("/", PathNormalizer.normalize("/.."));
    }

    @Test void normalize_dotDotAboveRoot() {
        assertEquals("/", PathNormalizer.normalize("/../.."));
    }

    @Test void normalize_dotDotAboveRootWithValidPath() {
        assertEquals("/", PathNormalizer.normalize("/a/b/../../.."));
    }

    // ===== normalize() — 复合场景（规格驱动） =====

    @Test void normalize_complexPath() {
        assertEquals("/a/b", PathNormalizer.normalize("/a//b/./c/../"));
    }

    @Test void normalize_specExample_redundantSlashes() {
        assertEquals("/usr/local", PathNormalizer.normalize("//usr///local"));
    }

    @Test void normalize_specExample_dotSegment() {
        assertEquals("/usr/local/a.txt", PathNormalizer.normalize("/usr/local/./a.txt"));
    }

    @Test void normalize_specExample_dotDotSegment() {
        assertEquals("/usr/b.txt", PathNormalizer.normalize("/usr/local/../b.txt"));
    }

    @Test void normalize_specExample_complex() {
        assertEquals("/a/b", PathNormalizer.normalize("/a//b/./c/../"));
    }

    // ===== normalize() — 语义陷阱 =====

    @Test void normalize_tripleDotsIsRegularName() {
        // "..." 不是 ".."，应保留为合法路径名
        assertEquals("/a/.../b", PathNormalizer.normalize("/a/.../b"));
    }

    @Test void normalize_hiddenFileNameNotTreatedAsDot() {
        // ".hidden" 不是 "."，应保留为合法路径名
        assertEquals("/a/.hidden", PathNormalizer.normalize("/a/.hidden"));
    }

    @Test void normalize_dotSuffixInName() {
        assertEquals("/a/b.c.d", PathNormalizer.normalize("/a/b.c.d"));
    }

    @Test void normalize_dotAndDotDotMixedWithRealNames() {
        // /a/.../b/.hidden/../c → /a/.../b/c
        assertEquals("/a/.../b/c", PathNormalizer.normalize("/a/.../b/.hidden/../c"));
    }

    // ===== normalize() — 非法输入 =====

    @Test void normalize_nullReturnsNull() {
        assertNull(PathNormalizer.normalize(null));
    }

    @Test void normalize_emptyReturnsNull() {
        assertNull(PathNormalizer.normalize(""));
    }

    @Test void normalize_relativeReturnsNull() {
        assertNull(PathNormalizer.normalize("usr/local"));
    }

    // ===== normalize() — 已规范路径不变 =====

    @Test void normalize_alreadyCanonicalRoot() {
        assertEquals("/", PathNormalizer.normalize("/"));
    }

    @Test void normalize_alreadyCanonicalPath() {
        assertEquals("/a/b/c", PathNormalizer.normalize("/a/b/c"));
    }

    @Test void normalize_singleComponent() {
        assertEquals("/usr", PathNormalizer.normalize("/usr"));
    }

    // ===== split() =====

    @Test void split_rootReturnsEmptyArray() {
        assertArrayEquals(new String[]{}, PathNormalizer.split("/"));
    }

    @Test void split_singleComponent() {
        assertArrayEquals(new String[]{"usr"}, PathNormalizer.split("/usr"));
    }

    @Test void split_multiComponent() {
        assertArrayEquals(new String[]{"a", "b", "c"}, PathNormalizer.split("/a/b/c"));
    }

    @Test void split_nameWithDotsNotSplit() {
        assertArrayEquals(new String[]{"a", "b.c", "d"}, PathNormalizer.split("/a/b.c/d"));
    }

    @Test void split_hiddenFileNamePreserved() {
        assertArrayEquals(new String[]{"a", ".hidden"}, PathNormalizer.split("/a/.hidden"));
    }

    // ===== getParentPath() =====

    @Test void getParentPath_rootReturnsNull() {
        assertNull(PathNormalizer.getParentPath("/"));
    }

    @Test void getParentPath_singleComponentReturnsRoot() {
        assertEquals("/", PathNormalizer.getParentPath("/a"));
    }

    @Test void getParentPath_multiComponent() {
        assertEquals("/a/b", PathNormalizer.getParentPath("/a/b/c"));
    }

    @Test void getParentPath_chainConsistency() {
        // /a/b/c → /a/b → /a → / → null
        String path = "/a/b/c";
        path = PathNormalizer.getParentPath(path);
        assertEquals("/a/b", path);
        path = PathNormalizer.getParentPath(path);
        assertEquals("/a", path);
        path = PathNormalizer.getParentPath(path);
        assertEquals("/", path);
        path = PathNormalizer.getParentPath(path);
        assertNull(path);
    }

    // ===== getBaseName() =====

    @Test void getBaseName_rootReturnsEmpty() {
        assertEquals("", PathNormalizer.getBaseName("/"));
    }

    @Test void getBaseName_singleComponent() {
        assertEquals("a", PathNormalizer.getBaseName("/a"));
    }

    @Test void getBaseName_multiComponent() {
        assertEquals("c", PathNormalizer.getBaseName("/a/b/c"));
    }

    @Test void getBaseName_nameWithDots() {
        assertEquals("b.c.d", PathNormalizer.getBaseName("/a/b.c.d"));
    }

    @Test void getBaseName_hiddenFileName() {
        assertEquals(".hidden", PathNormalizer.getBaseName("/a/.hidden"));
    }

    @Test void getBaseName_tripleDots() {
        assertEquals("...", PathNormalizer.getBaseName("/a/..."));
    }

    // ===== 端到端一致性 =====

    @Test void consistency_splitAndJoinRoundTrip() {
        String raw = "/a//b/./c/../d/";
        String normalized = PathNormalizer.normalize(raw);
        String[] parts = PathNormalizer.split(normalized);

        String reconstructed;
        if (parts.length == 0) {
            reconstructed = "/";
        } else {
            reconstructed = "/" + String.join("/", parts);
        }
        assertEquals(normalized, reconstructed);
    }

    @Test void consistency_parentPlusBaseReconstructsPath() {
        String normalized = PathNormalizer.normalize("/a/b/c/d");
        String parent = PathNormalizer.getParentPath(normalized);
        String base = PathNormalizer.getBaseName(normalized);
        assertEquals(normalized, parent + "/" + base);
    }

    @Test void consistency_singleLevelReconstruction() {
        String normalized = PathNormalizer.normalize("/xyz");
        assertEquals("/", PathNormalizer.getParentPath(normalized));
        assertEquals("xyz", PathNormalizer.getBaseName(normalized));
        assertEquals(normalized, "/" + PathNormalizer.getBaseName(normalized));
    }

    @Test void consistency_lastSplitComponentEqualsBaseName() {
        String[] testPaths = {"/a/b/c", "/x", "/a/.hidden", "/a/..."};
        for (String path : testPaths) {
            String normalized = PathNormalizer.normalize(path);
            String[] parts = PathNormalizer.split(normalized);
            if (parts.length > 0) {
                assertEquals(PathNormalizer.getBaseName(normalized),
                        parts[parts.length - 1],
                        "Mismatch for path: " + path);
            }
        }
    }

    @Test void consistency_splitWithoutLastEqualsParentPath() {
        String[] testPaths = {"/a/b/c", "/x/y", "/a/.hidden"};
        for (String path : testPaths) {
            String normalized = PathNormalizer.normalize(path);
            String[] parts = PathNormalizer.split(normalized);
            if (parts.length > 1) {
                String parentFromSplit = "/" + String.join("/",
                        java.util.Arrays.copyOf(parts, parts.length - 1));
                assertEquals(PathNormalizer.getParentPath(normalized),
                        parentFromSplit,
                        "Mismatch for path: " + path);
            }
        }
    }
}
