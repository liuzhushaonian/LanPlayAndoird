package com.app.legend.lan_play_android.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.legend.lan_play_android.bean.PreBean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

public class Database extends SQLiteOpenHelper {

    private static volatile Database database;
    private static final String MUSICDATABASE="lan-play64-item";//数据库名称
    private static int VERSION=1;//数据库版本
    private SQLiteDatabase sqLiteDatabase;//数据库实例
    private static final String DEFAULT_TABLE="items";
    private static final String ID="id";
    private static final String NAME="name";
    private static final String URL="url";


    private static final String DEFAULT="CREATE TABLE IF NOT EXISTS "+DEFAULT_TABLE+"(" +
            ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            NAME+" TEXT NOT NULL," +
            URL+" TEXT DEFAULT ''" +
            ")";

    public static Database getDefault(){

        if (database == null) {
            synchronized (Database.class) {
                database = new Database(LanApp.getContext(), MUSICDATABASE, null, VERSION);
            }
        }

        return database;
    }

    public Database(Context context,String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sqLiteDatabase=getReadableDatabase();


    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DEFAULT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addItem(PreBean preBean){

        String sql="insert into "+DEFAULT_TABLE+" (name,url) values ('"+preBean.getName()+"','"+preBean.getUrl()+"')";

        String sqls="select last_insert_rowid() from "+DEFAULT_TABLE;

        sqLiteDatabase.execSQL(sql);
        Cursor cursor=sqLiteDatabase.rawQuery(sqls,null);


        if (cursor!=null){

            if (cursor.moveToFirst()){

                int id=cursor.getInt(0);//获取插入时的id

                preBean.setId(id);

            }
            cursor.close();

        }

    }

    public void deleteItem(PreBean preBean){

        String sql="delete from "+DEFAULT_TABLE+" where id ="+preBean.getId();

        sqLiteDatabase.execSQL(sql);



    }

    public void updateItem(PreBean preBean){

        String sql="update "+DEFAULT_TABLE+" set url='"+preBean.getUrl()+"', name = '"+preBean.getName()+"' where id = "+preBean.getId();

        sqLiteDatabase.execSQL(sql);

    }

    //获取所有的item
    public List<PreBean> getItems(){

        String sql="select * from "+DEFAULT_TABLE;

        List<PreBean> preBeans=new ArrayList<>();

        Cursor cursor=sqLiteDatabase.rawQuery(sql,null);

        if (cursor!=null){

            if (cursor.moveToFirst()){

                do {

                    String name=cursor.getString(cursor.getColumnIndex(NAME));

                    String url=cursor.getString(cursor.getColumnIndex(URL));

                    int id=cursor.getInt(cursor.getColumnIndex(ID));

                    PreBean preBean=new PreBean();

                    preBean.setUrl(url);
                    preBean.setName(name);
                    preBean.setId(id);

                    preBeans.add(preBean);


                }while (cursor.moveToNext());


            }


            cursor.close();
        }

        return preBeans;


    }



}
