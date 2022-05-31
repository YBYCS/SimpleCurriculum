package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Random;

public class SettingCourse extends AppCompatActivity {

    TextView courseTime,courseName,teacher,classroom,confirm,cancel;
    private courseDataClass dt;//临时存储数据
    int msum,asum,esum,weekSum,courseSum;
    ArrayList<ToggleButton> selectWeekList = new ArrayList<>();//选择第几周上课按钮列表
    RadioButton oddWeek,doubleWeek,allWeek;
    LinearLayout shangkeshijian;
    boolean isAdd;
    RadioGroup rg_week;
    DBOpenHelper dbOpenHelper;//sqlite helper类的子类
    SQLiteDatabase db;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_course);
        context = this;
        Intent intent = getIntent();
        getInitData();
        getUI();
        courseSum = msum+asum+esum;//有多少节课
        addWeekSelectCard();
        initWheelData();
        isAdd = intent.getBooleanExtra("isAdd",false);
        //如果是添加新的课程
        if(isAdd){
            initAdd(intent);
        }
        //如果是编辑已有的课程
        else{
            initEdit(intent);
        }
    }



    //如果是添加新的课程的初始化
    void initAdd(Intent intent){
        dt.beginTime = intent.getIntExtra("beginTime",0);
        dt.period = intent.getIntExtra("period",0);
        dt.sum = 1;
        courseTime.setText(dt.beginTime+"-"+dt.beginTime);
        allWeek.setChecked(true);
        dt.week = "";
        //初始化选择周的字符串
        for (int j = 0; j < weekSum; j++) {
            dt.week += "1";
        }
        //随机取个颜色
        Random random = new Random();
        colorSelectList.get(random.nextInt(6)).setChecked(true);
        //删除课程按钮不能使用
        Button button = findViewById(R.id.deleteCourse);
        button.setEnabled(false);
        button.setTextColor(getColor(R.color.blackOfShow));
    }
    //如果是编辑已有的课程的初始化
    void initEdit(Intent intent){
        dt.beginTime = intent.getIntExtra("beginTime",0);
        dt.sum = intent.getIntExtra("sum",0);
        dt.id = intent.getIntExtra("id",0);
        dt.name = intent.getStringExtra("name");
        dt.teacher = intent.getStringExtra("teacher");
        dt.classroom = intent.getStringExtra("classroom");
        dt.week = intent.getStringExtra("week");
        dt.period = intent.getIntExtra("period",0);
        classroom.setText(dt.classroom);
        teacher.setText(dt.teacher);
        courseName.setText(dt.name);
        courseTime.setText(dt.beginTime+"-"+(dt.beginTime+dt.sum-1));
        dt.color = intent.getIntExtra("color",1);
        colorSelectList.get(dt.color-1).setChecked(true);



        for (int i = 0; i < weekSum; i++) {
            if(dt.week.substring(i,i+1).equals("1")){
                selectWeekList.get(i).setChecked(true);
            }
            else{
                selectWeekList.get(i).setChecked(false);
            }
        }
        //改变周选择卡会改变单双周的状态所以要放在下面
        Cursor cursor = db.query("courseData",null,"id=?",new String[]{""+dt.id},null,null,null);
        cursor.moveToFirst();
        dt.isOddWeek = cursor.getInt(cursor.getColumnIndexOrThrow("isOddWeek"));
        dt.isDoubleWeek= cursor.getInt(cursor.getColumnIndexOrThrow("isDoubleWeek"));

        Button button = findViewById(R.id.deleteCourse);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View outerView = LayoutInflater.from(context).inflate(R.layout.dialog_hint,null);
                TextView tv = outerView.findViewById(R.id.tittle);
                tv.setText("确认删除");
                tv = outerView.findViewById(R.id.content);
                tv.setText("该操作将永久删除该课程,是否继续?");
                AlertDialog alertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                        db.delete("coursedata","id=?",new String[]{String.valueOf(dt.id)});
                        alertDialog.dismiss();
                        setResult(0);
                        finish();
                    }
                });

                alertDialog.show();
            }
        });
    }

    int monitorBegin,monitorEnd;//接受滑动选择框的返回值
    ArrayList<RadioButton> colorSelectList;
    RadioGroup rg_color;
    //获取界面UI控件 并且绑定事件
    private void getUI(){
        classroom = findViewById(R.id.et_classroom);
        teacher = findViewById(R.id.et_teacherName);
        courseTime = findViewById(R.id.tv_courseTime);
        courseName = findViewById(R.id.et_courseName);
        //确认按钮
        confirm = findViewById(R.id.stCourse_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(courseName.length()!=0){

                    dt.name = courseName.getText().toString();
                }
                if(teacher.length()!=0){
                    dt.teacher = teacher.getText().toString();
                }
                if(classroom.length()!=0){
                    dt.classroom = classroom.getText().toString();
                }
                if(dt.name==null){
                    Toast.makeText(context,"请输入课程名称",Toast.LENGTH_SHORT).show();
                }
                //保存数据
                else{
                    ContentValues values = new ContentValues();
                    values.put("name",dt.name);
                    values.put("teacher",dt.teacher);
                    values.put("classroom",dt.classroom);
                    values.put("begintime",dt.beginTime);
                    values.put("sum",dt.sum);
                    values.put("week",dt.week);
                    values.put("color",dt.color);
                    values.put("period",dt.period);
                    values.put("isOddWeek ",dt.isOddWeek);
                    values.put("isDoubleWeek",dt.isDoubleWeek);
                    //添加新的课程为插入
                    if(isAdd){
                        db.insert("coursedata",null,values);
                    }
                    //修改已知课程根据id修改
                    else{
                        Log.d(TAG, "onClick: "+dt.sum);
                        db.update("coursedata",values,"id=?",new String[]{String.valueOf(dt.id)});
                    }

                    setResult(0);
                    finish();
                }
            }
        });
        //取消按钮
        cancel = findViewById(R.id.stCourse_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        oddWeek = findViewById(R.id.oddWeek);
        doubleWeek = findViewById(R.id.doubleWeek);
        allWeek = findViewById(R.id.allWeek);
        rg_week = findViewById(R.id.select_week);
        //单周 双周 全选 组合框 监听函数
        rg_week.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //单周
                if(i==oddWeek.getId()){
                    for (int j = 0; j < selectWeekList.size(); j++) {
                        if(j%2==0){
                            selectWeekList.get(j).setChecked(true);

                        }
                        else{
                            selectWeekList.get(j).setChecked(false);

                        }
                    }
                    //是否为单周
                    dt.isOddWeek = 1;
                }
                //双周
                else if(i==doubleWeek.getId()){

                    for (int j = 0; j < selectWeekList.size(); j++) {
                        if(j%2!=0){
                            selectWeekList.get(j).setChecked(true);

                        }
                        else{
                            selectWeekList.get(j).setChecked(false);

                        }
                    }
                    dt.isDoubleWeek = 1;
                }
                //全选
                else{
                    for (int j = 0; j < selectWeekList.size(); j++) {
                        selectWeekList.get(j).setChecked(true);

                    }

                    dt.isOddWeek = 1;
                    dt.isDoubleWeek = 1;
                }
            }
        });
        shangkeshijian = findViewById(R.id.shangkeshijian);//设置上课第几节
        //设置选择上课时间节数框
        shangkeshijian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final WheelView beginWheel,endWheel;
                View outerView = LayoutInflater.from(context).inflate(R.layout.dialog_course_time,null);
                beginWheel = outerView.findViewById(R.id.wheel_begin_jie);
                endWheel = outerView.findViewById(R.id.wheel_end_jie);
                beginWheel.setOffset(1);
                beginWheel.setItems(bj);
                beginWheel.setSelection(dt.beginTime-1);
                beginWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
                    public void onSelected(int selectedIndex, String item) {
                        monitorBegin = selectedIndex+1;
                        if(monitorBegin>monitorEnd){
                            endWheel.setSelection(monitorBegin-1);
                        }
                    }
                });
                endWheel.setOffset(1);
                endWheel.setItems(bj);
                endWheel.setSelection(dt.beginTime+dt.sum-2);
                endWheel.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
                    public void onSelected(int selectedIndex, String item) {
                        monitorEnd = selectedIndex+1;
                        if(monitorBegin>monitorEnd){
                            beginWheel.setSelection(monitorEnd-2);
                        }
                    }
                });
                //生成弹出框
                AlertDialog alert;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(outerView);
                builder.setCancelable(false);
                alert = builder.create();
                //设置布局中的取消按钮
                outerView.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
                //设置确定按钮
                outerView.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dt.beginTime = monitorBegin-1;//减去偏移值
                        dt.sum = monitorEnd  -monitorBegin+1;
                        courseTime.setText(""+dt.beginTime+"-"+(dt.sum+dt.beginTime-1));
                        //实现点击按钮的作用
                        alert.dismiss();
                    }
                });
                alert.show();

            }
        });

        //颜色选择器
        initColorSelect();

    }

    //设置那一周上课的选择卡
    void addWeekSelectCard(){

        LinearLayout container = (LinearLayout) findViewById(R.id.selectWeekContainer);//第几周上课选择卡的容器
        for (int i = 0; i < weekSum; i+=6) {

            LinearLayout cardContainer = new LinearLayout(this);
            cardContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(this,40)));
            cardContainer.setOrientation(LinearLayout.HORIZONTAL);
            cardContainer.setPadding(dip2px(this,10),dip2px(this,5),dip2px(this,10),0);
            container.addView(cardContainer);
            for (int j = 0; j < 6; j++) {
                //添加空的占位控件
                if(i+j==weekSum){
                    for (int k = 0; k < 6-weekSum%6; k++) {
                        TextView kong = new TextView(this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1);
                        lp.setMargins(5,0,5,0);
                        kong.setLayoutParams(lp);
                        cardContainer.addView(kong);
                    }
                    break;
                }
                //添加周选择卡
                ToggleButton card = new ToggleButton(this);
                card.setTextOn(""+(i+1+j));
                card.setTextOff(""+(i+1+j));
                card.setTextColor(this.getColor(R.color.defaultWhite));

                card.setChecked(true);
                card.setBackground(this.getDrawable(R.drawable.bg_togglebutton));
                card.setTag(j+i);//数组从0开始
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1);
                lp.setMargins(5,0,5,0);
                card.setLayoutParams(lp);
                //周选择卡选择状态发生变更后(点击了或者是别的方法改变了) 改变选择的周
                card.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        int tag = (int)compoundButton.getTag();
                        String copy;
                        if(b){

                            copy = dt.week.substring(0,tag) + "1" +dt.week.substring(tag+1);

                        }
                        else{
                            copy = dt.week.substring(0,tag) + "0" +dt.week.substring(tag+1);
                        }
                        dt.week = copy;
                        dt.isDoubleWeek = 0;//点击了选择卡后就不再是单周双周全选了
                        dt.isOddWeek = 0;
                    }
                });
                cardContainer.addView(card);
                selectWeekList.add(card);
            }
        }
    }

    //初始化颜色选择器
    private void initColorSelect(){
        //颜色选择器
        colorSelectList = new ArrayList<>();
        colorSelectList.add(findViewById(R.id.color1));
        GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(0).getBackground().getCurrent();
        gradientDrawable.setColor(getShowColor(1));
        colorSelectList.add(findViewById(R.id.color2));
        gradientDrawable = (GradientDrawable) colorSelectList.get(1).getBackground().getCurrent();
        gradientDrawable.setColor(getShowColor(2));
        colorSelectList.add(findViewById(R.id.color3));
        gradientDrawable = (GradientDrawable) colorSelectList.get(2).getBackground().getCurrent();
        gradientDrawable.setColor(getShowColor(3));
        colorSelectList.add(findViewById(R.id.color4));
        gradientDrawable = (GradientDrawable) colorSelectList.get(3).getBackground().getCurrent();
        gradientDrawable.setColor(getShowColor(4));
        colorSelectList.add(findViewById(R.id.color5));
        gradientDrawable = (GradientDrawable) colorSelectList.get(4).getBackground().getCurrent();
        gradientDrawable.setColor(getShowColor(5));
        colorSelectList.add(findViewById(R.id.color6));
        gradientDrawable = (GradientDrawable) colorSelectList.get(5).getBackground().getCurrent();
        gradientDrawable.setColor(getShowColor(6));
        rg_color = findViewById(R.id.rg_color);
        rg_color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == colorSelectList.get(0).getId()){
                    GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(0).getBackground().getCurrent();
                    gradientDrawable.setColor(getShowColor(1));
                    dt.color = 1;
                }
                else if(checkedId == colorSelectList.get(1).getId()){
                    GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(1).getBackground().getCurrent();
                    gradientDrawable.setColor(getShowColor(2));
                    dt.color = 2;
                }
                else if(checkedId == colorSelectList.get(2).getId()){
                    GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(2).getBackground().getCurrent();
                    gradientDrawable.setColor(getShowColor(3));
                    dt.color = 3;
                }
                else if(checkedId == colorSelectList.get(3).getId()){
                    GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(3).getBackground().getCurrent();
                    gradientDrawable.setColor(getShowColor(4));
                    dt.color = 4;
                }
                else if(checkedId == colorSelectList.get(4).getId()){
                    GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(4).getBackground().getCurrent();
                    gradientDrawable.setColor(getShowColor(5));
                    dt.color = 5;
                }
                else{
                    GradientDrawable gradientDrawable = (GradientDrawable) colorSelectList.get(5).getBackground().getCurrent();
                    gradientDrawable.setColor(getShowColor(6));
                    dt.color = 6;
                }
            }

        });
    }

    //获取背景展示的颜色对应的int
    private int getShowColor(int a){
        switch (a){
            case 1:
                return context.getColor(R.color.show1);
            case 2:
                return context.getColor(R.color.show2);
            case 3:
                return context.getColor(R.color.show3);
            case 4:
                return context.getColor(R.color.show4);
            case 5:
                return context.getColor(R.color.show5);
            default:
                return context.getColor(R.color.show6);
        }
    }
    //获取文字展示的颜色对应的int
    private int getTextColor(int a){
        switch (a){
            case 1:
                return context.getColor(R.color.text1);
            case 2:
                return context.getColor(R.color.text2);
            case 3:
                return context.getColor(R.color.text3);
            case 4:
                return context.getColor(R.color.text4);
            case 5:
                return context.getColor(R.color.text5);
            default:
                return context.getColor(R.color.text6);
        }
    }

    //滑动选择框的数据初始化 选择上课时间
    ArrayList<String> bj = new ArrayList<>();
    private void initWheelData(){
        for (int i = 1; i <= courseSum; i++) {
            bj.add(String.valueOf(i));
        }
    }
    //课程数据类
    private class courseDataClass {
        public String name, teacher, classroom, week;
        public int beginTime,sum,period ,id,color,isOddWeek=0,isDoubleWeek=0;
    }


    //获取数据库数据
    private void getInitData(){
        dt = new courseDataClass();
        dbOpenHelper = new DBOpenHelper(this,"data.db",null,1);
        db = dbOpenHelper.getWritableDatabase();

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
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"weeksum"},
                null,
                null,
                null);
        cursor.moveToFirst();
        weekSum = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}