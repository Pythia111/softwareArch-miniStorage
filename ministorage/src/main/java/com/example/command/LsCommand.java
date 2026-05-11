package com.example.command;

import com.example.fs.Directory;
import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 处理 LS 命令的逻辑。
 */
public class LsCommand {

    /**
     * 执行 LS 命令并将结果逐行输出到标准输出。
     *
     * 约定：测试用例不会对不存在路径执行 LS。
     */
    public static void execute(Node root, String absPath) {
        List<String> lines = ls(root, absPath);
        for (String line : lines) {
            System.out.println(line);
        }
    }

    /**
     * 执行 LS 命令并返回应输出的每一行。
     * - 若目标是目录：返回其直接子节点名称（字母序）。
     * - 若目标是文件：仅返回文件名。
     * - 若目录为空：返回空列表。
     */
    public static List<String> ls(Node root, String absPath) {
        Node target = NodeResolver.resolve(root, absPath);
        if (target == null) {
            return new ArrayList<>();
        }

        if (target.isDirectory()) {
            Directory dir = (Directory) target;
            Collection<String> children = dir.listChildren();
            return new ArrayList<>(children);
        }

        List<String> out = new ArrayList<>();
        out.add(target.getName());
        return out;
    }
}
