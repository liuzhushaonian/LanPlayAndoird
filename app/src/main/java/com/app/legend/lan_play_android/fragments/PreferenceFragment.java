package com.app.legend.lan_play_android.fragments;


import android.content.Context;
import android.content.SharedPreferences;
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
import java.io.IOException;
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
            list = context.getAssets().list("libs64");

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

        for (int i=0;i<list.length;i++) {


            File file64_system = new File(VENDOR + "/lib64/" + list[i]);

            File file64_vendor = new File(SYSTEM + "/lib64/" + list[i]);

            if (file64_system.exists() || file64_vendor.exists()) {//64位lib存在
                lib64.setTextColor(getResources().getColor(R.color.colorGreen));
                lib64.setText("lib64状态:已存在");

                Objects.requireNonNull(getActivity())
                        .getSharedPreferences("lan-play64-android", Context.MODE_PRIVATE).edit()
                        .putBoolean(Conf.FIRST, false)
                        .apply();

            } else {

                lib64.setTextColor(getResources().getColor(R.color.colorRed));
                lib64.setText("lib64状态:不存在");

                break;

            }
        }

        for (int i=0;i<list.length;i++) {

            File file_system = new File(VENDOR + "/lib/" + list[i]);

            File file_vendor = new File(SYSTEM + "/lib/" + list[i]);

            if (file_system.exists() || file_vendor.exists()) {//lib存在
                lib.setTextColor(getResources().getColor(R.color.colorGreen));
                lib.setText("lib状态:已存在");
                Objects.requireNonNull(getActivity())
                        .getSharedPreferences("lan-play64-android", Context.MODE_PRIVATE).edit()
                        .putBoolean(Conf.FIRST, false)
                        .apply();

            } else {

                lib.setTextColor(getResources().getColor(R.color.colorRed));
                lib.setText("lib状态:不存在");

                break;

            }

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

//                        String[] mount=new String[]{"su","-c","cat","/proc/mounts"};
//
//
//                        ShellCommandExecutor shellCommandExecutor=new ShellCommandExecutor().addNumber(-1).addCommand(mount);
//
//                        shellCommandExecutor.executePlay();

                        String log = shellCommandExecutor.getLog();//获取输出信息


                        Log.d("log--->>>",log);


                        String error=shellCommandExecutor.getError();

                        if (error!=null)

                        Log.d("error--->>>",error);

                        String com = null;


                        if(error!=null&&!error.isEmpty()){

                            LogUtils.log(error);

                            Runnable runnable= () -> Toast.makeText(getContext(), "配置失败，请查阅日志寻找原因", Toast.LENGTH_SHORT).show();

                            getActivity().runOnUiThread(runnable);


                            return;
                        }

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
//
//                                String u="umount "+com;



//                        Log.d("cccc----->>>",c);

//                                shellCommandExecutor.addCommand(u).execute();

                        shellCommandExecutor.addCommand(c).execute();
//
                        log=shellCommandExecutor.getError();

                        LogUtils.log(log);

                        if (log!=null&&!log.isEmpty()){

                            String c1="mount -o rw,remount /system";//备用挂载方案

                            ShellCommandExecutor commandExecutor=new ShellCommandExecutor().addCommand(c1);

                            commandExecutor.execute();

                            String e=commandExecutor.getError();

                            if (e!=null&&!e.isEmpty()){//再次检查

                                LogUtils.log(e);


                                Runnable runnable= () -> Toast.makeText(getActivity(), "挂载失败", Toast.LENGTH_SHORT).show();

                                getActivity().runOnUiThread(runnable);

                                return;

                            }

//                            return;
                        }

//                        Log.d("log22---->>>",log);

                            }

                            //将lib文件拷贝到相关文件夹下

                            String targetPath = "";


                            String[] libList = new String[0];

                            String srcPath = getActivity().getFilesDir().getAbsolutePath()+ "/libs64/";
                            try {


                                if (MyUtils.is64()) {
                                    libList = getContext().getAssets().list("libs64");

                                    srcPath = getActivity().getFilesDir().getAbsolutePath()+ "/libs64/";

                                    targetPath = Environment.getRootDirectory().getAbsolutePath() + "/lib64/";
                                } else {

                                    libList = getContext().getAssets().list("libs");

                                    targetPath = Environment.getRootDirectory().getAbsolutePath() + "/lib/";

                                    srcPath = getActivity().getFilesDir().getAbsolutePath()+ "/lib/";
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (String s : libList) {

                                //获取文件下的lib路径

                                String path=srcPath+s;

                                String c="cp "+path+" "+targetPath;

                                Log.d("system---->>>", c);

                                new ShellCommandExecutor().addCommand(c).execute();

                            }

                            //拷贝完成，恢复system

                            String c = "mount -o remount -ro " + com;

                            String cc="mount -o ro,remount /system";

                            new ShellCommandExecutor().addCommand(c).addCommand(cc).execute();

                            SharedPreferences sharedPreferences=getActivity().getSharedPreferences("lan-play64-android",Context.MODE_PRIVATE);

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
