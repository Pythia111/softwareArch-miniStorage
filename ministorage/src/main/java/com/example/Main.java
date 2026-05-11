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
                        String path = parts[1];
                        int size = Integer.parseInt(parts[2]);
                        memFs.touch(path, size);
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
