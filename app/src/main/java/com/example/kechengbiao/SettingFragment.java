package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;


public class SettingFragment extends Fragment {
    private RelativeLayout st_BeginTime,st_SumWeek,st_ShowWeekend,st_NumberOfCourse,st_Time,st_StartNewSemester;//设置界面的几个按钮
    private DatePicker datePicker;//滑动日期选择框
    private ArrayList<String> wheelDataList = new ArrayList<>();//滑动框的数据
    private String selectText = "";//记录滑动选择框的文本
    private Switch sw_isShowWeekend;
    TextView tv_beginTime,tv_weekSum;
    DBOpenHelper dbOpenHelper;//sqlite helper类的子类
    SQLiteDatabase db;
    int selectWeek,weekSum;
    MainActivity mainActivity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //创建Fragment的布局
        View view = inflater.inflate(R.layout.fragment_setting,container,false);
        dbOpenHelper = new DBOpenHelper(getActivity(),"data.db",null,1);
        db = dbOpenHelper.getWritableDatabase();
        mainActivity =(MainActivity) getActivity();
        selectWeek = mainActivity.selectWeek;
        initData();
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
        //绑定是否显示周末
        sw_isShowWeekend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int data = (b==true? 1:0);
                ContentValues values = new ContentValues();
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"isshowweekend"});
                mainActivity.reFragment(selectWeek);
            }
        });

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
                Cursor cursor = db.query("setting",null,"name=?",new String[]{"weeksum"},null,null,null);
                cursor.moveToFirst();
                int weekSum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
                //初始化滑动选择框的数据
                for(int i=1;i<31;i++)wheelDataList.add(String.format("%d",i));
                showChoiceDialog(wheelDataList,  weekSum-1,
                        new WheelView.OnWheelViewListener() {
                            //监听滚动返回值
                            @Override
                            public void onSelected(int selectedIndex, String item) {
                                selectText = item;
                            }
                        });
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
                startActivityForResult(intent,999);//跳转

            }
        });

        //开启新学期
        st_StartNewSemester = (RelativeLayout) getActivity().findViewById(R.id.startNewSemester);
        st_StartNewSemester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View outerView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hint, null);
                TextView tv = outerView.findViewById(R.id.tittle);
                tv.setText("确认删除");
                tv = outerView.findViewById(R.id.content);
                tv.setText("该操作将永久删除所有课程数据,是否继续?");
                androidx.appcompat.app.AlertDialog alertDialog;
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                builder.setView(outerView);
                alertDialog = builder.create();
                //设置布局中的取消按钮
                outerView.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                //设置确定按钮
                outerView.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //实现点击按钮的作用
                        db.execSQL("DELETE FROM coursedata");
                        mainActivity.reFragment();
                        alertDialog.dismiss();

                    }

                });

                alertDialog.show();
            }
        });
        tv_beginTime = getActivity().findViewById(R.id.tv_semester_begin_time);
        tv_weekSum = getActivity().findViewById(R.id.tv_week_sum);



        //初始化 已设置的内容的展示
        Cursor cursor = db.query("setting",null,"name=?",new String[]{"year"},null,null,null);
        cursor.moveToFirst();
        int time = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        String str = time+"-";
        cursor = db.query("setting",null,"name=?",new String[]{"month"},null,null,null);
        cursor.moveToFirst();
        time = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        str += time+"-";
        cursor = db.query("setting",null,"name=?",new String[]{"day"},null,null,null);
        cursor.moveToFirst();
        time = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        str += time+"";
        tv_beginTime.setText(str);

        cursor = db.query("setting",null,"name=?",new String[]{"weeksum"},null,null,null);
        cursor.moveToFirst();
        time = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        weekSum = time;
        str = time+"";
        tv_weekSum.setText(str);

        cursor = db.query("setting",null,"name=?",new String[]{"isshowweekend"},null,null,null);
        cursor.moveToFirst();
        time = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        sw_isShowWeekend.setChecked(time==1);


    }

    //设置完时间后回调加载课程表
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==999&&resultCode==0){
            mainActivity.reFragment(selectWeek);
        }
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
        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {

            }
        });
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
                String str;
                str = data + "-";
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"year"});
                data = datePicker.getMonth()+1;//从0开始
                str += data + "-";
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"month"});
                data = datePicker.getDayOfMonth() ;
                str += data + "";
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"day"});
                tv_beginTime.setText(str);
                mainActivity.reFragment();
                alert.dismiss();
            }
        });
        alert.show();
    }

    //设置总周数 单个滑动选择框通用的监听
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
                if(Integer.parseInt(data)>weekSum){
                    modificationWeekData(Integer.parseInt(data));
                }
                weekSum = Integer.parseInt(data);
                values.put("state",data);
                db.update("setting",values,"name=?",new String[]{"weeksum"});
                tv_weekSum.setText(selectText);
                mainActivity.reFragment(selectWeek);
                alert.dismiss();
            }
        });
        alert.show();

    }

    //修改总周数后 统一修改所有课程的第几周上课情况
    private void modificationWeekData(int newWeekSum){

        Cursor cursor;
        cursor = db.query("courseData",null,null,null,null,null,null);
        while (cursor.moveToNext()){
            int isodd,isdouble,id;
            String week;

            isodd = cursor.getInt(cursor.getColumnIndexOrThrow("isOddWeek"));
            isdouble = cursor.getInt(cursor.getColumnIndexOrThrow("isDoubleWeek"));
            week = cursor.getString(cursor.getColumnIndexOrThrow("week"));
            week = week.substring(0,weekSum);//截取原来周数长度 因为修改的更小的时候没有处理多的部分
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            for (int i = weekSum; i < newWeekSum; i++) {
                //单数 从零开始
                if(i%2==0&&isodd==1){
                    week += "1";

                }
                else if(i%2!=0&&isdouble==1){
                    week += "1";
                }
                else{
                    week += "0";
                }
            }
            ContentValues values = new ContentValues();
            values.put("week",week);

            db.update("courseData",values,"id=?",new String[]{""+id});
        }

    }

    CourseData dt;//用来存放数据的类
    //初始化数据
    void initData(){
        dt = new CourseData();
        for(int i=1;i<11;i++) {
            mSumDataList.add(String.format("%d",i));
            aSumDataList.add(String.format("%d",i));
            eSumDataList.add(String.format("%d",i));
        }
        //查询数据
        Cursor cursor = db.query("setting",null,"name=?",new String[]{"msum"}, null, null, null);
        cursor.moveToFirst();
        dt.msum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",null,"name=?",new String[]{"asum"}, null, null, null);
        cursor.moveToFirst();
        dt.asum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",null,"name=?",new String[]{"esum"}, null, null, null);
        cursor.moveToFirst();
        dt.esum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
    }

    private ArrayList<String> mSumDataList = new ArrayList<>();//早上课程
    private ArrayList<String> aSumDataList = new ArrayList<>();//下午课程
    private ArrayList<String> eSumDataList = new ArrayList<>();//晚上课程
    private String mSelectText ,aSelectText ,eSelectText ;
    //课程节数设置
    private void stNumberOfCourse(){
        View outView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_stnumberofcourse,null);
        final WheelView mWheelView,aWheelView,eWheelView;


        mWheelView = outView.findViewById(R.id.wheel_msum);
        aWheelView = outView.findViewById(R.id.wheel_asum);
        eWheelView = outView.findViewById(R.id.wheel_esum);

        mWheelView.setOffset(1);
        mWheelView.setItems(mSumDataList);// 设置数据源
        mWheelView.setSelection(dt.msum-1);// 默认选中第几项
        mWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                mSelectText = item;
            }
        });//设置监听的函数

        aWheelView.setOffset(1);
        aWheelView.setItems(aSumDataList);// 设置数据源
        aWheelView.setSelection(dt.asum-1);// 默认选中第几项
        aWheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                aSelectText = item;
            }
        });//设置监听的函数

        eWheelView.setOffset(1);
        eWheelView.setItems(eSumDataList);// 设置数据源
        eWheelView.setSelection(dt.esum-1);// 默认选中第几项
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

                //根据结果 选择删除数据或者是初始化数据
                String data = mSelectText;
                int i = Integer.parseInt(data);
                if(i>dt.msum){
                    for (int j = dt.msum+1; j <= i; j++) {
                        addCourse(10+j);
                    }
                    values.put("state",data);
                    db.update("setting",values,"name=?",new String[]{"msum"});
                    values.clear();
                    dt.msum = i;
                }
                else if(i<dt.msum){
                    for (int j = dt.msum; j > i; j--) {
                        delCourse(10+j);
                    }
                    values.put("state",data);
                    db.update("setting",values,"name=?",new String[]{"msum"});
                    values.clear();
                    dt.msum = i;
                }
                data = aSelectText;
                i = Integer.parseInt(data);
                if(i>dt.asum){
                    for (int j = dt.asum+1; j <= i; j++) {
                        addCourse(20+j);
                    }
                    values.put("state",data);
                    db.update("setting",values,"name=?",new String[]{"asum"});
                    values.clear();
                    dt.asum = i;
                }
                else if(i<dt.asum){
                    for (int j = dt.asum; j > i; j--) {
                        delCourse(20+j);
                    }
                    values.put("state",data);
                    db.update("setting",values,"name=?",new String[]{"asum"});
                    values.clear();
                    dt.asum = i;
                }
                data = eSelectText;
                i = Integer.parseInt(data);
                if(i>dt.esum){
                    for (int j = dt.esum+1; j <= i; j++) {
                        addCourse(30+j);
                    }
                    values.put("state",data);
                    db.update("setting",values,"name=?",new String[]{"esum"});
                    values.clear();
                    dt.esum = i;
                }
                else if(i<dt.esum){
                    for (int j = dt.esum; j > i; j--) {
                        delCourse(30+j);
                    }
                    values.put("state",data);
                    db.update("setting",values,"name=?",new String[]{"esum"});
                    values.clear();
                    dt.esum = i;
                }
                mainActivity.reFragment(selectWeek);
                alert.dismiss();
            }
        });
        alert.show();
    }

    //设置课表节数 如果要添加一个新的课程时间 初始化这个课程的时间段防止为空
    void addCourse(int period){
        int bh,bm,eh,em;

        int courseDuration,restDuration;//一节课的世界,课间时间
        courseDuration = getDataBaseInt("setting","name=?","courseduration","state");
        restDuration = getDataBaseInt("setting","name=?","restduration","state");

        //初始化 插入的一节课的时间安排
        Log.d(TAG, "addCourse: "+(period-1));
        bh = getEndHour(String.valueOf(period-1));//根据上一节课的时间来调整
        bm = getEndMinute(String.valueOf(period-1))+restDuration;
        eh = bh;
        em = bm+courseDuration;


        if(bm>59){
            bh+=bm/60;
            bm%=60;
        }
        if(em>59){

            eh+=em/60;
            em%=60;
        }
        bh = bh>23?23:bh;
        eh = eh>23?23:eh;

        insertScheduleData(String.valueOf(bh),String.valueOf(bm),String.valueOf(eh),String.valueOf(em),String.valueOf(period));
    }

    void delCourse(int period){
        db.delete("courseschedule","period=?",new String[]{String.valueOf(period)});
    }

    //传入时间和对应编号 将课程时间储存到本地
    private void insertScheduleData(String bh,String bm,String eh,String em,String period){
        ContentValues values = new ContentValues();
        values.put("period",period);
        values.put("beginhour",bh);
        values.put("beginminute",bm);
        values.put("endhour",eh);
        values.put("endminute",em);
        db.insert("courseschedule",null,values);
    }

    //查询 数据表的数据 分别是 表名 条件 条件对应的key 和结果列名
    int getDataBaseInt(String tableName,String condition,String key,String resultColumn){
        Cursor cursor = db.query(tableName,//表名
                null,//指定列
                condition,//查询条件
                new String[]{key},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndexOrThrow(resultColumn));
    }

    //查询 数据表的数据 分别是 表名 条件 指定列 和结果列名
    String getDataBaseString(String tableName,String condition,String key,String resultColumn){
        Cursor cursor = db.query(tableName,//表名
                null,//指定列
                condition,//查询条件
                new String[]{key},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(resultColumn));
    }

    //传入对应编号获取对应课程的时间
    private int getBeginHour(String number){
        Cursor cursor = db.query("courseschedule",
                null,
                "period=?",
                new String[]{number},
                null,
                null,
                null);
        cursor.moveToFirst();
        int getData = cursor.getInt(cursor.getColumnIndexOrThrow("beginhour"));
        return getData;
    }
    private int getBeginMinute(String number){
        Cursor cursor = db.query("courseschedule",
                null,
                "period=?",
                new String[]{number},
                null,
                null,
                null);
        cursor.moveToFirst();
        int getData = cursor.getInt(cursor.getColumnIndexOrThrow("beginminute"));
        return getData;
    }
    private int getEndHour(String number){
        Cursor cursor = db.query("courseschedule",
                null,
                "period=?",
                new String[]{number},
                null,
                null,
                null);
        cursor.moveToFirst();
        int getData = cursor.getInt(cursor.getColumnIndexOrThrow("endhour"));
        return getData;
    }
    private int getEndMinute(String number){
        Cursor cursor = db.query("courseschedule",
                null,
                "period=?",
                new String[]{number},
                null,
                null,
                null);
        cursor.moveToFirst();
        int getData = cursor.getInt(cursor.getColumnIndexOrThrow("endminute"));
        return getData;
    }

    //课程数据类
    class CourseData{
        int msum,asum,esum;
    }
}