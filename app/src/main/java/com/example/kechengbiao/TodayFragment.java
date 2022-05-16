package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;
    View view;
    int nowWeek,period;
    MainActivity activity;
    Calendar calendar;
    TextView tv_date,tv_tittle;
    Context context;
    int msum,asum,esum,weekSum;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //创建Fragment的布局
        View view = inflater.inflate(R.layout.fragment_today,container,false);
        activity = (MainActivity)getActivity();
        this.view = view;
        nowWeek = activity.getNowWeek();
        calendar = Calendar.getInstance();
        period = calendar.get(Calendar.DAY_OF_WEEK);
        getInitData();
        if(period==0)period=7;
        else period-=1;
        context = (Context) getActivity();
        initTittle();
        addCard();
        return view;
    }
    private void initTittle(){
        tv_date = view.findViewById(R.id.date_tittle);
        tv_date.setText("今天是星期"+convertDayOfWeek(period));
        //todo 这里可以设置点击事件来修改标语
        //tv_tittle = view.findViewById(R.id.tittle);
    }
    //添加每日的课程
    private void addCard(){
        dbOpenHelper = new DBOpenHelper(context, "data.db", null, 1);
        db = dbOpenHelper.getWritableDatabase();
        LinearLayout linearLayout;
        linearLayout = view.findViewById(R.id.morning_course_container);
        for (int i = 0; i < msum; i++) {

            Cursor cursor = db.query("coursedata", null, "period=? and begintime=?",
                    new String[]{String.valueOf(period*10+1),String.valueOf(i+1)}, null, null, "begintime asc");
            if(cursor.getCount()>0){
                //todo 计时 看要多久才下课
                while (cursor.moveToNext()){
                    courseDataClass courseData = getCourseData(cursor);
                    //判断是否是这周的课
                    if(nowWeek<=weekSum&&0<nowWeek&&courseData.week.substring(nowWeek-1,nowWeek).equals("1")){
                        LinearLayout card = new LinearLayout(context);
                        card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dip2px(context,50)));
                        card.setPadding(dip2px(context,30),dip2px(context,5),dip2px(context,10),0);
                        card.setOrientation(LinearLayout.HORIZONTAL);
                        card.setGravity(Gravity.CENTER_VERTICAL);
                        linearLayout.addView(card);
                        //添加左边的信息 时间
                        LinearLayout life = new LinearLayout(context);
                        life.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1));
                        life.setOrientation(LinearLayout.VERTICAL);
                        life.setGravity(Gravity.CENTER);
                        card.addView(life);
                        TextView textView = new TextView(context);
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        //i从0开始 要加1
                        textView.setText(String.format(Locale.CHINA,"%02d:%02d\n%02d:%02d",
                                getBeginHour(String.valueOf(10+i+1)),getBeginMinute(String.valueOf(10+i+1)),getEndHour(String.valueOf(10+i+courseData.sum)),getEndMinute(String.valueOf(10+i+courseData.sum))));
                        //todo 这里其实上课时间可以和下课时间颜色不同 来提醒
                        textView.setTextColor(getResources().getColor(R.color.gray));
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(12);
                        life.addView(textView);

                        //分割线
                        View v = new View(context);
                        v.setLayoutParams(new LinearLayout.LayoutParams(dip2px(context,4), dip2px(context,35)));
                        v.setBackground(context.getDrawable(R.drawable.circular_light_sky_blue));
                        card.addView(v);

                        //添加右边的信息
                        LinearLayout right = new LinearLayout(context);
                        right.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,8));
                        right.setPadding(dip2px(context,6),0,0,0);
                        right.setOrientation(LinearLayout.VERTICAL);
                        right.setGravity(Gravity.CENTER_VERTICAL);
                        card.addView(right);

                        textView = new TextView(context);
                        textView.setText(courseData.name);
                        textView.setTextColor(getResources().getColor(R.color.blackOfShow));
                        textView.setTextSize(17);
                        textView.setIncludeFontPadding(false);
                        right.addView(textView);

                        textView = new TextView(context);
                        textView.setIncludeFontPadding(false);
                        String str = "第"+courseData.beginTime+"-"+(courseData.beginTime+courseData.sum-1)+"节";
                        if(courseData.classroom!=null)str+="|"+courseData.classroom;
                        if(courseData.teacher!=null)str+="|"+courseData.teacher;
                        textView.setText(str);
                        textView.setTextColor(getResources().getColor(R.color.gray));
                        textView.setTextSize(12);
                        textView.setPadding(0,0,0,0);
                        right.addView(textView);

                    }
                }
            }
        }
        linearLayout = view.findViewById(R.id.afternoon_course_container);
        for (int i = 0; i < asum; i++) {

            Cursor cursor = db.query("coursedata", null, "period=? and begintime=?",
                    new String[]{String.valueOf(period*10+2),String.valueOf(i+1+msum)}, null, null, "begintime asc");
            if(cursor.getCount()>0){
                //todo 计时 看要多久才下课
                while (cursor.moveToNext()){
                    courseDataClass courseData = getCourseData(cursor);
                    //判断是否是这周的课
                    if(nowWeek<=weekSum&&0<nowWeek&&courseData.week.substring(nowWeek-1,nowWeek).equals("1")){
                        LinearLayout card = new LinearLayout(context);
                        card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dip2px(context,50)));
                        card.setPadding(dip2px(context,30),dip2px(context,5),dip2px(context,10),0);
                        card.setOrientation(LinearLayout.HORIZONTAL);
                        card.setGravity(Gravity.CENTER_VERTICAL);
                        linearLayout.addView(card);
                        //添加左边的信息 时间
                        LinearLayout life = new LinearLayout(context);
                        life.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1));
                        life.setOrientation(LinearLayout.VERTICAL);
                        life.setGravity(Gravity.CENTER);
                        card.addView(life);
                        TextView textView = new TextView(context);
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        //i从0开始 要加1
                        textView.setText(String.format(Locale.CHINA,"%02d:%02d\n%02d:%02d",
                                getBeginHour(String.valueOf(20+i+1)),getBeginMinute(String.valueOf(20+i+1)),getEndHour(String.valueOf(20+i+courseData.sum)),getEndMinute(String.valueOf(20+i+courseData.sum))));
                        //todo 这里其实上课时间可以和下课时间颜色不同 来提醒
                        textView.setTextColor(getResources().getColor(R.color.gray));
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(12);
                        life.addView(textView);

                        //分割线
                        View v = new View(context);
                        v.setLayoutParams(new LinearLayout.LayoutParams(dip2px(context,4), dip2px(context,35)));
                        v.setBackground(context.getDrawable(R.drawable.circular_light_sky_blue));
                        card.addView(v);

                        //添加右边的信息
                        LinearLayout right = new LinearLayout(context);
                        right.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,8));
                        right.setPadding(dip2px(context,6),0,0,0);
                        right.setOrientation(LinearLayout.VERTICAL);
                        right.setGravity(Gravity.CENTER_VERTICAL);
                        card.addView(right);

                        textView = new TextView(context);
                        textView.setText(courseData.name);
                        textView.setTextColor(getResources().getColor(R.color.blackOfShow));
                        textView.setTextSize(17);
                        textView.setIncludeFontPadding(false);
                        right.addView(textView);

                        textView = new TextView(context);
                        textView.setIncludeFontPadding(false);
                        String str = "第"+courseData.beginTime+"-"+(courseData.beginTime+courseData.sum-1)+"节";
                        if(courseData.classroom!=null)str+="|"+courseData.classroom;
                        if(courseData.teacher!=null)str+="|"+courseData.teacher;
                        textView.setText(str);
                        textView.setTextColor(getResources().getColor(R.color.gray));
                        textView.setTextSize(12);
                        textView.setPadding(0,0,0,0);
                        right.addView(textView);

                    }
                }
            }
        }
        linearLayout = view.findViewById(R.id.evening_course_container);
        for (int i = 0; i < esum; i++) {

            Cursor cursor = db.query("coursedata", null, "period=? and begintime=?",
                    new String[]{String.valueOf(period*10+3),String.valueOf(i+1+msum+esum)}, null, null, "begintime asc");
            if(cursor.getCount()>0){
                //todo 计时 看要多久才下课
                while (cursor.moveToNext()){
                    courseDataClass courseData = getCourseData(cursor);
                    //判断是否是这周的课
                    if(nowWeek<=weekSum&&0<nowWeek&&courseData.week.substring(nowWeek-1,nowWeek).equals("1")){
                        LinearLayout card = new LinearLayout(context);
                        card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dip2px(context,50)));
                        card.setPadding(dip2px(context,30),dip2px(context,5),dip2px(context,10),0);
                        card.setOrientation(LinearLayout.HORIZONTAL);
                        card.setGravity(Gravity.CENTER_VERTICAL);
                        linearLayout.addView(card);
                        //添加左边的信息 时间
                        LinearLayout life = new LinearLayout(context);
                        life.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1));
                        life.setOrientation(LinearLayout.VERTICAL);
                        life.setGravity(Gravity.CENTER);
                        card.addView(life);
                        TextView textView = new TextView(context);
                        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        //i从0开始 要加1
                        textView.setText(String.format(Locale.CHINA,"%02d:%02d\n%02d:%02d",
                                getBeginHour(String.valueOf(30+i+1)),getBeginMinute(String.valueOf(30+i+1)),getEndHour(String.valueOf(30+i+courseData.sum)),getEndMinute(String.valueOf(30+i+courseData.sum))));
                        //todo 这里其实上课时间可以和下课时间颜色不同 来提醒
                        textView.setTextColor(getResources().getColor(R.color.gray));
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(12);
                        life.addView(textView);

                        //分割线
                        View v = new View(context);
                        v.setLayoutParams(new LinearLayout.LayoutParams(dip2px(context,4), dip2px(context,35)));
                        v.setBackground(context.getDrawable(R.drawable.circular_light_sky_blue));
                        card.addView(v);

                        //添加右边的信息
                        LinearLayout right = new LinearLayout(context);
                        right.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,8));
                        right.setPadding(dip2px(context,6),0,0,0);
                        right.setOrientation(LinearLayout.VERTICAL);
                        right.setGravity(Gravity.CENTER_VERTICAL);
                        card.addView(right);

                        textView = new TextView(context);
                        textView.setText(courseData.name);
                        textView.setTextColor(getResources().getColor(R.color.blackOfShow));
                        textView.setTextSize(17);
                        textView.setIncludeFontPadding(false);
                        right.addView(textView);

                        textView = new TextView(context);
                        textView.setIncludeFontPadding(false);
                        String str = "第"+courseData.beginTime+"-"+(courseData.beginTime+courseData.sum-1)+"节";
                        if(courseData.classroom!=null)str+="|"+courseData.classroom;
                        if(courseData.teacher!=null)str+="|"+courseData.teacher;
                        textView.setText(str);
                        textView.setTextColor(getResources().getColor(R.color.gray));
                        textView.setTextSize(12);
                        textView.setPadding(0,0,0,0);
                        right.addView(textView);

                    }
                }
            }
        }
    }

    //将px转换为dp
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //获取数据库数据 todo 这里的数据如果在主activity获取再读取主activity可能会优化
    private void getInitData(){
        msum = activity.msum;
        esum = activity.esum;
        asum = activity.asum;
        weekSum = activity.weekSum;
    }
    //课程数据类
    private class courseDataClass {
        public String name, teacher, classroom, week, color;
        public int beginTime,sum,period,id;
    }
    //从cursor中获取课程信息
    private courseDataClass getCourseData(Cursor cursor){
        courseDataClass course = new courseDataClass();
        course.beginTime = cursor.getInt(cursor.getColumnIndexOrThrow("begintime"));
        course.sum = cursor.getInt(cursor.getColumnIndexOrThrow("sum"));
        course.color = cursor.getString(cursor.getColumnIndexOrThrow("color"));
        course.classroom = cursor.getString(cursor.getColumnIndexOrThrow("classroom"));
        course.week = cursor.getString(cursor.getColumnIndexOrThrow("week"));
        course.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        course.teacher = cursor.getString(cursor.getColumnIndexOrThrow("teacher"));
        course.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        course.period = cursor.getInt(cursor.getColumnIndexOrThrow("period"));
        return course;
    }

    //传入对应时期编号获取对应课程的时间
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
    //返回对应的周的汉字
    private String convertDayOfWeek(int day){
        switch (day){
            case 1:
                return  "一";
            case 2:
                return  "二";
            case 3:
                return  "三";
            case 4:
                return  "四";
            case 5:
                return  "五";
            case 6:
                return  "六";
            case 7:
                return  "日";
            default:
                return "零";
        }
    }
}