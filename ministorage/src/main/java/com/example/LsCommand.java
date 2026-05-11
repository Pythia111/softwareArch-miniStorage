package com.example;

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
        if (!PathUtil.isAbsolutePath(absPath)) {
            return new ArrayList<>();
        }

        String[] pathComponents = PathUtil.split(absPath);
        Node target = findNode(root, pathComponents);
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

    private static Node findNode(Node root, String[] pathComponents) {
        Node current = root;
        if (pathComponents == null) {
            return current;
        }

        for (String component : pathComponents) {
            if (component == null || component.isEmpty() || component.equals(".")) {
                continue;
            }
            if (component.equals("..")) {
                // 迭代一：不处理 ..，留给 PathUtil 在迭代二规范化。
                continue;
            }

            if (!current.isDirectory()) {
                return null;
            }

            Directory dir = (Directory) current;
            current = dir.getChild(component);
            if (current == null) {
                return null;
            }
        }

        return current;
    }
}
