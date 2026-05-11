package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;

/**
 * 微型文件系统的完整测试用例
 * 覆盖所有命令的正常场景、边界情况和异常处理
 */
public class MemFsTest {

    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    public void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    public void restoreSystemInputOutput() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    private String getOutput() {
        return testOut.toString();
    }

    /**
     * 测试示例1：基本的MKDIR和LS命令
     */
    @Test
    public void testBasicMkdirAndLs() {
        String input = "MKDIR /usr\n" +
                       "TOUCH /readme.md 50\n" +
                       "LS /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "readme.md\nusr\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试示例2：对文件执行LS
     */
    @Test
    public void testLsOnFile() {
        String input = "TOUCH /readme.md 50\n" +
                       "LS /readme.md\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "readme.md\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试示例3：INFO命令查询文件大小
     */
    @Test
    public void testInfoOnFile() {
        String input = "TOUCH /readme.md 50\n" +
                       "INFO /readme.md\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "50\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试示例4：INFO命令递归计算目录大小
     */
    @Test
    public void testInfoOnDirectory() {
        String input = "MKDIR /usr\n" +
                       "MKDIR /usr/local\n" +
                       "TOUCH /usr/local/test.txt 100\n" +
                       "TOUCH /readme.md 50\n" +
                       "INFO /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "150\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试完整示例：包含多个命令
     */
    @Test
    public void testCompleteExample() {
        String input = "MKDIR /usr\n" +
                       "MKDIR /usr/local\n" +
                       "TOUCH /usr/local/test.txt 100\n" +
                       "TOUCH /readme.md 50\n" +
                       "LS /\n" +
                       "INFO /\n" +
                       "INFO /usr\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "readme.md\nusr\n150\n100\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试TOUCH覆盖已存在的文件
     */
    @Test
    public void testTouchOverwrite() {
        String input = "TOUCH /file.txt 100\n" +
                       "INFO /file.txt\n" +
                       "TOUCH /file.txt 200\n" +
                       "INFO /file.txt\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "100\n200\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试父目录不存在时MKDIR静默失败
     */
    @Test
    public void testMkdirParentNotExist() {
        String input = "MKDIR /a/b/c\n" +
                       "LS /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试父目录不存在时TOUCH静默失败
     */
    @Test
    public void testTouchParentNotExist() {
        String input = "TOUCH /a/b/file.txt 100\n" +
                       "LS /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试空目录的LS（无输出）
     */
    @Test
    public void testLsEmptyDirectory() {
        String input = "MKDIR /empty\n" +
                       "LS /empty\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试LS输出的字母序
     */
    @Test
    public void testLsAlphabeticalOrder() {
        String input = "MKDIR /zulu\n" +
                       "MKDIR /alpha\n" +
                       "TOUCH /beta.txt 10\n" +
                       "LS /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "alpha\nbeta.txt\nzulu\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试嵌套目录结构的INFO
     */
    @Test
    public void testNestedDirectoryInfo() {
        String input = "MKDIR /a\n" +
                       "MKDIR /a/b\n" +
                       "MKDIR /a/b/c\n" +
                       "TOUCH /a/b/c/file.txt 50\n" +
                       "INFO /a\n" +
                       "INFO /a/b\n" +
                       "INFO /a/b/c\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "50\n50\n50\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试多余斜杠的处理
     */
    @Test
    public void testRedundantSlashes() {
        String input = "MKDIR ///usr///\n" +
                       "TOUCH //usr//file.txt 100\n" +
                       "LS /usr\n" +
                       "INFO ///usr///\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "file.txt\n100\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试根目录的INFO
     */
    @Test
    public void testRootInfo() {
        String input = "TOUCH /a.txt 10\n" +
                       "TOUCH /b.txt 20\n" +
                       "TOUCH /c.txt 30\n" +
                       "INFO /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "60\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试不能创建根目录
     */
    @Test
    public void testCannotCreateRoot() {
        String input = "MKDIR /\n" +
                       "TOUCH /file.txt 10\n" +
                       "LS /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "file.txt\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试混合文件和目录
     */
    @Test
    public void testMixedFilesAndDirectories() {
        String input = "MKDIR /docs\n" +
                       "TOUCH /docs/readme.md 100\n" +
                       "TOUCH /docs/guide.md 200\n" +
                       "MKDIR /src\n" +
                       "TOUCH /src/main.java 500\n" +
                       "LS /\n" +
                       "INFO /\n" +
                       "INFO /docs\n" +
                       "INFO /src\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "docs\nsrc\n800\n300\n500\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试大小为0的文件
     */
    @Test
    public void testZeroSizeFile() {
        String input = "TOUCH /empty.txt 0\n" +
                       "INFO /empty.txt\n" +
                       "INFO /\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "0\n0\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试大文件
     */
    @Test
    public void testLargeFile() {
        String input = "TOUCH /large.bin 999999999\n" +
                       "INFO /large.bin\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "999999999\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试同名目录已存在时MKDIR静默失败
     */
    @Test
    public void testMkdirDuplicateName() {
        String input = "MKDIR /test\n" +
                       "MKDIR /test\n" +
                       "TOUCH /test/file.txt 100\n" +
                       "LS /test\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "file.txt\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试复杂的目录结构
     */
    @Test
    public void testComplexStructure() {
        String input = "MKDIR /home\n" +
                       "MKDIR /home/user\n" +
                       "MKDIR /home/user/documents\n" +
                       "MKDIR /home/user/downloads\n" +
                       "TOUCH /home/user/documents/report.pdf 5000\n" +
                       "TOUCH /home/user/documents/notes.txt 1000\n" +
                       "TOUCH /home/user/downloads/movie.mp4 100000\n" +
                       "LS /home/user\n" +
                       "INFO /home/user/documents\n" +
                       "INFO /home/user\n" +
                       "INFO /home\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "documents\ndownloads\n6000\n106000\n106000\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试非法的TOUCH输入（非数字大小）
     */
    @Test
    public void testInvalidTouchSize() {
        String input = "TOUCH /file xyz\n" +
                       "INFO /\n" +
                       "TOUCH /file 2\n" +
                       "INFO /file\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "0\n2\n";
        assertEquals(expected, getOutput());
    }

    /**
     * 测试负数大小（应该被忽略）
     */
    @Test
    public void testNegativeSize() {
        String input = "TOUCH /file -100\n" +
                       "INFO /\n" +
                       "TOUCH /file 50\n" +
                       "INFO /file\n";
        provideInput(input);
        Main.main(new String[]{});

        String expected = "0\n50\n";  // 负数被忽略
        assertEquals(expected, getOutput());
    }
}
