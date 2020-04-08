package com.example.lightdemo.Widget;

import android.content.Context;
import android.icu.util.Freezable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.lightdemo.Bean.Author;
import com.example.lightdemo.Bean.BookBitmap;
import com.example.lightdemo.R;
import com.example.lightdemo.tools.HttpUtils;

import java.util.List;

public class MyListView extends ScrollView {

    private List<BookBitmap> bookBitmaps;
    private Context context;
    private int windowWidth = 0;
    private int windowHeight = 0;

    private int bookNum = 0;
    public MyListView(Context context) {
        super(context);
        this.context = context;
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

    }

    LinearLayout linearLayout;
    /**
     * 显示自定义界面
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void init(List<BookBitmap> bookBitmaps) {
        if (bookBitmaps == null) return;
        else this.bookBitmaps = bookBitmaps;
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setId(R.id.ll);

        for (int i = 0; i < bookBitmaps.size(); i++) {
            View view = View.inflate(context, R.layout.cell_book, null);
            linearLayout.addView(view);
            refreshListData(view, i);
        }
        this.addView(linearLayout);
        bookNum = bookBitmaps.size();
    }

    public RelativeLayout rlCover;//封面
    public TextView tvTitle;//标题
    public TextView tvAuthor;//作者
    public TextView tvWords;//字数
    public TextView tvHot;//热度

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void refreshListData(View view, int pos) {
        rlCover = (RelativeLayout) view.findViewById(R.id.rl_cover);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvHot = (TextView) view.findViewById(R.id.tv_hot);
        tvWords = (TextView) view.findViewById(R.id.tv_words);
        tvAuthor = (TextView) view.findViewById(R.id.tv_author);
        BookBitmap bookBitmap = bookBitmaps.get(pos);
        rlCover.setBackground(bookBitmap.getCoverDraw());
        tvTitle.setText(bookBitmap.getName());
        float hot = (float) ((int) bookBitmap.getHot() / 10000 + ((int) bookBitmap.getHot() / 1000) * 0.1);
        tvHot.setText(hot + "万");
        float words = (float) ((int) bookBitmap.getWords() / 10000 + ((int) bookBitmap.getWords() / 1000) * 0.1);
        tvWords.setText(words + "万");
        Author author = bookBitmap.getAuthor();
        if (author != null) {
            tvAuthor.setText(bookBitmap.getAuthor().getNick());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void refreshData(List<BookBitmap> bookBitmaps){
        //将原有数据都清空后再次初始化数据
        this.bookBitmaps = bookBitmaps;
        init(bookBitmaps);

    }
    /**
     *追加数据
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void addData(List<BookBitmap> books) {
        int num = bookBitmaps.size();

        linearLayout = (LinearLayout)findViewById(R.id.ll);
        if (bookBitmaps == null||linearLayout==null) return;
//        else this.bookBitmaps.addAll(bookBitmaps);
        for (int i = bookNum; i < bookBitmaps.size(); i++) {
            View view = View.inflate(context, R.layout.cell_book, null);
            linearLayout.addView(view);
            refreshListData(view, i);
        }
        bookNum = bookBitmaps.size();
        this.requestLayout();
        this.invalidate();
    }

    /**
     * 清空列表
     * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void clearData() {
        bookBitmaps=null;
        bookNum =0;
        this.removeAllViews();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (bookBitmaps == null) return;
        //设置子元素left初始值
        int left = 0;
        int top = 0;

        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
        View childAt = ll.getChildAt(0);

        int childMeasuredWidth = childAt.getMeasuredWidth();
        int childMeasuredHeight = childAt.getMeasuredHeight();

        View linearChild = getChildAt(0);
        linearChild.layout(0,0,windowWidth,childMeasuredHeight*(bookBitmaps.size())/3);
        //给所有的子控件设置布局
        int childCount = ll.getChildCount();

        //设置子控件宽度以适应一行三个
        for (int i = 0; i < childCount; i++) {
            childAt = ll.getChildAt(i);
            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) childAt.getLayoutParams(); //取控件当前的布局参数
            linearParams.width = windowWidth / 3;// 控件的宽强制设成1/3
            childAt.setLayoutParams(linearParams);
        }

        for (int i = 0; i < childCount; i++) {

            View child = ll.getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            //子view的上下左右的值，均是相对于父控件的
            child.layout(left, top, left + childMeasuredWidth, top + childMeasuredHeight);
            left = left + childMeasuredWidth;
            //排列到第三个时换行
            if ((i + 1) % 3 == 0) {
                left = 0;
                top = top + childMeasuredHeight;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (bookBitmaps == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        //获取子view的个数
        int childCount;
        childCount = ll.getChildCount();
        if (childCount == 0) {
            //如果没有子元素，则设置宽高大小为0
            setMeasuredDimension(0, 0);
            return;
        }
        //先测量子控件，再测量自己；
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(windowWidth, windowHeight);
    }

    public boolean firstIn = true;
    /**
     * 滚动触底时更新显示内容
     */
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

       if(clampedY &&firstIn) {
           firstIn = false;
           Log.i("测试", "到底了");
           final int page = bookBitmaps.size() / 3 + 1;
           new Thread(new Runnable() {
               @Override
               public void run() {
                   HttpUtils.sendPostMessage("utf-8", page, context);
               }
           }).start();
       }
    }


    public void setWindowWidth(int windowWidth,int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

}


