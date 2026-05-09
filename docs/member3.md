# 成员3 工作说明

**姓名**：cyg
**负责模块**：路径工具 + MKDIR 命令
**负责文件**：`PathUtil.java`、`MkdirCommand.java`

## PathUtil.java

路径解析与规范化工具类，提供 4 个静态方法：

- `normalize(String absPath)`：去除多余斜杠（`///a//b` → `/a/b`）。已预留迭代二 `.` / `..` 处理扩展点（注释标注）。
- `split(String absPath)`：将绝对路径拆分为组件数组（`/a/b/c` → `["a","b","c"]`），根路径 `/` 返回空数组。
- `getParentPath(String absPath)`：获取父路径（`/a/b/c` → `/a/b`，`/a` → `/`，`/` → `/`）。
- `getBaseName(String absPath)`：获取末尾名称（`/a/b/c` → `c`，`/` → `""`）。

所有方法内部先调用 `normalize`，确保输入路径规范化后再处理。

## MkdirCommand.java

MKDIR 命令实现，逻辑如下：

1. 调用 `PathUtil.normalize` 规范化路径，若为 `/` 则直接返回（不能创建根目录）。
2. 调用 `getParentPath` 和 `getBaseName` 获取父路径与待创建目录名。
3. 从根节点沿路径定位父目录，若父目录不存在或不是目录则静默忽略。
4. 若父目录下已存在同名子节点则静默忽略。
5. 创建 `Directory` 实例并通过 `Directory.putChild` 加入父目录。

依赖成员2的 `Directory.putChild` / `getChild` / `isDirectory` 方法。

## 迭代二扩展点

- `PathUtil.normalize` 中已用注释标明 `.` / `..` 的处理位置，迭代二只需在该方法内补充逻辑，命令层无需改动。
