package com.app.legend.lan_play_android.utils;

import android.os.Build;
import android.os.Process;
import android.util.Log;

public class MyUtils {

    public static boolean is64(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Process.is64Bit();
        }else {

            String[] s=Build.SUPPORTED_ABIS;

            Log.d("cpu---->>",s[0]);

            return s[0].equals("arm64");
        }

    }

}
