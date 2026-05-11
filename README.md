# 微型内存文件系统 - 迭代一

这是一个命令行的微型内存文件系统实现，使用 Maven 构建。

当前开发版源码已按“入口层 / 命令层 / 文件系统模型层 / 路径层”重构；提交脚本会保留分层包目录结构导出到 `submission/src`，保证开发结构与提交结构一致。

## 项目结构

```
ministorage/
├── pom.xml                          # Maven 配置文件
├── build-submission.sh              # 提交脚本
├── src/
│   ├── main/java/com/example/      # 入口层（带 package）
│   │   ├── Main.java               # 主程序入口
│   │   ├── MemFs.java              # 文件系统外观类
│   │   ├── command/                # 命令层
│   │   │   ├── MkdirCommand.java
│   │   │   ├── TouchCommand.java
│   │   │   ├── LsCommand.java
│   │   │   └── InfoCommand.java
│   │   ├── fs/                     # 文件系统模型层
│   │   │   ├── Node.java
│   │   │   ├── File.java
│   │   │   ├── Directory.java
│   │   │   ├── NodeType.java
│   │   │   ├── SizeContext.java
│   │   │   └── NodeResolver.java
│   │   └── path/                   # 路径解析层
│   │       ├── PathUtil.java
│   │       └── PathInfo.java
│   └── test/java/com/example/      # 测试代码
│       └── ComprehensiveMemFsTest.java
└── submission/                      # 提交目录（自动生成）
    └── src/                         # 提交源代码（保留 package 目录结构）
        └── com/example/...          # 与主源码一致的包结构
```

## 成员分工

- **成员1** (sy): 核心模型 + INFO命令
  - Node.java, File.java, SizeContext.java, InfoCommand.java

- **成员2** (cx): 目录管理 + LS命令
  - Directory.java, LsCommand.java

- **成员3** (cyg): 路径工具 + MKDIR命令
  - PathUtil.java, MkdirCommand.java

- **成员4** (hly): 主程序 + TOUCH命令 + 集成打包
  - Main.java, MemFs.java, TouchCommand.java
  - 测试用例、提交脚本

## 功能实现

### 支持的命令

1. **MKDIR <绝对路径>**
   - 创建目录
   - 父目录不存在时静默忽略

2. **TOUCH <绝对路径> <大小>**
   - 创建文件并指定大小
   - 同名文件已存在则覆盖

3. **LS <绝对路径>**
   - 列出路径下的所有直接子节点（按字母序）
   - 对文件执行则只输出文件名

4. **INFO <绝对路径>**
   - 输出节点大小
   - 目录递归计算总大小

## 开发环境要求

- Java 17 或更高版本
- Maven 3.6+
- 操作系统：任意（macOS, Linux, Windows）

## 本地开发指南

### 1. 编译项目

```bash
cd ministorage
mvn clean compile
```

### 2. 运行测试

```bash
mvn test
```

项目包含 65 个完整的测试用例，覆盖：
- 基础命令功能测试
- 边界情况测试
- 异常处理测试
- 复杂场景测试

### 3. 运行程序

```bash
# 方式1: 通过 Maven
mvn exec:java -Dexec.mainClass="com.example.Main"

# 方式2: 直接运行编译后的类
cd target/classes
java com.example.Main

# 方式3: 从文件读取输入
java com.example.Main < input.txt
```

### 4. 手动测试示例

创建测试输入文件 `input.txt`:
```
MKDIR /usr
MKDIR /usr/local
TOUCH /usr/local/test.txt 100
TOUCH /readme.md 50
LS /
INFO /
INFO /usr
```

运行测试:
```bash
cd target/classes
java com.example.Main < ../../input.txt
```

期望输出:
```
readme.md
usr
150
100
```

## 提交到 Gradescope

### 自动打包（推荐）

使用提供的脚本一键完成编译、测试和打包：

```bash
./build-submission.sh
```

脚本会自动：
1. 清理并编译项目
2. 运行所有测试用例
3. 创建 `submission/` 目录
4. 递归收集分层源文件并保留包目录到 `submission/src`
5. 验证提交文件可以编译
6. 创建 `submission.zip`

### 手动打包

如果需要手动打包：

```bash
# 1. 创建提交目录
mkdir -p submission/src

# 2. 递归复制源文件并保留目录结构
for file in $(find src/main/java -name '*.java' | sort); do
    relative_path=${file#src/main/java/}
    mkdir -p "submission/src/$(dirname "$relative_path")"
    cp "$file" "submission/src/$relative_path"
done

# 3. 验证编译
cd submission/src
javac $(find . -name '*.java' | sort)
find . -name '*.class' -delete
cd ../..

# 4. 创建压缩包
zip -r submission.zip submission/

## 迭代二预留扩展点

代码中已经为迭代二的功能预留了扩展点：

1. **路径解析扩展点** - `path/PathUtil.java` 与 `path/PathInfo.java` 已独立承担路径判定与拆分
2. **防环机制** - `SizeContext` 已实现访问追踪
3. **链接支持** - `NodeType.LINK` 枚举值已定义
4. **递归搜索** - `NodeResolver` 与节点结构支持树遍历
5. **删除功能** - `Directory.removeChild()` 已实现

## 测试用例说明

### 主要覆盖范围
- MKDIR / TOUCH / LS / INFO 的正反向场景
- 覆盖行为：文件覆盖文件、文件覆盖目录、目录不可反向覆盖
- 路径合法性：根路径、尾斜杠、连续斜杠、`.`、`..`
- 深层目录大小递归与目录转文件后的大小更新
- 参数不足、参数过多、非法大小、相对路径等输入边界