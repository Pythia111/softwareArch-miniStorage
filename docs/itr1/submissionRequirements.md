你需要提交 Java 源码，入口类必须为 Main（文件名 Main.java）。

允许多文件实现，建议结构如下：
```
submission/
  src/
    Main.java
    MemFs.java
    CliParsers.java
```
也可以把 Main.java 放在提交包根目录，但必须保证：

- Main.java 存在
- 所有依赖的 .java 文件一并提交
- 不要提交编译产物（如 .class）

## 1. 打包 submission（推荐 zip）
假设你当前目录为项目根目录，且源码在 src/：
```bash

mkdir -p submission/src
cp src/*.java submission/src/
zip -r submission.zip submission
```

如果你是其它目录结构，请确保 zip 解压后仍能找到 Main.java 和相关 .java 文件。

## 2. 在 Gradescope 提交


1. 打开对应作业页面
2. 点击 Upload Submission
3. 上传你的 submission.zip
4. 等待自动评测完成并查看结果

你可以多次提交，系统以最新一次提交为准。

评测结果说明：

- hidden case 的具体输入输出与逐条失败细节仍会隐藏；
- 但结果页会额外显示一个可见汇总项 hidden-summary，用于告知你 hidden case 的通过情况（例如 Hidden cases passed: 6/8）。

## 5. 提交后自查清单
- 能否本地编译：javac *.java 或 javac src/*.java
- 程序是否从 stdin 读取、向 stdout 输出
- 是否严格按题面命令格式处理输入
- 是否存在本地路径依赖或硬编码输出

