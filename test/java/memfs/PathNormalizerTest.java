package memfs;

import memfs.path.PathNormalizer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PathNormalizerTest {

    @Test void rootIsValid() {
        assertArrayEquals(new String[]{}, PathNormalizer.normalize("/"));
    }

    @Test void simpleAbsolute() {
        assertArrayEquals(new String[]{"usr"}, PathNormalizer.normalize("/usr"));
    }

    @Test void redundantSlashes() {
        assertArrayEquals(new String[]{"usr","local"}, PathNormalizer.normalize("//usr///local"));
    }

    @Test void trailingSlash() {
        assertArrayEquals(new String[]{"usr","local"}, PathNormalizer.normalize("/usr/local/"));
    }

    @Test void dotSegment() {
        assertArrayEquals(new String[]{"usr","local"}, PathNormalizer.normalize("/usr/./local"));
    }

    @Test void dotDotSegment() {
        assertArrayEquals(new String[]{"usr"}, PathNormalizer.normalize("/usr/local/.."));
    }

    @Test void dotDotAtRoot() {
        assertArrayEquals(new String[]{}, PathNormalizer.normalize("/.."));
    }

    @Test void complexPath() {
        assertArrayEquals(new String[]{"a","b"}, PathNormalizer.normalize("/a//b/./c/../"));
    }

    @Test void notAbsoluteIsInvalid() {
        assertNull(PathNormalizer.normalize("usr/local"));
    }

    @Test void emptyStringIsInvalid() {
        assertNull(PathNormalizer.normalize(""));
    }

    @Test void nullIsInvalid() {
        assertNull(PathNormalizer.normalize(null));
    }
}
