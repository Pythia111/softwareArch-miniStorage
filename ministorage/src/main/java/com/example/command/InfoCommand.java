package com.example.command;

import com.example.fs.Node;
import com.example.fs.NodeResolver;
import com.example.path.PathInfo;
import com.example.path.PathUtil;

/**
 * 处理 INFO 命令的逻辑。
 */
public class InfoCommand {

    /**
     * 给定根节点和路径解析出的组件数组，定位目标节点并输出其大小。
     * 由于需要其他成员提供 PathUtil.split 以及 Directory.getChild，我们这里通过组件数组形式实现。
     * 当 Member 4 整合时，可向外包装该方法并传入解析后的 String[]。
     *
     * @param root           整个系统的根目录节点
     * @param pathComponents 要查找的绝对路径各段
     */
    public static void execute(Node root, String absPath) {
        Node target = NodeResolver.resolve(root, absPath);

        // 如果正确找到目标节点，打印大小
        if (target != null) {
            System.out.println(target.size());
        }
        // 如果路径不存在，按照题目要求忽略或报错但不输出内容。
    }
}
