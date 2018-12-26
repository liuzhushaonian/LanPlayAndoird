package com.app.legend.lan_play_android.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.lan_play_android.R;
import com.app.legend.lan_play_android.utils.Conf;
import com.app.legend.lan_play_android.utils.LogUtils;
import com.app.legend.lan_play_android.utils.MyUtils;
import com.app.legend.lan_play_android.utils.RootGet;
import com.app.legend.lan_play_android.utils.ShellCommandExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferenceFragment extends Fragment {


    TextView root, lib64, lib, getAll;

    public PreferenceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perference, container, false);

        getComponent(view);

        initLibs(Objects.requireNonNull(getContext()));

        event();

        return view;
    }

    //获取id
    private void getComponent(View view) {

//        root = view.findViewById(R.id.root_info);
        lib64 = view.findViewById(R.id.lib64_info);
        lib = view.findViewById(R.id.lib_info);
        getAll = view.findViewById(R.id.get_all);


    }

    //查询libs
    private void initLibs(Context context) {

        String[] list = null;

        try {
            list = context.getAssets().list("libs");

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list == null) {
            return;
        }

        String SYSTEM = Environment.getRootDirectory().getAbsolutePath();

        String VENDOR = Environment.getRootDirectory().getParent() + "vendor";

//        Log.d("system--->>>",SYSTEM);
//        Log.d("vendor--->>>",VENDOR);


        File file64_system = new File(VENDOR + "/lib64/" + list[0]);

        File file64_vendor = new File(SYSTEM + "/lib64/" + list[0]);

        if (file64_system.exists() || file64_vendor.exists()) {//64位lib存在
            lib64.setTextColor(getResources().getColor(R.color.colorGreen));
            lib64.setText("lib64状态:已存在");

            Objects.requireNonNull(getActivity())
                    .getSharedPreferences("lan-play-android",Context.MODE_PRIVATE).edit()
                    .putBoolean(Conf.FIRST,false)
                    .apply();

        } else {

            lib64.setTextColor(getResources().getColor(R.color.colorRed));
            lib64.setText("lib64状态:不存在");

        }


        File file_system = new File(VENDOR + "/lib/" + list[0]);

        File file_vendor = new File(SYSTEM + "/lib/" + list[0]);

        if (file_system.exists() || file_vendor.exists()) {//lib存在
            lib.setTextColor(getResources().getColor(R.color.colorGreen));
            lib.setText("lib状态:已存在");
            Objects.requireNonNull(getActivity())
                    .getSharedPreferences("lan-play-android",Context.MODE_PRIVATE).edit()
                    .putBoolean(Conf.FIRST,false)
                    .apply();

        } else {

            lib.setTextColor(getResources().getColor(R.color.colorRed));
            lib.setText("lib状态:不存在");

        }


    }

    //事件
    private void event() {

        getAll.setOnClickListener(v -> {

            new Thread() {

                @Override
                public void run() {
                    super.run();


                    if (RootGet.upgradeRootPermission(Objects.requireNonNull(getContext()).getPackageCodePath())) {


                        ShellCommandExecutor shellCommandExecutor = new ShellCommandExecutor();

                        shellCommandExecutor.addCommand("cd proc").addCommand("cat mounts").execute();

                        String log = ShellCommandExecutor.getOsReader();//获取输出信息

                        String com = null;

                        if (log != null) {
                            String[] strings = log.split("\n");

                            for (String s : strings) {

                                if (s.contains("/system")) {

                                    com = s;

                                    break;
                                }

                            }

                            if (com != null) {

                                int index = com.indexOf("/system");
                                index = index + 7;//加上 /system 的长度

//                        Log.d("index---->>>",index+"");

                                com = com.substring(0, index);

                                String c = "mount -o remount -rw " + com;

//                        Log.d("cccc----->>>",c);

                                shellCommandExecutor.addCommand(c).execute();
//
//                        log=ShellCommandExecutor.getOsReader();

//                        Log.d("log22---->>>",log);

                            }

                            //将lib文件拷贝到相关文件夹下


                            String[] libList = new String[0];
                            try {
                                libList = getContext().getAssets().list("libs");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (String s : libList) {

                                String targetPath = "";

                                if (MyUtils.is64()) {

                                    targetPath = Environment.getRootDirectory().getAbsolutePath() + "/lib64/";
                                } else {

                                    targetPath = Environment.getRootDirectory().getAbsolutePath() + "/lib/";
                                }

                                //获取文件下的lib路径


                                String srcPath = getActivity().getFilesDir().getAbsolutePath()+"/libs/"+s;

                                String c="cp "+srcPath+" "+targetPath;

                                Log.d("system---->>>", c);

                                new ShellCommandExecutor().addCommand(c).execute();

                            }

                            //拷贝完成，恢复system

                            String c = "mount -o remount -rw " + com;

                            new ShellCommandExecutor().addCommand(c).execute();

                            SharedPreferences sharedPreferences=getActivity().getSharedPreferences("lan-play-android",Context.MODE_PRIVATE);

                            sharedPreferences.edit().putBoolean(Conf.FIRST,false).apply();

                            getActivity().runOnUiThread(()->{

                                initLibs(getContext());
                                Toast.makeText(getContext(), "配置成功", Toast.LENGTH_SHORT).show();

                            });

                            LogUtils.log(log);


                        } else {//挂载失败

                            getActivity().runOnUiThread(() -> {//失败的解决方法，弹窗提示

//                        Toast.makeText(getContext(), "挂载system分区失败，请使用完整的root或手动", Toast.LENGTH_SHORT).show();




                            });

                        }


                    }


                }
            }.start();

        });


    }


}
