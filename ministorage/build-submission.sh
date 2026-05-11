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

# 4. 复制源文件（去除com/example目录，保留子目录结构）
echo "正在复制源文件（去除package声明，保留子目录结构）..."

for file in $(find src/main/java -name '*.java' | sort); do
    # 获取相对于 src/main/java/com/example 的路径
    relative_path=${file#src/main/java/com/example/}

    # 如果路径包含子目录（command/fs/path），保留子目录
    if [[ "$relative_path" == */* ]]; then
        target_path="submission/src/$relative_path"
        mkdir -p "$(dirname "$target_path")"
    else
        # 否则直接放在src根目录
        target_path="submission/src/$relative_path"
    fi

    echo "  - $relative_path"

    # 去除package声明和com.example的import语句
    sed '/^package com\.example;$/d' "$file" | \
    sed '/^package com\.example\./d' | \
    sed '/^import com\.example\./d' > "$target_path"
done

# 5. 验证Main.java存在
if [ ! -f "submission/src/Main.java" ]; then
    echo "错误: Main.java 不存在!"
    exit 1
fi

echo ""
echo "步骤 4: 验证提交文件..."

# 5. 验证Main.java在正确位置
if [ ! -f "submission/src/Main.java" ]; then
    echo "错误: submission/src/Main.java 不存在!"
    exit 1
fi

# 6. 在submission目录中测试编译
echo "正在验证提交文件可以编译..."
cd submission/src
javac $(find . -name '*.java' | sort)
if [ $? -eq 0 ]; then
    echo "✓ 编译成功"
    # 清理class文件
    find . -name '*.class' -delete
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
find submission/src -name '*.java' | sort | sed 's#^submission/src/##'
echo ""
echo "请将 submission.zip 上传到 Gradescope"
echo ""

# 8. 显示文件大小信息
echo "文件统计:"
echo "  - 源文件数量: $(find submission/src -name '*.java' | wc -l | tr -d ' ')"
echo "  - 压缩包大小: $(ls -lh submission.zip | awk '{print $5}')"
echo ""

# 9. 清理临时文件（可选）
read -p "是否删除 submission 文件夹？(y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    rm -rf submission
    echo "✓ 已删除 submission 文件夹"
else
    echo "✓ 保留 submission 文件夹供检查"
fi
echo ""