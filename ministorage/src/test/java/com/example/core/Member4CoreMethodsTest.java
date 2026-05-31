package com.example.core;

import com.example.fs.Directory;
import com.example.fs.File;
import com.example.fs.Link;
import com.example.fs.Node;
import com.example.path.PathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 成员4核心方法测试
 * 测试MemFs的核心私有方法：locateNode, getParentDirectory, resolveLink, getBaseName
 */
public class Member4CoreMethodsTest {

    private Directory root;
    private Object memFs;
    private Method locateNodeMethod;
    private Method getParentDirectoryMethod;
    private Method resolveLinkMethod;
    private Method getBaseNameMethod;

    @BeforeEach
    public void setUp() throws Exception {
        // 创建MemFs实例
        Class<?> memFsClass = Class.forName("com.example.MemFs");
        memFs = memFsClass.getDeclaredConstructor().newInstance();

        // 获取root字段
        java.lang.reflect.Field rootField = memFsClass.getDeclaredField("root");
        rootField.setAccessible(true);
        root = (Directory) rootField.get(memFs);

        // 获取私有方法
        locateNodeMethod = memFsClass.getDeclaredMethod("locateNode", String.class);
        locateNodeMethod.setAccessible(true);

        getParentDirectoryMethod = memFsClass.getDeclaredMethod("getParentDirectory", String.class);
        getParentDirectoryMethod.setAccessible(true);

        resolveLinkMethod = memFsClass.getDeclaredMethod("resolveLink", Node.class);
        resolveLinkMethod.setAccessible(true);

        getBaseNameMethod = memFsClass.getDeclaredMethod("getBaseName", String.class);
        getBaseNameMethod.setAccessible(true);

        // 构建测试文件树
        buildTestFileTree();
    }

    private void buildTestFileTree() {
        // 构建文件树:
        // /
        // ├─ usr/
        // │  ├─ local/
        // │  │  └─ bin/
        // │  └─ share/
        // │     └─ data.txt (100)
        // ├─ home/
        // │  └─ user/
        // │     └─ doc.txt (50)
        // └─ link_to_usr -> /usr

        Directory usr = new Directory("usr");
        Directory local = new Directory("local");
        Directory bin = new Directory("bin");
        Directory share = new Directory("share");
        File dataFile = new File("data.txt", 100);

        Directory home = new Directory("home");
        Directory user = new Directory("user");
        File docFile = new File("doc.txt", 50);

        local.putChild("bin", bin);
        share.putChild("data.txt", dataFile);
        usr.putChild("local", local);
        usr.putChild("share", share);

        user.putChild("doc.txt", docFile);
        home.putChild("user", user);

        root.putChild("usr", usr);
        root.putChild("home", home);

        // 创建链接
        Link linkToUsr = new Link("link_to_usr", usr);
        root.putChild("link_to_usr", linkToUsr);
    }

    @Test
    public void testLocateNode_Root() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/");
        assertNotNull(result, "定位根目录应该成功");
        assertSame(root, result, "应该返回root实例");
    }

    @Test
    public void testLocateNode_SimpleDirectory() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/usr");
        assertNotNull(result, "定位/usr应该成功");
        assertTrue(result instanceof Directory, "应该是目录");
        assertEquals("usr", result.getName(), "名称应该是usr");
    }

    @Test
    public void testLocateNode_NestedPath() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/usr/local/bin");
        assertNotNull(result, "定位/usr/local/bin应该成功");
        assertTrue(result instanceof Directory, "应该是目录");
        assertEquals("bin", result.getName(), "名称应该是bin");
    }

    @Test
    public void testLocateNode_File() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/usr/share/data.txt");
        assertNotNull(result, "定位文件应该成功");
        assertTrue(result instanceof File, "应该是文件");
        assertEquals("data.txt", result.getName(), "文件名应该是data.txt");
    }

    @Test
    public void testLocateNode_NonExistent() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/usr/nonexistent");
        assertNull(result, "定位不存在的路径应返回null");
    }

    @Test
    public void testLocateNode_WithDotDot() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/usr/local/../share/data.txt");
        assertNotNull(result, "定位包含..的路径应该成功");
        assertTrue(result instanceof File, "应该是文件");
        assertEquals("data.txt", result.getName(), "文件名应该是data.txt");
    }

    @Test
    public void testLocateNode_WithRedundantSlashes() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "//usr///local//bin");
        assertNotNull(result, "定位包含冗余/的路径应该成功");
        assertTrue(result instanceof Directory, "应该是目录");
        assertEquals("bin", result.getName(), "名称应该是bin");
    }

    @Test
    public void testLocateNode_WithDot() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/usr/./local/./bin");
        assertNotNull(result, "定位包含.的路径应该成功");
        assertTrue(result instanceof Directory, "应该是目录");
        assertEquals("bin", result.getName(), "名称应该是bin");
    }

    @Test
    public void testLocateNode_Link() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/link_to_usr");
        assertNotNull(result, "定位链接应该成功");
        assertTrue(result instanceof Link, "应该是链接");
        assertEquals("link_to_usr", result.getName(), "名称应该是link_to_usr");
    }

    @Test
    public void testLocateNode_InvalidPath() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "usr/local");
        assertNull(result, "非绝对路径应返回null");
    }

    @Test
    public void testGetParentDirectory_SimpleCase() throws Exception {
        Directory result = (Directory) getParentDirectoryMethod.invoke(memFs, "/usr/local");
        assertNotNull(result, "获取父目录应该成功");
        assertEquals("usr", result.getName(), "父目录名应该是usr");
    }

    @Test
    public void testGetParentDirectory_Root() throws Exception {
        Directory result = (Directory) getParentDirectoryMethod.invoke(memFs, "/");
        assertNull(result, "根目录的父目录应该是null");
    }

    @Test
    public void testGetParentDirectory_DirectChildOfRoot() throws Exception {
        Directory result = (Directory) getParentDirectoryMethod.invoke(memFs, "/usr");
        assertNotNull(result, "获取父目录应该成功");
        assertSame(root, result, "应该返回root");
    }

    @Test
    public void testGetParentDirectory_NestedPath() throws Exception {
        Directory result = (Directory) getParentDirectoryMethod.invoke(memFs, "/usr/local/bin");
        assertNotNull(result, "获取父目录应该成功");
        assertEquals("local", result.getName(), "父目录名应该是local");
    }

    @Test
    public void testGetParentDirectory_WithDotDot() throws Exception {
        Directory result = (Directory) getParentDirectoryMethod.invoke(memFs, "/usr/local/../share/data.txt");
        assertNotNull(result, "获取父目录应该成功");
        assertEquals("share", result.getName(), "父目录名应该是share");
    }

    @Test
    public void testGetParentDirectory_NonExistentParent() throws Exception {
        Directory result = (Directory) getParentDirectoryMethod.invoke(memFs, "/nonexistent/child");
        assertNull(result, "不存在的父目录应返回null");
    }

    @Test
    public void testResolveLink_File() throws Exception {
        File file = new File("test.txt", 100);
        Node result = (Node) resolveLinkMethod.invoke(memFs, file);
        assertSame(file, result, "文件应该返回自身");
    }

    @Test
    public void testResolveLink_Directory() throws Exception {
        Directory dir = new Directory("test");
        Node result = (Node) resolveLinkMethod.invoke(memFs, dir);
        assertSame(dir, result, "目录应该返回自身");
    }

    @Test
    public void testResolveLink_Link() throws Exception {
        File file = new File("target.txt", 100);
        Link link = new Link("link", file);
        Node result = (Node) resolveLinkMethod.invoke(memFs, link);
        assertSame(file, result, "链接应该返回target");
    }

    @Test
    public void testResolveLink_LinkToDirectory() throws Exception {
        Node result = (Node) locateNodeMethod.invoke(memFs, "/link_to_usr");
        assertNotNull(result, "定位链接应该成功");
        assertTrue(result instanceof Link, "应该是链接");

        Node resolved = (Node) resolveLinkMethod.invoke(memFs, result);
        assertTrue(resolved instanceof Directory, "解析后应该是目录");
        assertEquals("usr", resolved.getName(), "解析后应该是usr目录");
    }

    @Test
    public void testGetBaseName_SimpleFile() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "/usr/local/bin");
        assertEquals("bin", result, "应该返回bin");
    }

    @Test
    public void testGetBaseName_Root() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "/");
        assertNull(result, "根目录应该返回null");
    }

    @Test
    public void testGetBaseName_DirectChild() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "/usr");
        assertEquals("usr", result, "应该返回usr");
    }

    @Test
    public void testGetBaseName_WithExtension() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "/usr/share/data.txt");
        assertEquals("data.txt", result, "应该返回data.txt");
    }

    @Test
    public void testGetBaseName_WithDotDot() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "/usr/local/../share");
        assertEquals("share", result, "规范化后应该返回share");
    }

    @Test
    public void testGetBaseName_WithRedundantSlashes() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "//usr///share//");
        assertEquals("share", result, "规范化后应该返回share");
    }

    @Test
    public void testGetBaseName_InvalidPath() throws Exception {
        String result = (String) getBaseNameMethod.invoke(memFs, "usr/local");
        assertNull(result, "非绝对路径应返回null");
    }

    @Test
    public void testCoreMethodsCollaboration() throws Exception {
        // 测试核心方法的协作：定位节点 -> 解析链接 -> 获取名称
        Node linkNode = (Node) locateNodeMethod.invoke(memFs, "/link_to_usr");
        assertNotNull(linkNode, "定位链接应该成功");

        Node resolved = (Node) resolveLinkMethod.invoke(memFs, linkNode);
        assertTrue(resolved instanceof Directory, "解析后应该是目录");

        assertEquals("usr", resolved.getName(), "目录名应该是usr");
    }

    @Test
    public void testLocateAndGetParent_Collaboration() throws Exception {
        // 测试定位节点和获取父目录的协作
        Node node = (Node) locateNodeMethod.invoke(memFs, "/usr/share/data.txt");
        assertNotNull(node, "定位文件应该成功");

        Directory parent = (Directory) getParentDirectoryMethod.invoke(memFs, "/usr/share/data.txt");
        assertNotNull(parent, "获取父目录应该成功");
        assertEquals("share", parent.getName(), "父目录名应该是share");

        // 验证父目录确实包含该文件
        Node childFromParent = parent.getChild("data.txt");
        assertSame(node, childFromParent, "从父目录获取的子节点应该是同一个实例");
    }
}
