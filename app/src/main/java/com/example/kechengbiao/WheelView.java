package com.example.kechengbiao;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
//简单思路就是自己重写个Scrollview然后改改(迫真
//https://blog.csdn.net/yun382657988/article/details/84761433

public class WheelView extends ScrollView {
    //得到类的简写名称
    public static final String TAG = WheelView.class.getSimpleName();

    public static class OnWheelViewListener {
        //用来向外部传值 item为选择的值 selected为选择的index 并且默认初始时调用一次也就是说可以获得初始值
        public void onSelected(int selectedIndex, String item) {
        }
    }

    private Context context;

    private LinearLayout views;

    public WheelView(Context context) {
        super(context);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    //储存数据的
    List<String> items;
    //返回整个数据列表
    private List<String> getItems() {
        return items;
    }
    //设置数据
    public void setItems(List<String> list) {
        if (null == items) {//为空初始化
            items = new ArrayList<String>();
        }
        items.clear();//清除原数据
        items.addAll(list);

        // 前面和后面补全 防止空无法显示 因此
        for (int i = 0; i < offset; i++) {
            items.add(0, "");
            items.add("");
        }
        initData();
    }

    public static final int OFF_SET_DEFAULT = 1;
    int offset = OFF_SET_DEFAULT; // 偏移量（需要在最前面和最后面补全）

    public int getOffset() {
        return offset;
    }
    //设置偏移量 对话框中当前项上面和下面的项数
    public void setOffset(int offset) {
        this.offset = offset;
    }

    int displayItemCount; // 每页显示的数量

    int selectedIndex = 1;


    private void init(Context context) {
        this.context = context;
        //取消Vertical ScrollBar显示
        this.setVerticalScrollBarEnabled(false);
        //线性布局
        views = new LinearLayout(context);
        //设置垂直方向
        views.setOrientation(LinearLayout.VERTICAL);
        //
        this.addView(views);
        //匿名实现Runnable接口并重写run方法
        scrollerTask = new Runnable() {

            public void run() {
                //获取y坐标 判断是否停止滚动
                int newY = getScrollY();//监听现在的坐标
                if (initialY - newY == 0) { // 如果和上次坐标相等就是停止了
                    final int remainder = initialY % itemHeight;//判断滚多了多少
                    final int divided = initialY / itemHeight;//判断滚到了第几个
                    if (remainder == 0) {
                        selectedIndex = divided + offset;//加上偏移量

                        onSelectedCallBack();
                    } else {
                        //校准位置 保证每次都选中一个选项
                        if (remainder > itemHeight / 2) {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    WheelView.this.smoothScrollTo(0, initialY - remainder + itemHeight);
                                    selectedIndex = divided + offset + 1;
                                    onSelectedCallBack();
                                }
                            });
                        } else {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    WheelView.this.smoothScrollTo(0, initialY - remainder);
                                    selectedIndex = divided + offset;
                                    onSelectedCallBack();
                                }
                            });
                        }
                    }
                }
                //如果没停下来就继续更新上一次的坐标
                else {
                    initialY = getScrollY();
                    WheelView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };
        // 默认初始不滑动时执行一次回调
        if (null != onWheelViewListener) {
            onWheelViewListener.onSelected(selectedIndex, items.get(selectedIndex));
        }
    }

    int initialY;

    Runnable scrollerTask;
    int newCheck = 50;

    //启动线程任务
    public void startScrollerTask() {

        initialY = getScrollY();
        this.postDelayed(scrollerTask, newCheck);
    }

    private void initData() {
        displayItemCount = offset * 2 + 1;

        views.removeAllViews();
        for (String item : items) {
            views.addView(createView(item));
        }
        //初始化位置
        refreshItemView(0);
    }

    int itemHeight = 0;

    //创建对应文本
    private TextView createView(String item) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv.setText(item);
        tv.setGravity(Gravity.CENTER);
        int padding = dip2px(15);
        tv.setPadding(padding, padding, padding, padding);
        if (0 == itemHeight) {
            itemHeight = getViewMeasuredHeight(tv);
            views.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, itemHeight * displayItemCount, Gravity.CENTER_HORIZONTAL));
            views.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount));
        }
        return tv;
    }



    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //获取位置来判断选择哪一个
        refreshItemView(t);

        if (t > oldt) {
            scrollDirection = SCROLL_DIRECTION_DOWN;
        } else {
            scrollDirection = SCROLL_DIRECTION_UP;
        }
    }

    //设置位置
    private void refreshItemView(int y) {
        int position = y / itemHeight + offset;
        int remainder = y % itemHeight;
        int divided = y / itemHeight;

        if (remainder == 0) {
            position = divided + offset;
        } else {
            if (remainder > itemHeight / 2) {
                position = divided + offset + 1;
            }
        }
        //设置文本颜色
        int childSize = views.getChildCount();
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                return;
            }
            if (position == i) {
                //选中的颜色
                itemView.setTextColor(context.getResources().getColor(R.color.DoderBlue));
            } else {
                //没选择的颜色
                itemView.setTextColor(context.getResources().getColor(R.color.gray));
            }
        }
    }

    /**
     * 获取选中区域的边界
     */
    int[] selectedAreaBorder;
    //获取选中区的位置
    private int[] obtainSelectedAreaBorder() {
        if (null == selectedAreaBorder) {
            selectedAreaBorder = new int[2];
            selectedAreaBorder[0] = itemHeight * offset;
            selectedAreaBorder[1] = itemHeight * (offset + 1);
        }
        return selectedAreaBorder;
    }


    private int scrollDirection = -1;
    private static final int SCROLL_DIRECTION_UP = 0;
    private static final int SCROLL_DIRECTION_DOWN = 1;

    Paint paint;
    int viewWidth;
    //画线
    @Override
    public void setBackgroundDrawable(Drawable background) {
        //计算宽度
        if (viewWidth == 0) {
//            viewWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
            viewWidth = views.getWidth();
        }

        if (null == paint) {
            paint = new Paint();
            //画的线的颜色
            paint.setColor(getResources().getColor(R.color.somber));
            paint.setStrokeWidth(dip2px(1f));
        }

        background = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                canvas.drawLine(viewWidth * 1 / 6, obtainSelectedAreaBorder()[0], viewWidth * 5 / 6, obtainSelectedAreaBorder()[0], paint);
                canvas.drawLine(viewWidth * 1 / 6, obtainSelectedAreaBorder()[1], viewWidth * 5 / 6, obtainSelectedAreaBorder()[1], paint);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter cf) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }
        };
        super.setBackgroundDrawable(background);

    }

    //页面绘制完成后调用划线函数
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        setBackgroundDrawable(null);
    }

    /**
     * 选中回调 传值供外部监听监听
     */
    private void onSelectedCallBack() {
        if (null != onWheelViewListener) {
            onWheelViewListener.onSelected(selectedIndex, items.get(selectedIndex));
        }

    }

    //设置默认选择哪一个
    public void setSelection(int position) {
        final int p = position;
        selectedIndex = p + offset;
        this.post(new Runnable() {
            @Override
            public void run() {
                WheelView.this.smoothScrollTo(0, p * itemHeight);
            }
        });

    }

    public String getSelectedItem() {
        return items.get(selectedIndex);
    }
    public int getSelectedIndex() {
        return selectedIndex - offset;
    }


    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 3);
    }

    //
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }

    private OnWheelViewListener onWheelViewListener;

    public OnWheelViewListener getOnWheelViewListener() {
        return onWheelViewListener;
    }

    public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
        this.onWheelViewListener = onWheelViewListener;
        // 默认初始不滑动时执行一次回调
        onWheelViewListener.onSelected(selectedIndex, items.get(selectedIndex));
    }
    //根据手机的分辨率从 dp 的单位 转成为 px(像素)
    private int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //测量大小
    private int getViewMeasuredHeight(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                View.MeasureSpec.AT_MOST);
        //先替系统绘制完才能获取到高度 不然就是0
        view.measure(width, expandSpec);
        return view.getMeasuredHeight();
    }
}
