# 实验：微型内存文件系统（迭代一）
时间安排   第一次迭代截止时间（暂定）：6月1日 0 a.m.

## 实验背景

日常操作系统中的文件系统（File System）是极其复杂的，但其核心数据结构却非常优雅。本次实验要求大家开发一个命令行的“微型内存文件系统”。程序将通过标准输入读取用户的终端操作指令，在内存中动态构建文件树，并输出相应的执行结果。
## 迭代一：基础目录树与常规操作
### 1. 领域模型设定
系统只有一个根目录，路径用 / 表示。
- 文件（File）：拥有名称、内容大小（整数，模拟字节数）。
- 目录（Directory）：拥有名称，可以包含多个文件或子目录。其大小是其内部所有节点大小的总和。

### 2. 基础指令集

所有路径均为绝对路径。
- MKDIR <绝对路径>：创建目录。如果父目录不存在，需报错并忽略（本迭代中“报错”定义为：不输出任何内容，直接忽略该条指令）。
- TOUCH <绝对路径> <大小>：创建文件并指定大小。如果同名文件已存在则覆盖（覆盖后大小以新值为准）。
- LS <绝对路径>：列出该路径下的所有直接子节点（按字母顺序输出节点名称）。如果目标是文件，则只输出文件名。
- INFO <绝对路径>：输出该节点的大小（如果是目录，需递归计算总大小）。

补充说明（评测范围）：
- 测试用例中不会出现对不存在路径执行 LS / INFO 的情况。

### 3. 可输出指令的“单指令”示例
说明：下面每个示例的 Standard Input 都包含少量“前置建树指令”用于构造场景；你只需要关注最后一条会产生输出的指令及其输出规则。

`LS` **示例 1：列出目录的直接子节点（按字母序）**
Standard Input:
```bash
MKDIR /usr
TOUCH /readme.md 50
LS /
```

Standard Output:
```bash
readme.md
usr
```
`LS` 示**例 2：对文件执行 LS（仅输出目标文件名）**

Standard Input:
```
TOUCH /readme.md 50
LS /readme.md
```
Standard Output:
```
readme.md
```

```INFO``` **示例 1：查询文件大小**
Standard Input:
```
TOUCH /readme.md 50
INFO /readme.md
```

Standard Output:
```
50
```

`INFO` **示例 2：查询目录总大小（递归累加）**
Standard Input:

```
MKDIR /usr
MKDIR /usr/local
TOUCH /usr/local/test.txt 100
TOUCH /readme.md 50
INFO /
```

Standard Output:
```
150
```

### 4. 标准输入输出示例（OJ 格式）
Standard Input:
```
MKDIR /usr
MKDIR /usr/local
TOUCH /usr/local/test.txt 100
TOUCH /readme.md 50
LS /
INFO /
INFO /usr
```
Standard Output:
```
readme.md
usr
150
100
```

### 提交要求
本作业的提交方式、Gradescope 加课方法与打包说明，统一见：
./submissionRequirements

## 迭代二前瞻（请提前在设计上留好扩展点）

迭代二会在迭代一基础上做如下改动：

- 路径语义增强：支持路径规范化，包括冗余 /、以及 . / ..（根目录的父目录仍为根目录）。
- 递归搜索：新增 FIND <绝对路径> <name>，在目录子树中递归查找同名节点并输出匹配的绝对路径（按字典序）。
- 删除能力：新增 RM <绝对路径>；删除目录时一般会要求目录为空（否则按题目规则报错/忽略）。
- 链接机制：新增 LINK <srcAbsPath> <dstAbsPath>，在目标位置创建“指向既存节点”的链接；从而结构可能不再是严格的树。
- 大小统计更复杂：INFO 计算目录大小时需要避免重复计数，并且要能处理链接导致的潜在环路（必须防止无限递归）。

为确保你在迭代二不被代码结构拖垮，迭代一实现建议你按以下方式组织代码（强烈建议）：

- 统一的节点抽象（Java）：用 interface Node 或 abstract class Node 统一文件/目录/链接三类节点，对外暴露 NodeType type()、long size(SizeContext ctx) 等方法，避免在主流程里散落大量 instanceof + 分支。
- 路径解析与规范化独立成类（Java）：建议实现 PathUtil.normalize(String absPath)（或 Path 值对象），把“切分/处理 . .. /去除多余 /”封装起来；迭代一先支持最小规则，迭代二只扩展该模块，不改命令处理逻辑。
- 目录作为唯一命名空间：目录类集中提供 getChild(String) / putChild(Node) / removeChild(String) / listChildren()；内部可用 Map<String, Node> 管理子节点，若要天然字典序输出可用 TreeMap 或 children.keySet().stream().sorted()。
- 为链接、去重与防环预留机制（Java）：从一开始就把 INFO 的递归遍历设计成可携带上下文，例如 SizeContext { Set<NodeId> visited; boolean followLinks; }，用 Set 做去重/防环，这样迭代二加链接后无需推翻重写。