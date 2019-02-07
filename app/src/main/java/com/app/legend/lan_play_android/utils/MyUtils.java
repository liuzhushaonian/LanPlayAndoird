package com.app.legend.lan_play_android.utils;

import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.lang.reflect.Method;

public class MyUtils {

    public static boolean is64(){

        final String tag = "is64ART";
        final String fileName = "art";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Process.is64Bit();
        }else {

            try {
                ClassLoader classLoader = LanApp.getContext().getClassLoader();
                Class<?> cls = ClassLoader.class;
                Method method = cls.getDeclaredMethod("findLibrary", String.class);
                Object object = method.invoke(classLoader, fileName);
                if (object != null) {
                    return ((String)object).contains("lib64");
                }
            } catch (Exception e) {
                //如果发生异常就用方法②
                return is64bitCPU();
            }

            return false;


        }


    }

    private static boolean is64bitCPU() {
        String CPU_ABI = null;
        if (Build.VERSION.SDK_INT >= 21) {
            String[] CPU_ABIS = Build.SUPPORTED_ABIS;
            if (CPU_ABIS.length > 0) {
                CPU_ABI = CPU_ABIS[0];
            }
        } else {
            CPU_ABI = Build.CPU_ABI;
        }

        if (CPU_ABI != null && CPU_ABI.contains("arm64")) {
            return true;
        }

        return false;
    }

}
