package com.zhekasmirnov.horizon.compiler;

import com.pdaxrom.utils.Utils;
import com.zhekasmirnov.horizon.compiler.packages.Environment;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shell {
    @SuppressWarnings("unused")
    private static final String TAG = "Shell";

    private Shell() {
    }

    public static void execAndPrintResult(Object context, String str, String str2) {
        PrintStream printStream = System.out;
        printStream.println("executing: " + str2);
        PrintStream printStream2 = System.out;
        printStream2.println("execution result: " + exec(context, str, str2));
    }

    public static CommandResult exec(Object context, File file) {
        PrintStream printStream = System.out;
        printStream.println("can exec " + file.canExecute() + " " + file.getParentFile().canExecute());
        if (!file.canExecute()) {
            Utils.chmod(file.getAbsolutePath(), 509);
        }
        return exec(context, file.getParent(), file.getPath());
    }

    @SuppressWarnings("deprecation")
    public static CommandResult exec2(Object context, String str, String str2) {
        try {
            Process exec = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());
            dataOutputStream.writeBytes("exec " + str2 + "\n");
            dataOutputStream.flush();
            exec.waitFor();
            System.out.println("begin process output:");
            DataInputStream dataInputStream = new DataInputStream(exec.getInputStream());
            while (true) {
                String readLine = dataInputStream.readLine();
                if (readLine == null) {
                    break;
                }
                System.out.println(readLine);
            }
            DataInputStream dataInputStream2 = new DataInputStream(exec.getErrorStream());
            while (true) {
                String readLine2 = dataInputStream2.readLine();
                if (readLine2 == null) {
                    return null;
                }
                System.err.println(readLine2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static CommandResult exec(Object context, String str, String str2) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            String[] strArr = {"/system/bin/sh"};
            int[] iArr = new int[1];
            FileDescriptor createSubProcess = Utils.createSubProcess(str, strArr[0], strArr, Environment.buildDefaultEnv(context), iArr);
            final int i = iArr[0];
            if (i <= 0) {
                return new CommandResult(-1, "Could not create sub process");
            }
            Utils.setPtyUTF8Mode(createSubProcess, true);
            Utils.setPtyWindowSize(createSubProcess, 128, 1024, 0, 0);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(createSubProcess));
            FileOutputStream fileOutputStream = new FileOutputStream(createSubProcess);
            fileOutputStream.write("export PS1=''\n".getBytes("UTF-8"));
            fileOutputStream.write(("exec " + str2 + "\n").getBytes("UTF-8"));
            fileOutputStream.flush();
            final int[] iArr2 = new int[1];
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    iArr2[0] = Utils.waitFor(i);
                }
            });
            thread.start();
            StringBuilder sb = new StringBuilder();
            int i2 = 6;
            Pattern compile = Pattern.compile("(\\x08)\\1+");
            do {
                try {
                    String replaceAll = bufferedReader.readLine().replaceAll("\u001b\\[([0-9]|;)*m", "");
                    Matcher matcher = compile.matcher(replaceAll);
                    if (matcher.find()) {
                        int start = matcher.start();
                        int end = matcher.end() - matcher.start();
                        if (start > end) {
                            replaceAll = String.valueOf(replaceAll.substring(0, matcher.start() - end)) + replaceAll.substring(matcher.end());
                        }
                    }
                    if (i2 > 0) {
                        i2--;
                    } else {
                        sb.append(replaceAll);
                        sb.append("\n");
                    }
                } catch (IOException e) {
                }
            } while (thread.isAlive());
            fileOutputStream.close();
            bufferedReader.close();
            CommandResult commandResult = new CommandResult(iArr2[0], sb.toString());
            commandResult.setTime(System.currentTimeMillis() - currentTimeMillis);
            return commandResult;
        } catch (Throwable th) {
            th.printStackTrace();
            return new CommandResult(-1, th.getMessage());
        }
    }
}
