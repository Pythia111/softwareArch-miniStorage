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
        // 严格遵循规范：路径解析绝对交由成员3的 PathUtil 负责
        String[] pathComponents = PathUtil.split(absPath);
        Node target = findNode(root, pathComponents);

        // 如果正确找到目标节点，打印大小
        if (target != null) {
            System.out.println(target.getSize(new SizeContext()));
        }
        // 如果路径不存在，按照题目要求忽略或报错但不输出内容。
    }

    /**
     * 
     * 该方法用于根据路径定位指定的节点对象。
     * 成员1需自行完成查找逻辑。
     * 注意：由于 Directory 未在本成员代码中定义，请假设其拥有 Node getChild(String name) 方法。
     */
    public static Node findNode(Node root, String[] pathComponents) {
        Node current = root;

        for (String component : pathComponents) {
            // 跳过多余或者空的斜杠分段（预留扩展）
            if (component == null || component.isEmpty() || component.equals(".")) {
                continue;
            }
            if (component.equals("..")) {
                // 暂时简单的防御，具体 .. 逻辑可由 PathUtil 处理完毕后再传入。
                continue;
            }

            // 通过检查该节点是否为目录来继续向下走
            if (current.isDirectory()) {
                // 利用反射获取 Directory 类的 getChild 从而取到子节点。
                // 也可以强制类型转换，我们这里利用 Member 2 将提供的方法予以查找。
                try {
                    java.lang.reflect.Method getChildMethod = current.getClass().getMethod("getChild", String.class);
                    current = (Node) getChildMethod.invoke(current, component);
                    if (current == null) {
                        return null; // 路径中断，遇到不存在的子节点
                    }
                } catch (Exception e) {
                    return null;
                }
            } else {
                // 如果目前访问的是一个普通的文件而不是目录，那么就无法继续深入路径
                return null;
            }
        }

        return current;
    }
}
