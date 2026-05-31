package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * 微型内存文件系统主程序入口。
 * 从标准输入读取用户命令，构建文件树并输出执行结果。
 */
public class Main {
    public static void main(String[] args) {
        MemFs memFs = new MemFs();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(line);
                if (!tokenizer.hasMoreTokens()) {
                    continue;
                }

                String command = tokenizer.nextToken();
                String firstArg = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                String secondArg = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                boolean hasExtraArgs = tokenizer.hasMoreTokens();

                switch (command) {
                    case "MKDIR":
                        if (firstArg != null && secondArg == null && !hasExtraArgs) {
                            memFs.mkdir(firstArg);
                        }
                        break;

                    case "TOUCH":
                        if (firstArg != null && secondArg != null && !hasExtraArgs) {
                            try {
                                long size = Long.parseLong(secondArg);
                                if (size >= 0) {
                                    memFs.touch(firstArg, size);
                                }
                            } catch (NumberFormatException e) {
                                // 非法的文件大小，静默忽略
                            }
                        }
                        break;

                    case "LS":
                        if (firstArg != null && secondArg == null && !hasExtraArgs) {
                            memFs.ls(firstArg);
                        }
                        break;

                    case "INFO":
                        if (firstArg != null && secondArg == null && !hasExtraArgs) {
                            Long size = memFs.info(firstArg);
                            if (size != null) {
                                System.out.print(size + "\n");
                            }
                        }
                        break;

                    case "FIND":
                        if (firstArg != null && secondArg != null && !hasExtraArgs) {
                            memFs.find(firstArg, secondArg);
                        }
                        break;

                    case "RM":
                        if (firstArg != null && secondArg == null && !hasExtraArgs) {
                            memFs.rm(firstArg);
                        }
                        break;

                    case "LINK":
                        if (firstArg != null && secondArg != null && !hasExtraArgs) {
                            memFs.link(firstArg, secondArg);
                        }
                        break;

                    default:
                        // 未知命令，静默忽略
                        break;
                }
            }
        } catch (IOException e) {
            // 输入异常时按题意静默结束
        }
    }
}
