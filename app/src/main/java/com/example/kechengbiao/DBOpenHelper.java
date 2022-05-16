package com.example.kechengbiao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//重写SQLiteOpenHelper类
public class DBOpenHelper extends SQLiteOpenHelper {

    private String sql;
    private ContentValues values;
    private  SQLiteDatabase db;
    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//上下文,数据库名称,
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        values = new ContentValues();
        db = database;
        initSetting();
        initCourseSchedule();
        initCourseData();


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //创建设置表单 为设置名称和设置状态
    private void initSetting(){
        //创建设置表单 为设置名称和设置状态
        sql = "create table setting(id integer primary key autoincrement,name char(20),state integar)";
        db.execSQL(sql);
        //初始化表单,填入相关参数
        //开学时间
        values.put("name","year");
        db.insert("setting",null , values);
        values.clear();
        values.put("name","month");
        db.insert("setting",null , values);
        values.clear();
        values.put("name","day");
        db.insert("setting",null , values);
        values.clear();
        //目前第几周
        values.put("name","nowweek");
        values.put("state","1");
        db.insert("setting",null , values);
        values.clear();
        //是否展示周末
        values.put("name","isshowweekend");
        values.put("state","0");
        db.insert("setting",null , values);
        values.clear();
        //学期一共多少周
        values.put("name","weeksum");
        values.put("state","16");
        db.insert("setting",null , values);
        values.clear();
        //课表节数设置
        values.put("name","msum");
        values.put("state","4");
        db.insert("setting",null , values);
        values.clear();
        values.put("name","asum");
        values.put("state","4");
        db.insert("setting",null , values);
        values.clear();
        values.put("name","esum");
        values.put("state","2");
        db.insert("setting",null , values);
        values.clear();
        //每节课时长是否相等
        values.put("name","isfixedtime");
        values.put("state","0");
        db.insert("setting",null , values);
        values.clear();
        //每一节课时间
        values.put("name","courseduration");
        values.put("state","40");
        db.insert("setting",null , values);
        values.clear();
        //课间休息时间
        values.put("name","restduration");
        values.put("state","10");
        db.insert("setting",null , values);
        values.clear();
        //上面是设置表单的初始化
    }
    //创建默认课程表时间设置表 时期上下晚对应123 开始时间 时 分
    private void initCourseSchedule(){
        //创建默认课程表时间设置表 时期上下晚对应123 开始时间 时 分
        sql ="create table courseschedule(id integer primary key autoincrement,period integer,beginhour integer,beginminute integer,endhour integer,endminute integer)";
        db.execSQL(sql);
        values.put("period","11");
        values.put("beginhour","8");
        values.put("beginminute","30");
        values.put("endhour","9");
        values.put("endminute","10");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","12");
        values.put("beginhour","9");
        values.put("beginminute","20");
        values.put("endhour","10");
        values.put("endminute","0");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","13");
        values.put("beginhour","10");
        values.put("beginminute","20");
        values.put("endhour","11");
        values.put("endminute","00");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","14");
        values.put("beginhour","11");
        values.put("beginminute","10");
        values.put("endhour","11");
        values.put("endminute","50");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","21");
        values.put("beginhour","14");
        values.put("beginminute","00");
        values.put("endhour","14");
        values.put("endminute","40");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","22");
        values.put("beginhour","14");
        values.put("beginminute","50");
        values.put("endhour","15");
        values.put("endminute","30");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","23");
        values.put("beginhour","15");
        values.put("beginminute","40");
        values.put("endhour","16");
        values.put("endminute","20");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","24");
        values.put("beginhour","16");
        values.put("beginminute","30");
        values.put("endhour","17");
        values.put("endminute","10");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","31");
        values.put("beginhour","19");
        values.put("beginminute","0");
        values.put("endhour","19");
        values.put("endminute","40");
        db.insert("courseschedule",null, values);
        values.clear();
        values.put("period","32");
        values.put("beginhour","19");
        values.put("beginminute","50");
        values.put("endhour","20");
        values.put("endminute","30");
        db.insert("courseschedule",null, values);
        values.clear();
    }
    //创建课程数据表
    private void initCourseData(){
        //创建课程数据表
        sql ="create table coursedata(id integer primary key autoincrement,name char(20),teacher varchar(20),classroom varchar(20)," +
                " begintime integer, sum integer, week char(50),color char(50),period integer )";
        db.execSQL(sql);
    }
}
