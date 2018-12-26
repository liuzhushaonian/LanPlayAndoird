package com.app.legend.lan_play_android.activities;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.lan_play_android.R;
import com.app.legend.lan_play_android.adapters.MainAdapter;
import com.app.legend.lan_play_android.bean.PreBean;
import com.app.legend.lan_play_android.fragments.PreferenceFragment;
import com.app.legend.lan_play_android.utils.Conf;
import com.app.legend.lan_play_android.utils.Database;
import com.app.legend.lan_play_android.utils.LogUtils;
import com.app.legend.lan_play_android.utils.MyUtils;
import com.app.legend.lan_play_android.utils.RootGet;
import com.app.legend.lan_play_android.utils.ShellCommandExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;


public class MainActivity extends BaseActivity {


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MainAdapter adapter;
    private Toolbar toolbar;
    private TextView pre_infos;
    private FloatingActionButton add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getComponent();

        loadFirst();

        initList();

        initToolbar();

        event();
    }

    private void getComponent() {

        recyclerView = findViewById(R.id.main_list);
        toolbar = findViewById(R.id.toolbar);
        pre_infos = findViewById(R.id.pre_infos);
        add = findViewById(R.id.add);

    }

    private void event() {

        pre_infos.setOnClickListener(v -> {

            PreferenceFragment fragment = new PreferenceFragment();
            openFragment(fragment);

        });

        add.setOnClickListener(v -> {

            PreBean bean = new PreBean();

            bean.setName("xx服务器1线-马车8");
            bean.setUrl("example.com:port or ip:port");
            adapter.addBean(bean);

            Database.getDefault().addItem(bean);

        });


    }

    private void initToolbar() {

        toolbar.setTitle("");

        setSupportActionBar(toolbar);


    }

    private void initList() {

        linearLayoutManager = new LinearLayoutManager(this);
        adapter = new MainAdapter();


        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        //item点击，启动or关闭
        adapter.setListener((position, preBean) -> {


//            showPreDialog(preBean);

            PreBean pre = adapter.getSelectBean();

            if (preBean != pre) {

                shutdown(pre);
            }

            if (preBean.getSelect() > 0) {//已经启动

                shutdown(preBean);

            } else {

                startLanPlay(preBean);
            }


        });

        //菜单点击
        adapter.setMoreClickListener((view, position, bean) -> {

            PopupMenu popupMenu = setPopupMenu(view, R.menu.item_menu);

            popupMenu.setOnMenuItemClickListener(item -> {


                switch (item.getItemId()) {

                    case R.id.edit_item:

                        showPreDialog(bean);

                        break;

                    case R.id.delete_item:

                        adapter.removeItem(bean);
                        Database.getDefault().deleteItem(bean);

                        break;

                }


                return true;
            });

            popupMenu.show();


        });

        getData();

    }

    private void getData() {

        new Thread() {
            @Override
            public void run() {
                super.run();

                List<PreBean> preBeanList = Database.getDefault().getItems();

                runOnUiThread(() -> {

                    adapter.setPreBeanList(preBeanList);

                });


            }
        }.start();

    }


    private void loadFirst() {

        boolean first = sharedPreferences.getBoolean(Conf.FIRST, true);
        if (first) {//第一次打开

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    copyFiles();

                }
            }.start();

            PreferenceFragment fragment = new PreferenceFragment();
            openFragment(fragment);
        }

    }

    private void openFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_preference, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void removeFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentManager.popBackStack();//模拟栈操作，将栈顶null去掉
        fragmentTransaction.commit();

    }

    @WorkerThread
    private void copyFiles() {
        try {
            String[] libList = getAssets().list("libs");

            for (String s : libList) {
                String srcPath = "libs/" + s;

                String newPath = getApplicationContext().getFilesDir().getAbsolutePath();

                newPath = newPath + "/libs/" + s;

                File file = new File(newPath);

                if (!file.getParentFile().exists()) {

                    file.getParentFile().mkdirs();
                }

                if (!file.exists()) {

                    InputStream inputStream = getAssets().open(srcPath);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    byte[] bytes = new byte[1024];

                    int byteCount = 0;
                    while ((byteCount = inputStream.read(bytes)) != -1) {// 循环从输入流读取
                        // buffer字节
                        fileOutputStream.write(bytes, 0, byteCount);// 将读取的输入流写入到输出流
                    }

                    fileOutputStream.flush();

                    inputStream.close();

                    fileOutputStream.close();


                }

//                Log.d("path---->>",newPath);


            }

            String[] files = getAssets().list("lan_plays");

            for (String s : files) {

                String srcPath = "lan_plays/" + s;

                String newPath = getApplicationContext().getFilesDir().getAbsolutePath();

                newPath = newPath + "/" + s;

                File file = new File(newPath);

//                if (!file.getParentFile().exists()){
//
//                    file.getParentFile().mkdirs();
//
//                }

                if (!file.exists()) {

                    InputStream inputStream = getAssets().open(srcPath);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    byte[] bytes = new byte[1024];

                    int byteCount = 0;
                    while ((byteCount = inputStream.read(bytes)) != -1) {// 循环从输入流读取
                        // buffer字节
                        fileOutputStream.write(bytes, 0, byteCount);// 将读取的输入流写入到输出流
                    }

                    fileOutputStream.flush();

                    inputStream.close();

                    fileOutputStream.close();

                }
            }

            openFileOutput("log.txt", Context.MODE_PRIVATE);//创建log文件记录pid


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void showPreDialog(PreBean bean) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.item_preference, null, false);

        EditText nameText = view.findViewById(R.id.item_name);

        EditText ip = view.findViewById(R.id.item_url);

        TextView ss = view.findViewById(R.id.textView);

        nameText.setText(bean.getName());
        ip.setText(bean.getUrl());

        builder.setView(view).setPositiveButton("确定", (dialog, which) -> {

            //获取数据，传入item
            String name = nameText.getText().toString();

            String url = ip.getText().toString();

            String s = ss.getText().toString();

            bean.setName(name);
            bean.setUrl(url);
//            bean.setSs(s);

            adapter.notifyDataSetChanged();

            Database.getDefault().updateItem(bean);

            builder.create().cancel();


        }).setNegativeButton("取消", (dialog, which) -> {
            builder.create().cancel();

        }).create().show();

    }

    protected PopupMenu setPopupMenu(View view, int reMenu) {
        PopupMenu popupMenu = new PopupMenu(this, view, 0);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(reMenu, popupMenu.getMenu());

        return popupMenu;

    }

    //启动
    private void startLanPlay(PreBean preBean) {

        //先获取lan-play的位置 /data/user/0/com.app.legend.lan_play_android/files

        String newPath = getApplicationContext().getFilesDir().getAbsolutePath();

        Log.d("path---->>>", newPath);

        //改变lan-play的权限，设为777

        //启动命令
        String startCommand = "./lan-play --relay-server-addr " + preBean.getUrl();


        new Thread() {

            @Override
            public void run() {
                super.run();

                new ShellCommandExecutor().addCommand("cd " + newPath).addCommand("chmod 777 lan-play").addCommand(startCommand).executePlay();

                String result = ShellCommandExecutor.getOsErrorReader();


                runOnUiThread(() -> {

                    if (result != null && result.contains("ERROR")) {

                        Toast.makeText(MainActivity.this, "启动失败\n" + result, Toast.LENGTH_LONG).show();

                    } else {

                        preBean.setSelect(1);

                        adapter.notifyDataSetChanged();

                    }

                });

                LogUtils.log(result);//记录


//               Log.d("result---->>>",ShellCommandExecutor.getOsErrorReader());


            }
        }.start();

    }

    //关闭操作
    private void shutdown(PreBean bean) {

        if (bean == null) {
            return;
        }

        String path = getApplicationContext().getFilesDir().getAbsolutePath();

//        String newPath=getApplicationContext().getFilesDir().getAbsolutePath()+"/lan_plays/log.txt";

        new ShellCommandExecutor().addCommand("cd " + path).addCommand("chmod 777 log.txt").execute();

        LogUtils.log(ShellCommandExecutor.getOsReader());//记录

        File file = new File(getFilesDir(), "log.txt");

//        Log.d("file_path----->>",file.getAbsolutePath());


        if (!file.exists()) {
            return;
        }

        try {

            InputStream inputStream = openFileInput("log.txt");

            StringBuffer stringBuffer = new StringBuffer();

            String line; // 用来保存每行读取的内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            line = reader.readLine(); // 读取第一行
            while (line != null) { // 如果 line 为空说明读完了
                stringBuffer.append(line); // 将读到的内容添加到 buffer 中
                stringBuffer.append("\n"); // 添加换行符
                line = reader.readLine(); // 读取下一行
            }
            reader.close();
            inputStream.close();

            String pid = stringBuffer.toString();

            String com = "kill -9 " + pid;

//            Log.d("com--->>",com);

            new ShellCommandExecutor().addCommand(com).execute();

            bean.setSelect(-1);
            adapter.notifyDataSetChanged();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void deleteLib(){

        new Thread() {

            @Override
            public void run() {
                super.run();

                if (RootGet.upgradeRootPermission(Objects.requireNonNull(getPackageCodePath()))){


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



                            com = com.substring(0, index);

                            String c = "mount -o remount -rw " + com;



                            shellCommandExecutor.addCommand(c).execute();


                        }

                        //将lib文件删除


                        String[] libList = new String[0];
                        try {
                            libList =getAssets().list("libs");

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


                            String c="rm -f "+targetPath+s;

                            Log.d("ccccc---->>>", c);

                            new ShellCommandExecutor().addCommand(c).execute();

                        }

                        //拷贝完成，恢复system

                        String c = "mount -o remount -rw " + com;

                        new ShellCommandExecutor().addCommand(c).execute();

                        runOnUiThread(()->{


                            Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();

                        });

                        LogUtils.log(log);


                    }

                }


            }
        }.start();


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.delete_lib:

                showLogs();

                break;


            case R.id.about:

                showAbout();

                break;

            case R.id.show_logs:

                showShellLogs();

                break;


        }


        return true;
    }

    private void showLogs(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view=LayoutInflater.from(this).inflate(R.layout.text,null,false);

        TextView textView=view.findViewById(R.id.text_about);

        String text=getString(R.string.delete_lib);

        textView.setText(text);

        builder.setView(view).setTitle("警告").setPositiveButton("确定要删除",(dialog, which) -> {

            deleteLib();

        }).setNegativeButton("点错了",(dialog, which) -> {
            builder.create().cancel();

        }).show();

    }

    private void showAbout(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view=LayoutInflater.from(this).inflate(R.layout.text,null,false);

        TextView textView=view.findViewById(R.id.text_about);

        String text=getString(R.string.about);

        textView.setText(text);

        builder.setView(view).setTitle("关于").show();

    }

    //显示shell的log
    private void showShellLogs(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view=LayoutInflater.from(this).inflate(R.layout.text,null,false);

        TextView textView=view.findViewById(R.id.text_about);


        textView.setText(LogUtils.getLogs());

        builder.setView(view).setTitle("日志").setPositiveButton("清除",(dialog, which) -> {

            LogUtils.cleanLogs();

            Toast.makeText(this, "清除完成", Toast.LENGTH_SHORT).show();


        }).setNegativeButton("返回",(dialog, which) -> {

            builder.create().cancel();

        }).show();

    }
}