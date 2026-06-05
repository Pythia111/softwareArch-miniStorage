package com.example.command;

import com.example.fs.Directory;
import com.example.fs.Link;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 处理 FIND 命令的逻辑。
 * 在路径对应节点中递归查找名称匹配的节点，并输出匹配节点的绝对路径。
 */
public class FindCommand {

    /**
     * 执行 FIND 命令。
     * - 如果起点是文件或链接到文件，只检查该起点自身名称是否匹配
     * - 如果起点是目录或链接到目录，递归搜索该目录子树
     * - 链接节点本身也可以作为匹配结果，匹配的是链接名称
     * - 同一个底层目录在一次 FIND 中最多展开一次
     *
     * @param root       根目录节点
     * @param absPath    起始路径
     * @param targetName 要查找的节点名称
     * @return 匹配的绝对路径列表（字典序升序）
     */
    public static List<String> execute(Node root, String absPath, String targetName) {
        PathInfo pathInfo = PathUtil.parse(absPath);
        if (pathInfo == null) {
            // 非法路径，忽略
            return Collections.emptyList();
        }

        // 定位起点节点
        Node startNode = NodeResolver.resolve(root, pathInfo);
        if (startNode == null) {
            // 路径不存在，忽略
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        Set<Directory> visitedDirs = new HashSet<>();

        // 递归查找（起点节点传true，表示如果是链接需要跟随）
        String normalizedPath = pathInfo.getNormalizedPath();
        findRecursive(startNode, normalizedPath, targetName, results, visitedDirs, true);

        // 按字典序排序输出
        Collections.sort(results);
        return results;
    }

    /**
     * 递归查找匹配的节点
     *
     * 注意：只有起点节点是链接时才跟随链接，遍历过程中遇到链接节点只检查链接本身的名称，不进入链接指向的目录
     *
     * @param node        当前节点
     * @param currentPath 当前路径
     * @param targetName  目标名称
     * @param results     结果列表
     * @param visitedDirs 已访问的目录集合（防重复）
     * @param isStartNode 是否为起点节点（起点节点如果是链接需要跟随）
     */
    private static void findRecursive(Node node, String currentPath, String targetName,
                                      List<String> results, Set<Directory> visitedDirs, boolean isStartNode) {
        // 检查当前节点名称是否匹配
        if (node.getName().equals(targetName)) {
            results.add(currentPath);
        }

        // 判断是否需要跟随链接：只有起点节点是链接时才跟随
        Node target = node;
        if (isStartNode && node instanceof Link) {
            // 递归解析链接链：lnk2 -> lnk1 -> dir
            target = node;
            while (target instanceof Link) {
                target = ((Link) target).getTarget();
            }
        }

        // 如果是目录，递归搜索子节点（注意：子节点如果是链接，不跟随）
        if (target.isDirectory()) {
            Directory dir = (Directory) target;

            // 防重复：同一底层目录只展开一次
            if (visitedDirs.contains(dir)) {
                return;
            }
            visitedDirs.add(dir);

            // 递归搜索所有子节点
            for (String childName : dir.listChildren()) {
                Node child = dir.getChild(childName);
                String childPath = currentPath.equals("/")
                        ? "/" + childName
                        : currentPath + "/" + childName;
                // 子节点不是起点节点，所以isStartNode传false
                findRecursive(child, childPath, targetName, results, visitedDirs, false);
            }
        }
    }
}
