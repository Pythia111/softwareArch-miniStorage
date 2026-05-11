package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;

/**
 * 微型文件系统的详尽测试套件
 * 基于需求文档和文件系统知识设计
 */
public class ComprehensiveMemFsTest {

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

    // ==================== MKDIR 测试 ====================

    @Test
    @DisplayName("MKDIR正面测试: 在根目录下创建目录")
    public void mkdirPositive_RootLevel() {
        provideInput("MKDIR /usr\nLS /\n");
        Main.main(new String[]{});
        assertEquals("usr\n", getOutput());
    }

    @Test
    @DisplayName("MKDIR正面测试: 创建嵌套目录")
    public void mkdirPositive_Nested() {
        provideInput("MKDIR /a\nMKDIR /a/b\nMKDIR /a/b/c\nLS /a/b\n");
        Main.main(new String[]{});
        assertEquals("c\n", getOutput());
    }

    @Test
    @DisplayName("MKDIR正面测试: 创建多个兄弟目录")
    public void mkdirPositive_Siblings() {
        provideInput("MKDIR /a\nMKDIR /b\nMKDIR /c\nLS /\n");
        Main.main(new String[]{});
        assertEquals("a\nb\nc\n", getOutput());
    }

    @Test
    @DisplayName("MKDIR负面测试: 父目录不存在")
    public void mkdirNegative_ParentNotExist() {
        provideInput("MKDIR /a/b/c\nLS /\n");
        Main.main(new String[]{});
        assertEquals("", getOutput());
    }

    @Test
    @DisplayName("MKDIR负面测试: 尝试创建根目录")
    public void mkdirNegative_CreateRoot() {
        provideInput("MKDIR /\nTOUCH /test 10\nLS /\n");
        Main.main(new String[]{});
        assertEquals("test\n", getOutput());
    }

    @Test
    @DisplayName("MKDIR负面测试: 同名目录已存在")
    public void mkdirNegative_DirectoryExists() {
        provideInput("MKDIR /test\nTOUCH /test/file 10\nMKDIR /test\nINFO /test\n");
        Main.main(new String[]{});
        assertEquals("10\n", getOutput());
    }

    @Test
    @DisplayName("MKDIR负面测试: 同名文件已存在")
    public void mkdirNegative_FileExists() {
        provideInput("TOUCH /test 100\nMKDIR /test\nINFO /test\n");
        Main.main(new String[]{});
        assertEquals("100\n", getOutput());
    }

    @Test
    @DisplayName("MKDIR边界测试: 深层嵌套路径")
    public void mkdirEdge_DeepNesting() {
        provideInput("MKDIR /a\nMKDIR /a/b\nMKDIR /a/b/c\nMKDIR /a/b/c/d\nMKDIR /a/b/c/d/e\nLS /a/b/c/d\n");
        Main.main(new String[]{});
        assertEquals("e\n", getOutput());
    }

    // ==================== TOUCH 测试 ====================

    @Test
    @DisplayName("TOUCH正面测试: 创建普通文件")
    public void touchPositive_CreateFile() {
        provideInput("TOUCH /file.txt 100\nINFO /file.txt\n");
        Main.main(new String[]{});
        assertEquals("100\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH正面测试: 覆盖同名文件")
    public void touchPositive_OverwriteFile() {
        provideInput("TOUCH /file 50\nINFO /file\nTOUCH /file 100\nINFO /file\n");
        Main.main(new String[]{});
        assertEquals("50\n100\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH正面测试: 覆盖同名目录（目录到文件转换）")
    public void touchPositive_OverwriteDirectory() {
        provideInput("MKDIR /dir\nTOUCH /dir/file 50\nINFO /dir\nTOUCH /dir 100\nINFO /dir\n");
        Main.main(new String[]{});
        assertEquals("50\n100\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH正面测试: 大小为0的文件")
    public void touchPositive_ZeroSize() {
        provideInput("TOUCH /empty 0\nINFO /empty\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH正面测试: 大文件")
    public void touchPositive_LargeFile() {
        provideInput("TOUCH /large 999999999\nINFO /large\n");
        Main.main(new String[]{});
        assertEquals("999999999\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH正面测试: 超过int范围的大文件")
    public void touchPositive_VeryLargeFile() {
        provideInput("TOUCH /huge 2147483648\nINFO /huge\n");
        Main.main(new String[]{});
        assertEquals("2147483648\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH正面测试: 嵌套路径中创建文件")
    public void touchPositive_NestedPath() {
        provideInput("MKDIR /a\nMKDIR /a/b\nTOUCH /a/b/file 50\nINFO /a/b/file\n");
        Main.main(new String[]{});
        assertEquals("50\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 父目录不存在")
    public void touchNegative_ParentNotExist() {
        provideInput("TOUCH /a/b/file 100\nLS /\n");
        Main.main(new String[]{});
        assertEquals("", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 尝试在根目录创建文件")
    public void touchNegative_CreateAtRoot() {
        provideInput("TOUCH / 100\nMKDIR /test\nLS /\n");
        Main.main(new String[]{});
        assertEquals("test\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 负数大小")
    public void touchNegative_NegativeSize() {
        provideInput("TOUCH /file -100\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 非数字大小")
    public void touchNegative_NonNumericSize() {
        provideInput("TOUCH /file abc\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 不接受正号前缀")
    public void touchNegative_PlusPrefixedSize() {
        provideInput("TOUCH /file +7\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 不接受负零")
    public void touchNegative_NegativeZeroSize() {
        provideInput("TOUCH /file -0\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH负面测试: 空字符串大小")
    public void touchNegative_EmptySize() {
        provideInput("TOUCH /file\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("TOUCH边界测试: 多次覆盖同一文件")
    public void touchEdge_MultipleOverwrites() {
        provideInput("TOUCH /file 10\nTOUCH /file 20\nTOUCH /file 30\nINFO /file\n");
        Main.main(new String[]{});
        assertEquals("30\n", getOutput());
    }

    // ==================== LS 测试 ====================

    @Test
    @DisplayName("LS正面测试: 列出根目录")
    public void lsPositive_Root() {
        provideInput("MKDIR /usr\nTOUCH /readme 10\nLS /\n");
        Main.main(new String[]{});
        assertEquals("readme\nusr\n", getOutput());
    }

    @Test
    @DisplayName("LS正面测试: 列出空目录")
    public void lsPositive_EmptyDirectory() {
        provideInput("MKDIR /empty\nLS /empty\n");
        Main.main(new String[]{});
        assertEquals("", getOutput());
    }

    @Test
    @DisplayName("LS正面测试: 列出非空目录")
    public void lsPositive_NonEmptyDirectory() {
        provideInput("MKDIR /dir\nTOUCH /dir/file1 10\nTOUCH /dir/file2 20\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("file1\nfile2\n", getOutput());
    }

    @Test
    @DisplayName("LS正面测试: 对文件执行LS")
    public void lsPositive_OnFile() {
        provideInput("TOUCH /file 10\nLS /file\n");
        Main.main(new String[]{});
        assertEquals("file\n", getOutput());
    }

    @Test
    @DisplayName("LS正面测试: 字母序排序")
    public void lsPositive_AlphabeticalOrder() {
        provideInput("MKDIR /z\nMKDIR /a\nTOUCH /m 10\nLS /\n");
        Main.main(new String[]{});
        assertEquals("a\nm\nz\n", getOutput());
    }

    @Test
    @DisplayName("LS边界测试: 只有文件的目录")
    public void lsEdge_OnlyFiles() {
        provideInput("MKDIR /dir\nTOUCH /dir/a 1\nTOUCH /dir/b 2\nTOUCH /dir/c 3\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("a\nb\nc\n", getOutput());
    }

    @Test
    @DisplayName("LS边界测试: 只有目录的目录")
    public void lsEdge_OnlyDirectories() {
        provideInput("MKDIR /dir\nMKDIR /dir/a\nMKDIR /dir/b\nMKDIR /dir/c\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("a\nb\nc\n", getOutput());
    }

    @Test
    @DisplayName("LS边界测试: 混合文件和目录")
    public void lsEdge_Mixed() {
        provideInput("MKDIR /dir\nMKDIR /dir/subdir\nTOUCH /dir/file 10\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("file\nsubdir\n", getOutput());
    }

    @Test
    @DisplayName("LS边界测试: 特殊字符命名")
    public void lsEdge_SpecialNames() {
        provideInput("MKDIR /dir\nTOUCH /dir/file.txt 10\nTOUCH /dir/file-1 10\nTOUCH /dir/file_2 10\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("file-1\nfile.txt\nfile_2\n", getOutput());
    }

    // ==================== INFO 测试 ====================

    @Test
    @DisplayName("INFO正面测试: 查询文件大小")
    public void infoPositive_FileSize() {
        provideInput("TOUCH /file 123\nINFO /file\n");
        Main.main(new String[]{});
        assertEquals("123\n", getOutput());
    }

    @Test
    @DisplayName("INFO正面测试: 查询空目录")
    public void infoPositive_EmptyDirectory() {
        provideInput("MKDIR /empty\nINFO /empty\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("INFO正面测试: 查询非空目录")
    public void infoPositive_NonEmptyDirectory() {
        provideInput("MKDIR /dir\nTOUCH /dir/file1 10\nTOUCH /dir/file2 20\nINFO /dir\n");
        Main.main(new String[]{});
        assertEquals("30\n", getOutput());
    }

    @Test
    @DisplayName("INFO正面测试: 递归计算嵌套目录")
    public void infoPositive_NestedDirectory() {
        provideInput("MKDIR /a\nMKDIR /a/b\nMKDIR /a/b/c\nTOUCH /a/b/c/file 50\nINFO /a\n");
        Main.main(new String[]{});
        assertEquals("50\n", getOutput());
    }

    @Test
    @DisplayName("INFO正面测试: 根目录大小")
    public void infoPositive_RootSize() {
        provideInput("TOUCH /file1 10\nTOUCH /file2 20\nMKDIR /dir\nTOUCH /dir/file3 30\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("60\n", getOutput());
    }

    @Test
    @DisplayName("INFO边界测试: 大小为0的文件")
    public void infoEdge_ZeroSizeFile() {
        provideInput("TOUCH /zero 0\nINFO /zero\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("INFO边界测试: 深层嵌套目录")
    public void infoEdge_DeepNesting() {
        provideInput("MKDIR /a\nMKDIR /a/b\nMKDIR /a/b/c\nMKDIR /a/b/c/d\n" +
                    "TOUCH /a/file1 10\nTOUCH /a/b/file2 20\nTOUCH /a/b/c/file3 30\nTOUCH /a/b/c/d/file4 40\n" +
                    "INFO /a\n");
        Main.main(new String[]{});
        assertEquals("100\n", getOutput());
    }

    @Test
    @DisplayName("INFO边界测试: 多个文件和目录混合")
    public void infoEdge_ComplexStructure() {
        provideInput("MKDIR /home\nMKDIR /home/user\n" +
                    "TOUCH /home/user/doc1 100\nTOUCH /home/user/doc2 200\n" +
                    "MKDIR /home/user/pics\nTOUCH /home/user/pics/img1 300\n" +
                    "INFO /home\n");
        Main.main(new String[]{});
        assertEquals("600\n", getOutput());
    }

    // ==================== 路径处理测试 ====================

    @Test
    @DisplayName("路径测试: 多余斜杠")
    public void pathTest_RedundantSlashes() {
        provideInput("MKDIR ///usr///\nTOUCH //usr//file 100\nINFO /usr\nLS ///\n");
        Main.main(new String[]{});
        assertEquals("100\nusr\n", getOutput());  // LS /// 列出根目录，只有usr
    }

    @Test
    @DisplayName("路径测试: 根目录多种表示")
    public void pathTest_RootVariations() {
        provideInput("TOUCH /file1 10\nINFO /\nINFO //\nINFO ///\n");
        Main.main(new String[]{});
        assertEquals("10\n10\n10\n", getOutput());
    }

    @Test
    @DisplayName("路径测试: 末尾斜杠")
    public void pathTest_TrailingSlash() {
        provideInput("MKDIR /dir/\nTOUCH /dir/file/ 100\nLS /dir/\n");
        Main.main(new String[]{});
        assertEquals("file\n", getOutput());
    }

    @Test
    @DisplayName("路径测试: 点号在迭代一中视为普通名称")
    public void pathTest_DotAsLiteralName() {
        provideInput("MKDIR /dir\nTOUCH /dir/. 100\nINFO /dir/.\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("100\n.\n", getOutput());
    }

    @Test
    @DisplayName("路径测试: 双点在迭代一中视为普通名称")
    public void pathTest_DotDotAsLiteralName() {
        provideInput("MKDIR /dir\nTOUCH /dir/.. 7\nINFO /dir/..\nLS /dir\n");
        Main.main(new String[]{});
        assertEquals("7\n..\n", getOutput());
    }

    @Test
    @DisplayName("路径测试: 点路径只做多余斜杠规范化")
    public void pathTest_DotPathsDoNotNavigate() {
        provideInput("MKDIR /a\nMKDIR /a/..\nLS /a\n");
        Main.main(new String[]{});
        assertEquals("..\n", getOutput());
    }

    // ==================== 覆盖行为测试 ====================

    @Test
    @DisplayName("覆盖测试: 文件覆盖文件")
    public void overwriteTest_FileToFile() {
        provideInput("TOUCH /test 100\nINFO /test\nTOUCH /test 200\nINFO /test\n");
        Main.main(new String[]{});
        assertEquals("100\n200\n", getOutput());
    }

    @Test
    @DisplayName("覆盖测试: 文件覆盖目录")
    public void overwriteTest_FileToDirectory() {
        provideInput("MKDIR /test\nTOUCH /test/file 50\nINFO /test\nTOUCH /test 100\nINFO /test\nLS /\n");
        Main.main(new String[]{});
        assertEquals("50\n100\ntest\n", getOutput());
    }

    @Test
    @DisplayName("覆盖测试: 目录不能覆盖文件")
    public void overwriteTest_DirectoryCannotOverwriteFile() {
        provideInput("TOUCH /test 100\nMKDIR /test\nINFO /test\n");
        Main.main(new String[]{});
        assertEquals("100\n", getOutput());
    }

    @Test
    @DisplayName("覆盖测试: 目录不能覆盖目录")
    public void overwriteTest_DirectoryCannotOverwriteDirectory() {
        provideInput("MKDIR /test\nTOUCH /test/file 50\nMKDIR /test\nINFO /test\n");
        Main.main(new String[]{});
        assertEquals("50\n", getOutput());
    }

    // ==================== 综合场景测试 ====================

    @Test
    @DisplayName("综合测试: 标准示例")
    public void integrationTest_StandardExample() {
        provideInput("MKDIR /usr\nMKDIR /usr/local\n" +
                    "TOUCH /usr/local/test.txt 100\nTOUCH /readme.md 50\n" +
                    "LS /\nINFO /\nINFO /usr\n");
        Main.main(new String[]{});
        assertEquals("readme.md\nusr\n150\n100\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 复杂操作序列")
    public void integrationTest_ComplexSequence() {
        provideInput("MKDIR /a\nMKDIR /b\n" +
                    "TOUCH /a/f1 10\nTOUCH /b/f2 20\n" +
                    "MKDIR /a/sub\nTOUCH /a/sub/f3 30\n" +
                    "INFO /a\nINFO /b\nINFO /\nLS /\n");
        Main.main(new String[]{});
        assertEquals("40\n20\n60\na\nb\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 目录到文件转换后的大小计算")
    public void integrationTest_SizeAfterConversion() {
        provideInput("MKDIR /dir\nTOUCH /dir/f1 10\nTOUCH /dir/f2 20\n" +
                    "INFO /\nTOUCH /dir 100\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("30\n100\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 空命令和空行处理")
    public void integrationTest_EmptyLines() {
        provideInput("\n\nMKDIR /test\n\nLS /\n\n");
        Main.main(new String[]{});
        assertEquals("test\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 参数不足的命令")
    public void integrationTest_InsufficientArguments() {
        provideInput("MKDIR\nTOUCH /file\nLS\nINFO\nMKDIR /test\nLS /\n");
        Main.main(new String[]{});
        assertEquals("test\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 参数过多的命令应忽略")
    public void integrationTest_TooManyArguments() {
        provideInput("MKDIR /dir extra\nTOUCH /file 10 extra\nLS / extra\nINFO / extra\nINFO /\n");
        Main.main(new String[]{});
        assertEquals("0\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 非绝对路径应忽略")
    public void integrationTest_NonAbsolutePathsIgnored() {
        provideInput("MKDIR dir\nTOUCH file 10\nMKDIR /ok\nINFO /\nLS /\n");
        Main.main(new String[]{});
        assertEquals("0\nok\n", getOutput());
    }

    @Test
    @DisplayName("综合测试: 未知命令")
    public void integrationTest_UnknownCommands() {
        provideInput("UNKNOWN /test\nMKDIR /dir\nDELETE /dir\nLS /\n");
        Main.main(new String[]{});
        assertEquals("dir\n", getOutput());
    }

    @Test
    @DisplayName("边界测试: 同名节点在不同父目录")
    public void edgeTest_SameNameDifferentParents() {
        provideInput("MKDIR /a\nMKDIR /b\n" +
                    "TOUCH /a/file 10\nTOUCH /b/file 20\n" +
                    "INFO /a/file\nINFO /b/file\n");
        Main.main(new String[]{});
        assertEquals("10\n20\n", getOutput());
    }

    @Test
    @DisplayName("边界测试: 非常长的路径")
    public void edgeTest_VeryLongPath() {
        provideInput("MKDIR /a\nMKDIR /a/b\nMKDIR /a/b/c\nMKDIR /a/b/c/d\n" +
                    "MKDIR /a/b/c/d/e\nMKDIR /a/b/c/d/e/f\n" +
                    "TOUCH /a/b/c/d/e/f/file 100\nINFO /a/b/c/d/e/f/file\n");
        Main.main(new String[]{});
        assertEquals("100\n", getOutput());
    }

    @Test
    @DisplayName("边界测试: 大量兄弟节点")
    public void edgeTest_ManySiblings() {
        StringBuilder input = new StringBuilder("MKDIR /dir\n");
        for (int i = 0; i < 100; i++) {
            input.append("TOUCH /dir/file").append(i).append(" 1\n");
        }
        input.append("INFO /dir\n");
        provideInput(input.toString());
        Main.main(new String[]{});
        assertEquals("100\n", getOutput());
    }
}
