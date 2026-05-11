#!/bin/bash

# 微型内存文件系统 - 提交脚本
# 此脚本将源代码打包为submission.zip，符合Gradescope要求

set -e  # 遇到错误立即退出

echo "=== 微型内存文件系统提交准备 ==="
echo ""

# 1. 清理并编译
echo "步骤 1: 清理并编译项目..."
cd "$(dirname "$0")"
mvn clean compile

# 2. 运行测试
echo ""
echo "步骤 2: 运行测试用例..."
mvn test

# 3. 创建提交目录结构
echo ""
echo "步骤 3: 准备提交文件..."
rm -rf submission
mkdir -p submission/src

# 4. 复制源文件（不带package声明的版本）
echo "正在复制源文件（移除package声明）..."

# 处理每个Java文件，移除package声明
for file in src/main/java/com/example/*.java; do
    filename=$(basename "$file")
    # 移除package声明行，但保留其他所有内容
    sed '/^package com\.example;$/d' "$file" > "submission/src/$filename"
    echo "  - $filename"
done

# 5. 验证Main.java存在
if [ ! -f "submission/src/Main.java" ]; then
    echo "错误: Main.java 不存在!"
    exit 1
fi

echo ""
echo "步骤 4: 验证提交文件..."

# 6. 在submission目录中测试编译
echo "正在验证提交文件可以编译..."
cd submission/src
javac *.java
if [ $? -eq 0 ]; then
    echo "✓ 编译成功"
    # 清理class文件
    rm -f *.class
else
    echo "✗ 编译失败"
    exit 1
fi
cd ../..

# 7. 创建zip文件
echo ""
echo "步骤 5: 创建 submission.zip..."
rm -f submission.zip
zip -r submission.zip submission/

echo ""
echo "=== 提交准备完成 ==="
echo ""
echo "提交文件: submission.zip"
echo "包含的源文件:"
ls -1 submission/src/*.java | xargs -n1 basename
echo ""
echo "请将 submission.zip 上传到 Gradescope"
echo ""

# 8. 显示文件大小信息
echo "文件统计:"
echo "  - 源文件数量: $(ls -1 submission/src/*.java | wc -l)"
echo "  - 压缩包大小: $(ls -lh submission.zip | awk '{print $5}')"
echo ""