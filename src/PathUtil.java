/**
 * 这是成员3负责的路径工具类（此处仅作为成员1开发时的桩代码/Placeholder）。
 * 成员1在 InfoCommand 中直接调用此类的 split 方法，以满足“路径解析交由成员3”的规范。
 */
public class PathUtil {
    public static String[] split(String absPath) {
        if (absPath == null || absPath.equals("/")) {
            return new String[0];
        }
        return absPath.split("/");
    }
}
