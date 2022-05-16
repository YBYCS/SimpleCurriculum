package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class ScheduleTimeSetting extends AppCompatActivity {
    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;
    private int msum, asum, esum;
    private int courseDuration,restDuration;
    private Context context;
    //填充滑动选择框的数据
    private ArrayList<String>bhDataList,bmDataList,ehDataList,emDataList,durationDataList;
    //获取滑动选择框返回的数据
    private String bhSelectText,bmSelectText,ehSelectText,emSelectText,selectText;
    //储存课程时间展示文本框
    ArrayList<TextView> mtvList = new ArrayList<>();
    ArrayList<TextView> atvList = new ArrayList<>();
    ArrayList<TextView> etvList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_time_setting);
        dbOpenHelper = new DBOpenHelper(this, "data.db", null, 1);
        db = dbOpenHelper.getWritableDatabase();
        context = this;
        getInitData();
        initDataList();
        initClassUI();


    }
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
        //获取每节课时间是否固定
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"isfixedtime"},
                null,
                null,
                null);
        cursor.moveToFirst();
        isFixedTime = cursor.getInt(cursor.getColumnIndexOrThrow("state"))==1;

        //获取一节课时长和课间休息时长
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"courseduration"},
                null,
                null,
                null);
        cursor.moveToFirst();
        courseDuration = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"restduration"},
                null,
                null,
                null);
        cursor.moveToFirst();
        restDuration = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
    }
    //动态加载课程设置控件
    private void initClassUI(){

        SwitchCompat sw = (SwitchCompat) findViewById(R.id.sw_is_fixed_time);
        sw.setChecked(isFixedTime);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int i = b?1:0;
                isFixedTime = b;
                ContentValues vl =  new ContentValues();
                vl.put("state",String.valueOf(i));
                db.update("setting",vl,"name=?",new String[]{"isfixedtime"});
                if(isFixedTime) autoSetSchedule();
            }
        });


        //设置文本
        TextView textView = (TextView) findViewById(R.id.tv_class_duration);
        textView.setText(String.format(Locale.CHINA,"%dmin",courseDuration));
        textView = (TextView) findViewById(R.id.tv_rest_duration);
        textView.setText(String.format(Locale.CHINA,"%dmin",restDuration));

        //设置每节课和课间时长功能
        LinearLayout lrm = (LinearLayout)findViewById(R.id.course_duration);
        lrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //设置滑动选择器
                View outerView = LayoutInflater.from(context).inflate(R.layout.dialog_wheelview,null);
                final WheelView wheelView = outerView.findViewById(R.id.wheel_view);
                wheelView.setOffset(1);
                wheelView.setItems(durationDataList);
                wheelView.setSelection(courseDuration/5 - 1);
                wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        selectText = item;
                    }
                });
                AlertDialog alertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(outerView);
                builder.setCancelable(false);
                alertDialog = builder.create();
                //取消按钮
                outerView.findViewById(R.id.bt_wheel_view_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                //确定按钮
                outerView.findViewById(R.id.bt_wheel_view_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        courseDuration = Integer.parseInt(selectText);
                        ContentValues values = new ContentValues();
                        String data = String.valueOf(courseDuration);
                        values.put("state",data);
                        db.update("setting",values,"name=?",new String[]{"courseduration"});
                        if(isFixedTime) autoSetSchedule();
                        TextView tv = (TextView) findViewById(R.id.tv_class_duration);
                        tv.setText(String.format(Locale.CHINA,"%dmin",courseDuration));

                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
        lrm = (LinearLayout)findViewById(R.id.rest_duration);
        lrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //设置滑动选择器
                View outerView = LayoutInflater.from(context).inflate(R.layout.dialog_wheelview,null);
                final WheelView wheelView = outerView.findViewById(R.id.wheel_view);
                wheelView.setOffset(1);
                wheelView.setItems(durationDataList);
                wheelView.setSelection(restDuration/5 - 1);
                wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        selectText = item;
                    }
                });
                AlertDialog alertDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(outerView);
                builder.setCancelable(false);
                alertDialog = builder.create();
                //取消按钮
                outerView.findViewById(R.id.bt_wheel_view_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                //确定按钮
                outerView.findViewById(R.id.bt_wheel_view_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restDuration = Integer.parseInt(selectText);
                        ContentValues values = new ContentValues();
                        String data = String.valueOf(restDuration);
                        values.put("state",data);
                        db.update("setting",values,"name=?",new String[]{"restduration"});
                        if(isFixedTime) autoSetSchedule();
                        TextView tv = (TextView) findViewById(R.id.tv_rest_duration);
                        tv.setText(String.format(Locale.CHINA,"%dmin",restDuration));
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });


        //动态加载对应数量课程时间设置选项

        //上午
        lrm = (LinearLayout) findViewById(R.id.lr_schedule_morning);
        //用于传递循环到第几个了
        int i1;
        for (int i = 0; i< msum; i++){

            //获取挂载容器
            LinearLayout lr = new LinearLayout(this);
            //设置布局
            lr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(this,60)));
            lr.setBackground(getDrawable(R.drawable.click_background));

            lr.setPadding(dip2px(this,15),dip2px(this,15),dip2px(this,15),dip2px(this,15));

            lr.setOrientation(LinearLayout.HORIZONTAL);
            lrm.addView(lr);

            TextView tvl = new TextView(this);
            tvl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            tvl.setText("第" + (i+1) + "节");
            lr.setBackground(getDrawable(R.drawable.click_background));
            tvl.setGravity(Gravity.CENTER_VERTICAL);
            tvl.setTextSize(20);
            tvl.setTextColor(getResources().getColor(R.color.blackOfShow));

            lr.addView(tvl);
            TextView tvr = new TextView(this);
            tvr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            tvr.setText(returnDataString(String.valueOf(10+i+1)));
            tvr.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            tvr.setTextColor(getResources().getColor(R.color.gray));
            tvr.setTextSize(20);


            i1 = 10+i+1;
            lr.setTag(i1);
            lr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settingSchedule(view);


                }
            });

            lr.addView(tvr);
            mtvList.add(tvr);



        }
        //下午
        lrm = (LinearLayout) findViewById(R.id.lr_schedule_afternoon);
        for (int i = 0; i< asum; i++){
            LinearLayout lr = new LinearLayout(this);
            lr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(this,60)));

            lr.setBackground(getDrawable(R.drawable.click_background));
            i1 = 20+i+1;
            lr.setTag(i1);
            lr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settingSchedule(view);
                }
            });
            lr.setPadding(dip2px(this,15),dip2px(this,15),dip2px(this,15),dip2px(this,15));
            //lr.setPadding(5,10,5,10);

            lr.setOrientation(LinearLayout.HORIZONTAL);
            lrm.addView(lr);

            TextView tvl = new TextView(this);
            tvl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            tvl.setText("第" + (i+ msum +1) + "节");
            lr.setBackground(getDrawable(R.drawable.click_background));
            tvl.setGravity(Gravity.CENTER_VERTICAL);
            tvl.setTextSize(20);
            tvl.setTextColor(getResources().getColor(R.color.blackOfShow));

            lr.addView(tvl);
            TextView tvr = new TextView(this);
            tvr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            tvr.setText(returnDataString(String.valueOf(20+i+1)));//这里变20对应下午
            tvr.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            tvr.setTextColor(getResources().getColor(R.color.gray));
            tvr.setTextSize(20);
            lr.addView(tvr);
            atvList.add(tvr);


        }
        //晚上
        lrm = (LinearLayout) findViewById(R.id.lr_schedule_evening);
        for (int i = 0; i< esum; i++){
            LinearLayout lr = new LinearLayout(this);
            lr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(this,60)));

            lr.setBackground(getDrawable(R.drawable.click_background));
            i1 = 30+i+1;
            lr.setTag(i1);
            lr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settingSchedule(view);
                }
            });
            lr.setPadding(dip2px(this,15),dip2px(this,15),dip2px(this,15),dip2px(this,15));
            //lr.setPadding(5,10,5,10);

            lr.setOrientation(LinearLayout.HORIZONTAL);
            lrm.addView(lr);

            TextView tvl = new TextView(this);
            tvl.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            tvl.setText("第" + (i+ msum + asum +1) + "节");
            lr.setBackground(getDrawable(R.drawable.click_background));
            tvl.setGravity(Gravity.CENTER_VERTICAL);
            tvl.setTextSize(20);
            tvl.setTextColor(getResources().getColor(R.color.blackOfShow));

            lr.addView(tvl);
            TextView tvr = new TextView(this);
            tvr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            tvr.setText(returnDataString(String.valueOf(30+i+1)));
            tvr.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            tvr.setTextColor(getResources().getColor(R.color.gray));
            tvr.setTextSize(20);
            lr.addView(tvr);
            etvList.add(tvr);



        }
    }



    //初始化数据
    private void initDataList(){

        bhDataList = new ArrayList<>();
        bmDataList = new ArrayList<>();
        ehDataList = new ArrayList<>();
        emDataList = new ArrayList<>();
        durationDataList = new ArrayList<>();
        for(int i=0;i<24;i++) {
            bhDataList.add(String.format("%d",i));
            ehDataList.add(String.format("%d",i));
        }
        for(int i=0;i<60;i++) {
            bmDataList.add(String.format("%d",i));
            emDataList.add(String.format("%d",i));
        }
        for(int i=5;i<=120;i+=5){
            durationDataList.add(String.format("%d",i));
        }
    }
    //弹出设置课程时间弹出框
    private void settingSchedule(View view) {

        int number = (Integer)view.getTag();//课程编号
        //获取弹出框布局
        View outView = LayoutInflater.from(this).inflate(R.layout.dialog_scheduletime, null);
        //滑动选择框
        final WheelView bhwl, bmwl, ehwl, emwl;
        String Snumber = String.valueOf(number);//获取number对应的string格式
        bhwl = outView.findViewById(R.id.wheel_beginhour);
        bmwl = outView.findViewById(R.id.wheel_beginminute);
        ehwl = outView.findViewById(R.id.wheel_endhour);
        emwl = outView.findViewById(R.id.wheel_endminute);

        bhwl.setOffset(1);//设置偏差值 可以直接1
        bhwl.setItems(bhDataList);//设置数据源
        bhwl.setSelection(getBeginHour(Snumber));//设置选择第几个 因为时间是从0开始所以不用减去偏差值
        bhwl.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                bhSelectText = item;
            }
        });//设置监听的函数

        bmwl.setOffset(1);//设置偏差值 可以直接1
        bmwl.setItems(bmDataList);//设置数据源
        bmwl.setSelection(getBeginMinute(Snumber));
        bmwl.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                bmSelectText = item;
            }
        });//设置监听的函数

        ehwl.setOffset(1);
        ehwl.setItems(ehDataList);//设置数据源
        ehwl.setSelection(getEndHour(Snumber));
        ehwl.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                ehSelectText = item;
            }
        });//设置监听的函数

        emwl.setOffset(1);
        emwl.setItems(emDataList);//设置数据源
        emwl.setSelection(getEndMinute(Snumber));
        emwl.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            //监听滚动返回值
            @Override
            public void onSelected(int selectedIndex, String item) {
                emSelectText = item;
            }
        });//设置监听的函数
        String eh,em;//记录初始值 用来调整后面的课程时间
        eh = ehSelectText;
        em = emSelectText;


        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(outView);
        builder.setCancelable(false);
        alert = builder.create();

        //取消按钮
        outView.findViewById(R.id.bt_schedule_time_cancel).setOnClickListener((View v)-> alert.dismiss());

        outView.findViewById(R.id.bt_schedule_time_confirm).setTag(number);
        //确定按钮
        outView.findViewById(R.id.bt_schedule_time_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeScheduleData(bhSelectText,bmSelectText,ehSelectText,emSelectText,String.valueOf(number));
                int hourOffset=0,minuteOffset;
                minuteOffset = Integer.parseInt(emSelectText)-Integer.parseInt(em);
                hourOffset += Integer.parseInt(ehSelectText)-Integer.parseInt(eh);
                if(isFixedTime) {
                    Toast.makeText(ScheduleTimeSetting.this,"已自动修改其他课程时间设置",Toast.LENGTH_SHORT).show();
                    setAllSchedule(number, hourOffset, minuteOffset);
                }
                showSchedule(number);

                alert.dismiss();
            }
        });
        alert.show();

    }

    //每节课时间是否固定为统一时间
    private boolean isFixedTime;
    //展示修改后的文本并且如果课程时间固定的话 就把后面的课程也一并调整 传入时间差


    //修改一节课时间 后面跟着改
    private void setAllSchedule(int period,int hourOffset,int minuteOffset){

        int p = period/10;//判断是上午还是下午

        if(isFixedTime){
            int sum;//记录要循环多次
            switch (p) {
                case 1:
                    sum = msum+10;
                    break;
                case 2:
                    sum = asum+20;
                    break;
                case 3:
                    sum = esum+30;
                    break;
                default:
                    sum = 0;//java的switch有点奇妙
            }

            for (int i = period + 1; i <= sum; i++) {
                String si = String.valueOf(i);
                //确保传入数据合法
                int bh,bm,eh,em;
                bh = getBeginHour(si)+ hourOffset;
                bm = getBeginMinute(si)+ minuteOffset;
                eh = getEndHour(si)+ hourOffset;
                em = getEndMinute(si)+ minuteOffset;
                if(bm<0){
                    bm +=60;
                    bh--;
                }
                else if(bm>59){
                    bm -=60;
                    bh++;
                }
                if(em<0){
                    em +=60;
                    eh--;
                }
                else if(em>59){
                    em -=60;
                    eh++;
                }
                bh = bh<0?0:bh;
                eh = eh<0?0:eh;
                bh = bh>23?23:bh;
                eh = eh>23?23:eh;
                storeScheduleData(String.valueOf(bh),
                        String.valueOf(bm),
                        String.valueOf(eh),
                        String.valueOf(em),
                        si);
                showSchedule(i);//重新展示
            }

        }

    };

    //修改固定课程时间 或课间时间调用该函数自动根据第一节开始时间修改其余所有课程时间
    private void autoSetSchedule(){
        int sh,sm;//课开始时间
        sh = getBeginHour("11");
        sm = getBeginMinute("11");
        int eh,em;//课结束时间
        for(int i=1;i<=msum;i++){
            em = sm + courseDuration;
            eh = sh + em/60;
            em %= 60;
            storeScheduleData(String.valueOf(sh),String.valueOf(sm),
                    String.valueOf(eh),String.valueOf(em),String.valueOf(i+10));
            showSchedule(i+10);
            sm = em + restDuration;
            sh = eh + sm/60;
            sm %= 60;
        }
        sh = getBeginHour("21");
        sm = getBeginMinute("21");
        for(int i=1;i<=asum;i++){
            em = sm + courseDuration;
            eh = sh + em/60;
            em %= 60;
            storeScheduleData(String.valueOf(sh),String.valueOf(sm),
                    String.valueOf(eh),String.valueOf(em),String.valueOf(i+20));
            showSchedule(i+20);

            sm = em + restDuration;
            sh = eh + sm/60;
            sm %= 60;
        }
        sh = getBeginHour("31");
        sm = getBeginMinute("31");
        for(int i=1;i<=esum;i++){
            em = sm + courseDuration;
            eh = sh + em/60;
            em %= 60;
            storeScheduleData(String.valueOf(sh),String.valueOf(sm),
                    String.valueOf(eh),String.valueOf(em),String.valueOf(i+30));
            showSchedule(i+30);

            sm = em + restDuration;
            sh = eh + sm/60;
            sm %= 60;
        }
        Toast.makeText(context,"已自动设置所有课程时间",Toast.LENGTH_SHORT).show();
    }

    //刷新展示课程时间 传入课程对应编号
    private void showSchedule(int period){
        int index = period%10-1;//获取文本控件索引
        int p = period/10;//获取时间段

        switch (p){
            case 1:
                mtvList.get(index).setText(returnDataString(String.valueOf(period)));
                break;
            case 2:
                atvList.get(index).setText(returnDataString(String.valueOf(period)));
                break;
            case 3:
                etvList.get(index).setText(returnDataString(String.valueOf(period)));
                break;
        }

    }

    //传入时间和对应编号 将课程时间储存到本地
    private void storeScheduleData(String bh,String bm,String eh,String em,String period){
        ContentValues values = new ContentValues();
        String data =  bh;
        values.put("beginhour",data);
        data = bm;
        values.put("beginminute",data);
        data = eh;
        values.put("endhour",data);
        data = em;
        values.put("endminute",data);
        db.update("courseschedule",values,"period=?",new String[]{period});
    }

    //传入课程编号返回对应时间数据格式
    private String returnDataString(String number) {

        Cursor cursor = db.query("courseschedule",
                null,
                "period=?",
                new String[]{number},
                null,
                null,
                null);
        cursor.moveToFirst();
        int getData = cursor.getInt(cursor.getColumnIndexOrThrow("beginhour"));
        String data = String.format("%02d", getData);
        data += ":";
        getData = cursor.getInt(cursor.getColumnIndexOrThrow("beginminute"));
        data += String.format("%02d", getData);
        data += "-";
        getData = cursor.getInt(cursor.getColumnIndexOrThrow("endhour"));
        data += String.format("%02d", getData);
        data += ":";
        getData = cursor.getInt(cursor.getColumnIndexOrThrow("endminute"));
        data += String.format("%02d", getData);
        return data;

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

    //将px转换为dp
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}