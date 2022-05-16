package com.example.kechengbiao;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class KcbFragment extends Fragment {

    private DBOpenHelper dbOpenHelper;
    private SQLiteDatabase db;
    private int nowWeek;//记录是第几周
    private Fragment fragment;
    private View view;
    private Context context;
    private int msum, asum, esum,weekSum;
    private boolean isShowWeekend;
    private int dayOfWeek;//记录是否要
    //储存课程展示容器的列表
    private ArrayList<LinearLayout> ml_list = new ArrayList<>();
    private ArrayList<LinearLayout> al_list = new ArrayList<>();
    private ArrayList<LinearLayout> el_list = new ArrayList<>();
    private LinearLayout selectLinearLayout;
    private MainActivity activity;
    private TextView nextWeek, lastWeek,tittle;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //创建Fragment的布局
        view = inflater.inflate(R.layout.fragment_kcb,container,false);
        activity = (MainActivity)getActivity();
        nowWeek = activity.getNowWeek();
        context = getActivity();
        dbOpenHelper = new DBOpenHelper(context, "data.db", null, 1);
        db = dbOpenHelper.getWritableDatabase();
        Log.d(TAG, "onCreateView: 1");
        initButton();
        getContainer();
        getInitData();
        initTimeShow();
        addClassCard();
        return view;
    }
    //获取数据库数据 todo 这里的数据如果在主activity获取再读取主activity可能会优化
    private void getInitData(){
        //获取有多少节课
        Cursor cursor ;
        msum = activity.msum;
        esum = activity.esum;
        asum = activity.asum;
        weekSum = activity.weekSum;
        //获取每节课时间是否固定
        cursor = db.query("setting",
                null,
                "name=?",
                new String[]{"isshowweekend"},
                null,
                null,
                null);
        cursor.moveToFirst();

        int i = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
        isShowWeekend = i==1;


    }


    //生成展示课程时间卡
    private void initTimeShow(){
        LinearLayout container = (LinearLayout) view.findViewById(R.id.MorningTime);
        LinearLayout card;
        TextView textView;
        for (int i = 1; i <= msum; i++) {
            //设置布局
            card = new LinearLayout(context);
            card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(context,60)));
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(context.getDrawable(R.drawable.solid));
            card.setGravity(Gravity.CENTER);
            container.addView(card);
            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.valueOf(i));
            textView.setTextColor(getResources().getColor(R.color.blackOfShow));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(14);
            card.addView(textView);
            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.format(Locale.CHINA,"%02d:%02d\n%02d:%02d",
                    getBeginHour(String.valueOf(10+i)),getBeginMinute(String.valueOf(10+i)),getEndHour(String.valueOf(10+i)),getEndMinute(String.valueOf(10+i))));
            textView.setTextColor(getResources().getColor(R.color.gray));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(12);
            card.addView(textView);

        }
        container = (LinearLayout) view.findViewById(R.id.AfternoonTime);
        for (int i = 1; i <= asum; i++) {
            //设置布局
            card = new LinearLayout(context);
            card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(context,60)));
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(context.getDrawable(R.drawable.solid));
            container.addView(card);
            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.valueOf(i+msum));
            textView.setTextColor(getResources().getColor(R.color.blackOfShow));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(14);
            card.addView(textView);
            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.format(Locale.CHINA,"%02d:%02d\n%02d:%02d",
                    getBeginHour(String.valueOf(20+i)),getBeginMinute(String.valueOf(20+i)),getEndHour(String.valueOf(20+i)),getEndMinute(String.valueOf(20+i))));
            textView.setTextColor(getResources().getColor(R.color.gray));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(12);
            card.addView(textView);

        }
        container = (LinearLayout) view.findViewById(R.id.EveningTime);
        for (int i = 1; i <= esum; i++) {
            //设置布局
            card = new LinearLayout(context);
            card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dip2px(context,60)));
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(context.getDrawable(R.drawable.solid));
            container.addView(card);
            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.valueOf(i+msum+asum));
            textView.setTextColor(getResources().getColor(R.color.blackOfShow));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(14);
            card.addView(textView);
            textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.format(Locale.CHINA,"%02d:%02d\n%02d:%02d",
                    getBeginHour(String.valueOf(30+i)),getBeginMinute(String.valueOf(30+i)),getEndHour(String.valueOf(30+i)),getEndMinute(String.valueOf(30+i))));
            textView.setTextColor(getResources().getColor(R.color.gray));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(12);
            card.addView(textView);

        }
    }

    //生成课程展示卡
    private void addClassCard(){
        LinearLayout container;
        LinearLayout card;
        TextView textView;
        int day = isShowWeekend? 7:5;
        for (int i = 1; i <= day; i++) {
            for (int j = 1; j <= 3; j++) {
                int sum,bt;
                //判断是上午还是下午还是晚上
                switch (j){
                    case 1:
                        bt=0;//修正是第几节课
                        sum = msum;
                        break;
                    case 2:
                        bt = msum;
                        sum = asum;
                        break;
                    case 3:
                        bt = msum+asum;
                        sum = esum;
                        break;
                    default:
                        bt = 0;
                        sum = 0;
                        break;
                }

                for (int k = 1; k <= sum; k++) {
                    card = new LinearLayout(context);
                    Cursor cursor = db.query("coursedata", null, "period=? and begintime=?",
                            new String[]{String.valueOf(i*10+j),String.valueOf(k+bt)}, null, null, "begintime asc");
                    //选择容器
                    switch (j){
                        case 1:
                            container = ml_list.get(i-1);
                            break;
                        case 2:
                            container = al_list.get(i-1);
                            break;
                        case 3:
                            container = el_list.get(i-1);
                            break;
                        default:
                            container = null;
                            break;
                    }
                    if(cursor.getCount()>0){
                        //记录是否添加了课程
                        boolean haveCourse=false;
                        while(cursor.moveToNext()){
                            //课程数据类储存信息
                            courseDataClass courseData = getCourseData(cursor);
                            //判断是否为当前周的课
                            if(nowWeek<=weekSum&&0<nowWeek&&courseData.week.substring(nowWeek-1,nowWeek).equals("1")){
                                //todo 单独使用外边距来隔开 会出现对不齐的问题 可以尝试设定一个背景来代替外边距来隔开(白色边框)
                                LinearLayout body = new LinearLayout(context);

                                haveCourse=true;
                                //设置布局
                                card = new LinearLayout(context);
                                body.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        dip2px(context,60)*courseData.sum));
                                body.setOrientation(LinearLayout.VERTICAL);
                                body.setBackground(ContextCompat.getDrawable(context,R.drawable.solid));
                                //LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)body.getLayoutParams();
                                //layoutParams.setMargins(dip2px(context,1),dip2px(context,1),dip2px(context,1),dip2px(context,1));
                                body.setPadding(dip2px(context,3),dip2px(context,3),dip2px(context,3),dip2px(context,3));
                                card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT));
                                //todo 颜色设置
                                card.setBackgroundColor(ContextCompat.getColor(context,R.color.bg_blue1));
                                body.addView(card);
                                body.setTag(courseData);
                                body.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        editCourse(view);
                                    }
                                });

                                container.addView(body);
                                //设置文本布局和内容
                                textView = new TextView(context);
                                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                                textView.setTextColor(context.getColor(R.color.show_blue1));
                                String str = courseData.name+"\n";
                                if(courseData.classroom!=null) str += courseData.classroom+"\n";
                                if(courseData.teacher!=null) str += courseData.teacher;
                                textView.setText(str);
                                textView.setGravity(Gravity.CENTER);
                                textView.setTextSize(15);
                                card.addView(textView);
                                k+=courseData.sum-1;//加上所占的课程 但是本身还++了所以要-1
                                break;
                            }
                        }

                        //没课
                        if(!haveCourse){
                            //设置布局
                            card = new LinearLayout(context);
                            card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    dip2px(context,60)));
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackground(ContextCompat.getDrawable(context,R.drawable.solid));
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)card.getLayoutParams();
                            //layoutParams.setMargins(dip2px(context,1),dip2px(context,1),dip2px(context,1),dip2px(context,1));
                            card.setPadding(dip2px(context,2),dip2px(context,2),dip2px(context,2),dip2px(context,2));
                            courseDataClass dt = new courseDataClass();
                            dt.period = i*10+j;//保存这个选项卡是在周几的什么时间段的数据
                            dt.beginTime = j>2?msum+asum+k:(j>1?msum+k:k);//保存这个选项卡是第几节课的数据
                            card.setTag(dt);
                            //todo 颜色设置
                            //card.setBackgroundColor(ContextCompat.getColor(context,R.color.bg_blue1));
                            //选择容器
                            card.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    addCourse(view);

                                }
                            });

                            container.addView(card);
                        }
                    }//没课
                    else{
                        //设置布局
                        card = new LinearLayout(context);
                        card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                dip2px(context,60)));
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackground(ContextCompat.getDrawable(context,R.drawable.solid));
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)card.getLayoutParams();
                        courseDataClass dt = new courseDataClass();
                        dt.period = i*10+j;//保存这个选项卡是在周几的什么时间段的数据
                        dt.beginTime = j>2?msum+asum+k:(j>1?msum+k:k);//保存这个选项卡是第几节课的数据
                        card.setTag(dt);
                        //todo 颜色设置
                        //card.setBackgroundColor(ContextCompat.getColor(context,R.color.bg_blue1));
                        //选择容器
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                addCourse(view);

                            }
                        });

                        container.addView(card);
                    }
                }


            }
        }
    }
    // todo 添加课程的点击事件
    private void addCourse(View view){
        if(selectLinearLayout == (LinearLayout) view){
            //跳转
            Intent intent = new Intent(context,SettingCourse.class);
            courseDataClass dt = (courseDataClass) view.getTag();
            intent.putExtra("beginTime",dt.beginTime);
            intent.putExtra("period",dt.period);
            intent.putExtra("isAdd",true);
            startActivityForResult(intent,666);
        }
        else{
            Toast.makeText(context,"再次点击添加课程",Toast.LENGTH_SHORT).show();
            if(selectLinearLayout!=null) selectLinearLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.solid));
            selectLinearLayout = (LinearLayout) view;
            //todo 选中的选择卡的背景
            view.setBackgroundColor(ContextCompat.getColor(context,R.color.cyan_bg));
        }
    }
    //todo 编辑现有课程
    private void editCourse(View view){
        if(selectLinearLayout == (LinearLayout) view){

            Intent intent = new Intent(context,SettingCourse.class);
            courseDataClass dt = (courseDataClass) view.getTag();
            intent.putExtra("beginTime",dt.beginTime);
            intent.putExtra("isAdd",false);
            intent.putExtra("id",dt.id);
            intent.putExtra("sum",dt.sum);
            intent.putExtra("name",dt.name);
            intent.putExtra("teacher",dt.teacher);
            intent.putExtra("classroom",dt.classroom);
            intent.putExtra("week",dt.week);
            intent.putExtra("period",dt.period);
            startActivityForResult(intent,666);
        }
        else{
            Toast.makeText(context,"再次点击编辑课程",Toast.LENGTH_SHORT).show();
            selectLinearLayout = (LinearLayout) view;
        }
    }

    //返回该窗口时回调的函数
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==0&&requestCode==666){
            //处理回调相关操作 todo
            Log.d(TAG, "onActivityResult: 1");
            activity.reLoadKcb(nowWeek);
        }
    }


    //获取展示课程容器控件
    private void getContainer(){
        ml_list.add(view.findViewById(R.id.MonMorning));
        ml_list.add(view.findViewById(R.id.TueMorning));
        ml_list.add(view.findViewById(R.id.WebMorning));
        ml_list.add(view.findViewById(R.id.ThuMorning));
        ml_list.add(view.findViewById(R.id.FriMorning));

        al_list.add(view.findViewById(R.id.MonAfternoon));
        al_list.add(view.findViewById(R.id.TueAfternoon));
        al_list.add(view.findViewById(R.id.WebAfternoon));
        al_list.add(view.findViewById(R.id.ThuAfternoon));
        al_list.add(view.findViewById(R.id.FriAfternoon));

        el_list.add(view.findViewById(R.id.MonEvening));
        el_list.add(view.findViewById(R.id.TueEvening));
        el_list.add(view.findViewById(R.id.WebEvening));
        el_list.add(view.findViewById(R.id.ThuEvening));
        el_list.add(view.findViewById(R.id.FriEvening));

        //Todo 动态添加周末的控件
        if(isShowWeekend){

        }
    }

    private void initButton(){
        nextWeek = view.findViewById(R.id.NextWeek);
        nextWeek.setText("->");
        nextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowWeek<weekSum){
                    activity.reLoadKcb(nowWeek+1);
                }
                else{
                    Toast.makeText(context,"已经是最后一周惹",Toast.LENGTH_SHORT).show();
                }
            }
        });
        lastWeek = view.findViewById(R.id.LastWeek);
        lastWeek.setText("<-");
        lastWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowWeek>1){
                    activity.reLoadKcb(nowWeek-1);
                }
                else{
                    Toast.makeText(context,"前方是虚空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        tittle = view.findViewById(R.id.WhenTime);
        tittle.setText("第"+nowWeek+"周");
    }

    //删除所有课程展示卡
    private void delClassCard() {

    }

    //课程数据类
    private class courseDataClass {
        public String name, teacher, classroom, week, color;
        public int beginTime,sum,period,id;
    }

    //从cursor中获取课程信息 todo 有bug重叠课程 应该改用id才行
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
    //将px转换为dp
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}