package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //定义Fragment对象
    private Fragment fragment_1,fragment_2,fragment_3,nowFragment;
    private TextView tab_1,tab_2,tab_3;
    public int selectWeek =1,nowWeek=1;

    Calendar calendar;
    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;
    public int msum,asum,esum,weekSum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注意db要先打开 先初始化 否则有些数据没初始化没法读取
        dbOpenHelper = new DBOpenHelper(this, "data.db", null, 1);
        db = dbOpenHelper.getWritableDatabase();
        calendar = Calendar.getInstance();
        initNowWeek();
        selectWeek = nowWeek;//默认选择的周就是当前周
        initUI();
        getInitData();
    }
    //初始化UI
    private  void initUI(){
        tab_1 = findViewById(R.id.tv_today);
        tab_2 = findViewById(R.id.tv_kcb);
        tab_3 = findViewById(R.id.tv_setting);


        tab_1.setBackgroundColor(Color.rgb(222,222,222));
        tab_2.setBackgroundColor(Color.rgb(255,255,255));
        tab_3.setBackgroundColor(Color.rgb(255,255,255));

        //设置点击事件
        tab_1.setOnClickListener(this);
        tab_2.setOnClickListener(this);
        tab_3.setOnClickListener(this);


        //显示fragment
        showFragment1();




    }

    //初始化现在是第几周
    private void initNowWeek(){
        //获取开学时间
        int starYear,starDay,starMonth;
        Cursor cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"year"},
                null,
                null,
                null);
        //如果还未初始化开学时间
        if(cursor.getCount()==0){

            selectWeek = -1;
            return;
        }
        cursor.moveToFirst();
        starYear = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"day"},
                null,
                null,
                null);
        cursor.moveToFirst();
        starDay = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"month"},
                null,
                null,
                null);
        cursor.moveToFirst();
        starMonth = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        String beginDateStr,endDateStr;
        beginDateStr = starYear+"-";
        beginDateStr += (starMonth<10?"0"+starMonth:String.valueOf(starMonth))+"-";
        beginDateStr += (starDay<10?"0"+starDay:String.valueOf(starDay));
        starDay = calendar.get(Calendar.DAY_OF_MONTH);
        starMonth = calendar.get(Calendar.MONTH)+1; //从0开始
        starYear = calendar.get(Calendar.YEAR);
        endDateStr = starYear+"-";
        endDateStr += (starMonth<10?"0"+starMonth:String.valueOf(starMonth))+"-";
        endDateStr += (starDay<10?"0"+starDay:String.valueOf(starDay));

        long day = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate;
        Date endDate;

        firstDay = Calendar.getInstance();

        try {

            beginDate = format.parse(beginDateStr);
            endDate = format.parse(endDateStr);
            //获取开学时间是周几 进行偏移计算
            Calendar cal = Calendar.getInstance();
            cal.setTime(beginDate);
            int week = cal.get(Calendar.DAY_OF_WEEK);
            week = week==1? 7:week-1;
            day = (endDate.getTime()-beginDate.getTime())/(24*60*60*1000) +week-1;//调整为开学周的周一那一天 -1是为了求模的时候算对
            firstDay.setTime(beginDate);
            firstDay.add(Calendar.DATE,1-week);
        } catch (ParseException e) {

            e.printStackTrace();
        }
        nowWeek = (int) day/7+1;

    }

    public Calendar firstDay;//开学那一周的周一是什么时候

    //显示框架的代码 用于切换界面
    private void showFragment1(){
        //开启事务
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //判断Fragment是否为空
        if(fragment_1 == null){
            fragment_1 = new TodayFragment();
            transaction.add(R.id.content_layout,fragment_1);
        }
        hideAllFragment(transaction);
        transaction.show(fragment_1);

        //记录当前的fragment
        nowFragment = fragment_1;
        transaction.commit();

        //变换按钮颜色
        tab_1.setBackgroundColor(Color.rgb(222,222,222));
        tab_2.setBackgroundColor(Color.rgb(255,255,255));
        tab_3.setBackgroundColor(Color.rgb(255,255,255));


    }
    private void showFragment2(){

        //开启事务
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //判断Fragment是否为空
        if(fragment_2 == null){

            fragment_2 = new KcbFragment();

            transaction.add(R.id.content_layout,fragment_2);
        }
        hideAllFragment(transaction);
        transaction.show(fragment_2);

        //记录当前的fragment
        nowFragment = fragment_2;
        transaction.commit();

        //变换按钮颜色
        tab_2.setBackgroundColor(Color.rgb(222,222,222));
        tab_1.setBackgroundColor(Color.rgb(255,255,255));
        tab_3.setBackgroundColor(Color.rgb(255,255,255));


    }
    private void showFragment3(){
        //开启事务
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //判断Fragment是否为空
        if(fragment_3 == null){
            fragment_3 = new SettingFragment();
            transaction.add(R.id.content_layout,fragment_3);


        }
        hideAllFragment(transaction);
        transaction.show(fragment_3);

        //记录当前的fragment
        nowFragment = fragment_3;
        transaction.commit();

        //变换按钮颜色

        tab_3.setBackgroundColor(Color.rgb(222,222,222));
        tab_2.setBackgroundColor(Color.rgb(255,255,255));
        tab_1.setBackgroundColor(Color.rgb(255,255,255));


    }

    //隐藏先前的fragment
    private void hideAllFragment(FragmentTransaction transaction){
        if(fragment_1!=null)transaction.hide(fragment_1);
        if(fragment_2!=null)transaction.hide(fragment_2);
        if(fragment_3!=null)transaction.hide(fragment_3);
    }



    //监听点击事件 用于底部导航的监听
    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.tv_today)showFragment1();
        else if(view.getId()==R.id.tv_kcb)showFragment2();
        else if(view.getId()==R.id.tv_setting)showFragment3();

    }

    //重新载入课程表 和今日课程 来响应设置的修改
    public void reFragment(int theWeek){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        selectWeek = theWeek;
        //删除fragment
        if(fragment_2!=null){
            transaction.remove(fragment_2);
            fragment_2 = new KcbFragment();
            transaction.add(R.id.content_layout,fragment_2);
        }
        if(fragment_1!=null){
            transaction.remove(fragment_1);
            fragment_1 = new TodayFragment();
            transaction.add(R.id.content_layout,fragment_1);
        }
        transaction.commit();
        initNowWeek();
    }
    //重载 如果不传参数 则按当前的周来更改
    public void reFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        initNowWeek();
        selectWeek = nowWeek;
        //删除fragment
        if(fragment_2!=null){
            transaction.remove(fragment_2);
            fragment_2 = new KcbFragment();
            transaction.add(R.id.content_layout,fragment_2);
        }
        if(fragment_1!=null){
            transaction.remove(fragment_1);
            fragment_1 = new TodayFragment();
            transaction.add(R.id.content_layout,fragment_1);
        }
        transaction.commit();

    }

    //获取数据库数据
    private void getInitData(){
        //获取有多少节课
        Cursor cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"msum"},
                null,
                null,
                null);
        cursor.moveToFirst();
        msum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"asum"},
                null,
                null,
                null);
        cursor.moveToFirst();
        asum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"esum"},
                null,
                null,
                null);
        cursor.moveToFirst();
        esum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        //获取有多少节周
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"weeksum"},
                null,
                null,
                null);
        cursor.moveToFirst();
        cursor.moveToFirst();
        weekSum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));



    }
}