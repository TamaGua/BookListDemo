package com.example.lightdemo.Widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.lightdemo.Bean.Author;
import com.example.lightdemo.Bean.BookBitmap;
import com.example.lightdemo.R;
import com.example.lightdemo.Tools.HttpUtils;

import java.util.List;

public class MyListView extends ScrollView {

    private View view;
    private View childAt;
    private Author author;

    private  ViewHolder viewHolder;
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
            view = View.inflate(context, R.layout.cell_book, null);
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

    private int itemNum = 18;//当前显示个数

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void refreshListData(View view, int pos) {
        //试图用ViewHolder替代
//        rlCover = (RelativeLayout) view.findViewById(R.id.rl_cover);
//        tvTitle = (TextView) view.findViewById(R.id.tv_title);
//        tvHot = (TextView) view.findViewById(R.id.tv_hot);
//        tvWords = (TextView) view.findViewById(R.id.tv_words);
//        tvAuthor = (TextView) view.findViewById(R.id.tv_author);
        viewHolder = new ViewHolder(view);
        rlCover = viewHolder.rlCover;
        tvTitle = viewHolder.tvTitle;
        tvHot = viewHolder.tvHot;
        tvWords = viewHolder.tvWords;
        tvAuthor = viewHolder.tvAuthor;

        BookBitmap bookBitmap = bookBitmaps.get(pos);

        rlCover.setBackground(bookBitmap.getCoverDraw());


        tvTitle.setText(bookBitmap.getName());
        float hot = (float) ((int) bookBitmap.getHot() / 10000 + ((int) bookBitmap.getHot() / 1000) * 0.1);
        tvHot.setText(hot + "万");
        float words = (float) ((int) bookBitmap.getWords() / 10000 + ((int) bookBitmap.getWords() / 1000) * 0.1);
        tvWords.setText(words + "万");
         author = bookBitmap.getAuthor();
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
    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void addData(List<BookBitmap> books) {
        page++;

        bookBitmaps.addAll(books);
        //移除末尾三个提示view
        for(int i = 0;i<3;i++){
//            if(page<4) childAt = linearLayout.getChildAt((page-2)*18);
//            else  childAt = linearLayout.getChildAt(2*18);
            childAt = linearLayout.getChildAt(itemNum);
            linearLayout.removeView(childAt);
        }
        //已显示item总数
        itemNum = (page-1)*18;



        //清理不可见书籍数据
        if(bookBitmaps.size()>36){
            for(int i = 0; i<18;i++){
                bookBitmaps.remove(i);
            }}

        if (bookBitmaps == null||linearLayout==null) return;
        for (int i = 18; i < bookBitmaps.size(); i++) {
             view = View.inflate(context, R.layout.cell_book, null);
            linearLayout.addView(view);
            refreshListData(view, i);
        }

        this.requestLayout();
        this.invalidate();
        this.firstIn = true;
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

    //导出子控件高度以便后续使用
    int childMeasuredHeight;
    int childMeasuredWidth;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (bookBitmaps == null) return;
        //设置子元素left初始值
        int left = 0;
        int top = 0;

        //设置图片高度自适应
         childAt = linearLayout.getChildAt(0);
        if(childAt==null)return;
         childMeasuredWidth = childAt.getMeasuredWidth();
         childMeasuredHeight = childAt.getMeasuredHeight();
        //给所有的子控件设置布局
        int childCount = linearLayout.getChildCount();
        View linearChild = getChildAt(0);
        linearChild.layout(0,0,windowWidth,childMeasuredHeight*(itemNum/3+1));


        //设置子控件宽度以适应一行三个
        for (int i = 0; i < childCount; i++) {
            childAt = linearLayout.getChildAt(i);
            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) childAt.getLayoutParams(); //取控件当前的布局参数
            linearParams.width = windowWidth / 3;// 控件的宽强制设为窗口的1/3
            linearParams.height = (int) (windowHeight / 3.5);
            childAt.setLayoutParams(linearParams);


        }

        for (int i = 0; i < childCount; i++) {

            View child = linearLayout.getChildAt(i);
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
//        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        //获取子view的个数
        int childCount;
        childCount = linearLayout.getChildCount();
        if (childCount == 0) {
            //如果没有子元素，则设置宽高大小为0
            setMeasuredDimension(0, 0);
            return;
        }
        //先测量子控件，再测量自己；
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(windowWidth, windowHeight);
    }
    int page = 2;
    public boolean firstIn = true;
    /**
     * 滚动触底时更新显示内容
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
//            if(scrollY>childMeasuredHeight*12){
//                for(int i = 0;i<18;i++){
//                    View view = linearLayout.getChildAt(0);
//
//                    linearLayout.removeView(view);
//                }
//                itemNum = linearLayout.getChildCount();
//                this.requestLayout();
//                this.invalidate();
//            }
            if(scrollY >childMeasuredHeight*itemNum/3-2500&&firstIn){
//       if(clampedY &&firstIn) {
           firstIn = false;


           //在加载时显示正在加载字样
           Log.i("测试", "到底了");
           TextView tv1 = new TextView(context);
           tv1.setText("");
           TextView tv2 = new TextView(context);
           tv2.setText("正在加载");
           tv2.setGravity(Gravity.CENTER_HORIZONTAL);
           tv2.setPadding(0,100,0,0);
           TextView tv3 = new TextView(context);
           tv3.setText("");

           linearLayout.addView(tv1);
           linearLayout.addView(tv2);
           linearLayout.addView(tv3);
           this.requestLayout();
           this.invalidate();
           final MyListView view = this;


        /*   new Handler().postDelayed(new Runnable() {
               @Override
                 public void run() {
//               view.scrollTo(0,0);
                 view.requestLayout();
                 view.invalidate();
                 view.fullScroll(FOCUS_DOWN);
                    }
                }, 0);*/

//           page = bookBitmaps.size() / 18+1;
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

        class ViewHolder {
            public RelativeLayout rlCover;//封面
            public TextView tvTitle;//标题
            public TextView tvAuthor;//作者
            public TextView tvWords;//字数
            public TextView tvHot;//热度
            public ViewHolder(@NonNull View itemView) {
                rlCover = (RelativeLayout)itemView.findViewById(R.id.rl_cover);
                tvTitle = (TextView)itemView.findViewById(R.id.tv_title);
                tvHot = (TextView)itemView.findViewById(R.id.tv_hot);
                tvWords = (TextView)itemView.findViewById(R.id.tv_words);
                tvAuthor = (TextView)itemView.findViewById(R.id.tv_author);

            }
        }

}


