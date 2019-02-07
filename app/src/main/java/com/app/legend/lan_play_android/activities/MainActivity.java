package com.app.legend.lan_play_android.activities;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.app.legend.lan_play_android.R;
import com.app.legend.lan_play_android.adapters.MainAdapter;
import com.app.legend.lan_play_android.bean.PreBean;
import com.app.legend.lan_play_android.utils.Conf;
import com.app.legend.lan_play_android.utils.Database;
import com.app.legend.lan_play_android.utils.LogUtils;
import com.app.legend.lan_play_android.utils.MyUtils;
import com.app.legend.lan_play_android.utils.ShellCommandExecutor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends BaseActivity {


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MainAdapter adapter;
    private Toolbar toolbar;
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
        add = findViewById(R.id.add);


    }

    private void event() {

        add.setOnClickListener(v -> {

            PreBean bean = new PreBean();

            bean.setName("xx服务器1线-怪物猎人xx");
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

        }

    }



    @WorkerThread
    private void copyFiles() {

        String path="libs64";

        if (MyUtils.is64()){

            path="libs64";

        }else {

            path="libs";

        }


        try {
            String[] libList = getAssets().list(path);

            assert libList != null;
            for (String s : libList) {
                String srcPath = path +"/"+ s;

                String newPath = getApplicationContext().getFilesDir().getAbsolutePath();

                newPath = newPath + "/"+path +"/"+ s;


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


            /**
             *
             * 转移lan-play文件
             */
            String[] files = getAssets().list("lan_plays");

            assert files != null;
            for (String s : files) {

                String srcPath = "lan_plays/" + s;

                String newPath = getApplicationContext().getFilesDir().getAbsolutePath();

                newPath = newPath + "/" + s;

                File file = new File(newPath);


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


                    //转移完成后，改权限
                    String[] chmod=new String[]{"chmod","777",""+s};
                    new ShellCommandExecutor().addCommand(chmod).addNumber(-1).executePlay();


//                    Log.d("file_name--->>>",s);

                }
            }



            File file=new File(getFilesDir(),"log.txt");

            if (!file.exists()){
                openFileOutput("log.txt", Context.MODE_PRIVATE);//创建log文件记录pid
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //显示配置界面
    private void showPreDialog(PreBean bean) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.item_preference, null, false);

        EditText nameText = view.findViewById(R.id.item_name);

        EditText ip = view.findViewById(R.id.item_url);

        TextView ss = view.findViewById(R.id.textView);

        Spinner spinner=view.findViewById(R.id.spinner_list);

        /**
         * 初始化网卡列表
         */

        Toast.makeText(this, "正在初始化网卡，请授予root权限并稍等~", Toast.LENGTH_LONG).show();


        new Thread(){
            @Override
            public void run() {
                super.run();

                String lan="lan-play";

                if (MyUtils.is64()){
                    lan="lan-play64";
                }else {
                    lan="lan-play";
                }

                String[] get_list=new String[]{"su","-c","./"+lan,"--list-if"};//仅仅是获取网卡列表

                new ShellCommandExecutor().addNumber(-1).addCommand(get_list).executePlay();

                String list_s=ShellCommandExecutor.getOsReader();

                List<String> list=new ArrayList<>();

                if (list_s!=null) {

                    String[] lists = list_s.split("\n");

                    for (int i=0;i<lists.length;i++){

                        if (isStartWithNumber(lists[i])){//判断是否以数字打头
                            list.add(lists[i]);
                        }else if (list.size()!=0){

                            String last=list.get(list.size()-1);//获取最后一个，接上

                            list.remove(last);//移除

                            last=last+"\n"+lists[i];

                            list.add(last);

                        }

                    }

                }

                String[] finalList =new String[list.size()];

                for (int i=0;i<list.size();i++){

                    finalList[i]=list.get(i);

                }


                Runnable runnable= () -> {

                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_spinner_item,finalList);

                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner.setAdapter(arrayAdapter);

                    Toast.makeText(MainActivity.this, "网卡初始化完成~", Toast.LENGTH_SHORT).show();

                };

                runOnUiThread(runnable);//在主线程运行


            }
        }.start();


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                bean.setNumber(position+1);//从0开始计，所以要+1

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        nameText.setText(bean.getName());
        ip.setText(bean.getUrl());

        builder.setView(view).setPositiveButton("确定", (dialog, which) -> {

            //获取数据，传入item
            String name = nameText.getText().toString();

            String url = ip.getText().toString();

            String s = ss.getText().toString();

            bean.setName(name);
            bean.setUrl(url);


            adapter.notifyDataSetChanged();

            Database.getDefault().updateItem(bean);




        }).setNegativeButton("取消", (dialog, which) -> {


        }).create().show();

    }

    protected PopupMenu setPopupMenu(View view, int reMenu) {
        PopupMenu popupMenu = new PopupMenu(this, view, 0);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(reMenu, popupMenu.getMenu());

        return popupMenu;

    }

    //启动lan-play
    private void startLanPlay(PreBean preBean) {


        if (preBean.getNumber()<=0){

            Toast.makeText(this, "网卡尚未选择，请在修改页面选择网卡", Toast.LENGTH_SHORT).show();

            return;
        }

        //先获取lan-play的位置 /data/user/0/com.app.legend.lan_play_android/files


        String lan_play="";

        if (MyUtils.is64()){

            lan_play="lan-play64";

        }else {

            lan_play="lan-play";

        }

        //启动命令
        String[] startCommand=new String[]{"su","-c","./"+lan_play,"--relay-server-addr",preBean.getUrl()};

        preBean.setSelect(1);

        adapter.notifyDataSetChanged();


        new Thread() {

            @Override
            public void run() {
                super.run();

                new ShellCommandExecutor().addNumber(preBean.getNumber()).addCommand(startCommand).executePlay();

                String result = ShellCommandExecutor.getOsErrorReader();


                runOnUiThread(() -> {//在UI线程运行，避免在线程改动UI而崩溃

                    if (result.contains("ERROR")) {

                        preBean.setSelect(-1);

                        adapter.notifyDataSetChanged();

                        Toast.makeText(MainActivity.this, "启动失败\n" + result, Toast.LENGTH_LONG).show();

                    }

                });

                LogUtils.log(result);//记录

            }
        }.start();

    }

    //关闭操作
    private void shutdown(PreBean bean) {

        if (bean == null) {
            return;
        }

        File file = new File(getFilesDir(), "log.txt");


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


            String[] kill=new String[]{"su","-c","kill","-9",""+pid};

//            Log.d("com--->>",com);

            new ShellCommandExecutor().addNumber(-1).addCommand(kill).executePlay();

            bean.setSelect(-1);
            adapter.notifyDataSetChanged();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){


            case R.id.about:

                showAbout();

                break;

            case R.id.show_logs:

                showShellLogs();

                break;


        }


        return true;
    }

    private void showAbout(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view=LayoutInflater.from(this).inflate(R.layout.text,null,false);

        TextView textView=view.findViewById(R.id.text_about);

        String text=getString(R.string.about);


        String versionName="";

        try {

            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);

            versionName = pi.versionName;

        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }

        text=text+"\n\n当前版本："+versionName;


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


    public static boolean isStartWithNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str.charAt(0)+"");
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
