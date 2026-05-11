package com.example;

import java.util.Scanner;

/**
 * 微型内存文件系统主程序入口。
 * 从标准输入读取用户命令，构建文件树并输出执行结果。
 */
public class Main {
    public static void main(String[] args) {
        MemFs memFs = new MemFs();
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0];

            switch (command) {
                case "MKDIR":
                    if (parts.length >= 2) {
                        String path = parts[1];
                        memFs.mkdir(path);
                    }
                    break;

                case "TOUCH":
                    if (parts.length >= 3) {
                        try {
                            String path = parts[1];
                            int size = Integer.parseInt(parts[2]);
                            // 只允许非负数的文件大小
                            if (size >= 0) {
                                memFs.touch(path, size);
                            }
                            // 负数静默忽略
                        } catch (NumberFormatException e) {
                            // 非法的文件大小，静默忽略
                        }
                    }
                    break;

                case "LS":
                    if (parts.length >= 2) {
                        String path = parts[1];
                        memFs.ls(path);
                    }
                    break;

                case "INFO":
                    if (parts.length >= 2) {
                        String path = parts[1];
                        memFs.info(path);
                    }
                    break;

                default:
                    // 未知命令，静默忽略
                    break;
            }
        }

        scanner.close();
    }
}
