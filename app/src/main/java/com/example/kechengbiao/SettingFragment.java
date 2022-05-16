package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLData;
import java.util.ArrayList;
import java.util.Calendar;


public class SettingFragment extends Fragment {
    private RelativeLayout st_BeginTime,st_SumWeek,st_ShowWeekend,st_NumberOfCourse,st_Time;//设置界面的几个按钮
    private DatePicker datePicker;//滑动日期选择框
    private ArrayList<String> wheelDataList = new ArrayList<>();//滑动框的数据
    private String selectText = "";//记录滑动选择框的文本
    private Switch sw_isShowWeekend;
    DBOpenHelper dbOpenHelper;//sqlite helper类的子类
    SQLiteDatabase db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //创建Fragment的布局
        View view = inflater.inflate(R.layout.fragment_setting,container,false);
        dbOpenHelper = new DBOpenHelper(getActivity(),"data.db",null,1);
        db = dbOpenHelper.getWritableDatabase();
        return view;
    }

    //获取设置页面的相关组件 并且绑定点击事件
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        st_BeginTime = (RelativeLayout) getActivity().findViewById(R.id.st_BeginTime);
        st_SumWeek = (RelativeLayout) getActivity().findViewById(R.id.st_SumWeek);
        st_ShowWeekend = (RelativeLayout) getActivity().findViewById(R.id.st_ShowWeekend);
        st_NumberOfCourse = (RelativeLayout) getActivity().findViewById(R.id.st_NumberOfCourse);
        st_Time = (RelativeLayout) getActivity().findViewById(R.id.st_TimeSetting);
        sw_isShowWeekend = (Switch) getActivity().findViewById(R.id.sw_isShowWeekend);
        //todo 初始化选择框的数据是多少 并且将储存的数据显示出来先

        //绑定设置开学日期
        st_BeginTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                settingBeginTime();
            }
        });

        //绑定设置总周数
        st_SumWeek.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                wheelDataList.clear();
                //初始化数据
                for(int i=1;i<31;i++)wheelDataList.add(String.format("%d",i));
                showChoiceDialog(wheelDataList,  15,
                        new WheelView.OnWheelViewListener() {
                            //监听滚动返回值
                            @Override
                            public void onSelected(int selectedIndex, String item) {
                                selectText = item;
                            }
                        });
            }
        });

        //绑定是否显示周末
        sw_isShowWeekend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int data = (b==true? 1:0);
                ContentValues values = new ContentValues();
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"isshowweekend"});
            }
        });

        //绑定课程节数
        st_NumberOfCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stNumberOfCourse();
            }
        });

        //绑定课程表时间设置
        st_Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),ScheduleTimeSetting.class);//context 跳转的窗口的类
                //intent.putExtra("nickName",Intent.ACTION_CALL);
                startActivity(intent);//跳转

            }
        });
    }

    //设置开学时间
    private void settingBeginTime(){
        //生成弹出框
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view_custom;//获取弹出框的的布局
        final LayoutInflater inflater = getActivity().getLayoutInflater(); //LayoutInflater.from(getActivity());
        view_custom = inflater.inflate(R.layout.dialog_stbegintime,null,false);

        builder.setView(view_custom);
        builder.setCancelable(false);
        alert = builder.create();
        datePicker = (DatePicker) view_custom.findViewById(R.id.dp_begin_time_data_picker);//获取日期选择器
        //取消按钮
        view_custom.findViewById(R.id.bt_begin_time_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        //确定按钮
        view_custom.findViewById(R.id.bt_begin_time_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                int data = datePicker.getYear();
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"year"});
                data = datePicker.getMonth();
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"month"});
                data = datePicker.getDayOfMonth() ;
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"day"});
                //db.close();
                alert.dismiss();
            }
        });
        alert.show();
    }

    //单个滑动选择框
    private void showChoiceDialog(ArrayList<String> dataList,int selected,
                                      WheelView.OnWheelViewListener listener){
        selectText = "";
        View outerView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_wheelview,null);
        final WheelView wheelView = outerView.findViewById(R.id.wheel_view);
        wheelView.setOffset(2);// 对话框中当前项上面和下面的项数
        wheelView.setItems(dataList);// 设置数据源
        wheelView.setSelection(selected);// 默认选中第几项
        wheelView.setOnWheelViewListener(listener);//设置监听的函数

        //生成弹出框
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(outerView);
        builder.setCancelable(false);
        alert = builder.create();
        //取消按钮
        outerView.findViewById(R.id.bt_wheel_view_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        //确定按钮
        outerView.findViewById(R.id.bt_wheel_view_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                String data = selectText;
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"weeksum"});
                alert.dismiss();
            }
        });
        alert.show();

    }

    private ArrayList<String> mSumDataList = new ArrayList<>();//早上课程
    private ArrayList<String> aSumDataList = new ArrayList<>();//下午课程
    private ArrayList<String> eSumDataList = new ArrayList<>();//晚上课程
    private String mSelectText ,aSelectText ,eSelectText ;
    //课程节数设置
    private void stNumberOfCourse(){

        mSumDataList.clear();
        aSumDataList.clear();
        eSumDataList.clear();
        //初始化数据
        for(int i=1;i<11;i++) {
            mSumDataList.add(String.format("%d",i));
            aSumDataList.add(String.format("%d",i));
            eSumDataList.add(String.format("%d",i));
        }
        mSelectText = "";
        aSelectText = "";
        eSelectText = "";
        View outView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_stnumberofcourse,null);
        final WheelView mWheelView,aWheelView,eWheelView;


        mWheelView = outView.findViewById(R.id.wheel_msum);
        aWheelView = outView.findViewById(R.id.wheel_asum);
        eWheelView = outView.findViewById(R.id.wheel_esum);

        mWheelView.setOffset(1);
        mWheelView.setItems(mSumDataList);// 设置数据源
        mWheelView.setSelection(3);// 默认选中第几项
        mWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                mSelectText = item;
            }
        });//设置监听的函数

        aWheelView.setOffset(1);
        aWheelView.setItems(aSumDataList);// 设置数据源
        aWheelView.setSelection(3);// 默认选中第几项
        aWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                aSelectText = item;
            }
        });//设置监听的函数

        eWheelView.setOffset(1);
        eWheelView.setItems(eSumDataList);// 设置数据源
        eWheelView.setSelection(1);// 默认选中第几项
        eWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                eSelectText = item;
            }
        });//设置监听的函数

        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(outView);
        builder.setCancelable(false);
        alert = builder.create();
        //取消按钮
        outView.findViewById(R.id.bt_number_of_course_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        //确定按钮
        outView.findViewById(R.id.bt_number_of_course_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                String data = mSelectText;
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"msum"});
                values.clear();
                data = aSelectText;
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"asum"});
                values.clear();
                data = eSelectText;
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"esum"});
                alert.dismiss();
            }
        });
        alert.show();
    }
}