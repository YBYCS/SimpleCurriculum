package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //定义Fragment对象
    private Fragment fragment_1,fragment_2,fragment_3,nowFragment;
    private TextView tab_1,tab_2,tab_3;
    private int howWeek=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
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
    public int getHowWeek() {
        return howWeek;
    }
    public void setHowWeek(int HowWeek){
        howWeek = HowWeek;
    }

    //重新载入课程表 todo 和今日课程
    public void reLoadKcb(int theWeek){
        howWeek = theWeek;
        //删除fragment
        getSupportFragmentManager().beginTransaction().remove(fragment_2).commit();
        fragment_2 = null;
        showFragment2();
    }
}