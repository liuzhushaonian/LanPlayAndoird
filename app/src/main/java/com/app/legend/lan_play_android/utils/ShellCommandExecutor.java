package com.app.legend.lan_play_android.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Android shell 命令执行器，支持无限个命令串型执行（需要有root权限！！）
 * <p>
 * <p>
 * HOW TO USE?
 * Example:修改开机启动动画。把/sdcard/Download目录下的bootanimation.zip文件拷贝到
 * /system/media目录下并修改bootanimation.zip的权限为777。
 * <p>
 * <pre>
 *      int result = new ShellCommandExecutor()
 *                  .addCommand("mount -o remount,rw /system")
 *                  .addCommand("cp /sdcard/Download/bootanimation.zip /system/media")
 *                  .addCommand("cd /system/media")
 *                  .addCommand("chmod 777 bootanimation.zip")
 *                  .execute();
 * <pre/>
 *
 * @author AveryZhong.
 */


/**
 * 以上代码来自https://blog.csdn.net/han_han_1/article/details/79556733，感谢原作者，个人懒得写了
 */
public class ShellCommandExecutor {
    private static final String TAG = "ShellCommandExecutor";

    private StringBuilder mCommands;

    private static BufferedReader osReader = null;
    private static BufferedReader osErrorReader = null;
    private String[] commands;
    private int number=-1;

    private String log="";

    private String error="";

    public String getLog() {
        return log;
    }

    public String getError() {
        return error;
    }

    public ShellCommandExecutor addCommand(String[] commands){

        this.commands=commands;

        return this;

    }

    public ShellCommandExecutor addNumber(int n){

        this.number=n;

        return this;

    }


    public ShellCommandExecutor() {
        mCommands = new StringBuilder();
    }

    public int execute() {
        return execute(mCommands.toString());
    }

    public int executePlay() {

        return executeLan(commands);
    }

    public ShellCommandExecutor addCommand(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            throw new IllegalArgumentException("command can not be null.");
        }
        mCommands.append(cmd);
        mCommands.append("\n");
        return this;
    }

    private int execute(String command) {
        int result = -1;
        DataOutputStream dos = null;
        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            dos.writeBytes(command + "\n");
            dos.flush();

            if (number!=-1) {

                dos = new DataOutputStream(p.getOutputStream());

                dos.writeBytes(number + "\n");

                dos.flush();
            }


            dos.writeBytes("exit\n");
            dos.flush();

            osReader=new BufferedReader(new InputStreamReader(p.getInputStream()));
            osErrorReader=new BufferedReader(new InputStreamReader(p.getErrorStream()));

            log=readOSMessage(osReader);

            error=readOSMessage(osErrorReader);



            LogUtils.log(command);
            p.waitFor();
            result = p.exitValue();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    private int executeLan(String[] command) {
        int result = -1;
        DataOutputStream dos = null;
        try {

            ProcessBuilder builder=new ProcessBuilder();


            File file=new File(LanApp.getContext().getFilesDir().getAbsolutePath());

            builder.directory(file);//设置改命令运行的位置，主要是在哪个文件夹内


            builder.command(command);//运行命令


            Process p =builder.start();



            p.waitFor();

            osReader=new BufferedReader(new InputStreamReader(p.getInputStream()));
            osErrorReader=new BufferedReader(new InputStreamReader(p.getErrorStream()));

            error=readOSMessage(osErrorReader);

            log=readOSMessage(osReader);

//            Log.d("rr--->>>",readOSMessage(osReader));
//            Log.d("ee--->>>",readOSMessage(osErrorReader));


            LogUtils.log(command);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }



    public static String getOsReader() {
        try {
            return readOSMessage(osReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getOsErrorReader() {
        try {
            if (osErrorReader==null){
                return "";
            }

            return readOSMessage(osErrorReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    //读取执行命令后返回的信息
    private static String readOSMessage(BufferedReader messageReader) throws IOException {
        StringBuilder content = new StringBuilder();
        String lineString;
        while ((lineString = messageReader.readLine()) != null) {

//            System.out.println("lineString : " + lineString);

            content.append(lineString).append("\n");
        }

        return content.toString();
    }

}