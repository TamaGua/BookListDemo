package com.example.lightdemo.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lightdemo.Bean.Author;
import com.example.lightdemo.Bean.BookBitmap;
import com.example.lightdemo.R;
import com.example.lightdemo.tools.HttpUtils;
import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
//    List<Book> books;
    List<BookBitmap> bookBitmaps;
    Context context;
    private final static int TYPE_CONTENT=0;//正常内容
    private final static int TYPE_FOOTER=1;//下拉刷新

    //调出viewholder以便于外部控制
    FootViewHolder footViewHolder;
    @Override
    public int getItemViewType(int position) {
        if (position==bookBitmaps.size()){
            return TYPE_FOOTER;
        }
        return TYPE_CONTENT;
    }

    public BookListAdapter(List<BookBitmap> bookBitmaps, Context context) {
        this.bookBitmaps = bookBitmaps;
        this.context = context;
//        initLoader();
    }
    @SuppressLint("NewApi")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==TYPE_FOOTER){
            View view = LayoutInflater.from(context).inflate(R.layout.activity_main_foot, parent, false);
            return new BookListAdapter.FootViewHolder(view);
        }else{

        View view= LayoutInflater.from(context).inflate(R.layout.cell_book,parent,false);
        return new BookListAdapter.MyViewHolder(view);}
    }

    //隐藏
    public boolean hideFootViewHolder(){
        if(footViewHolder==null) return false;
        else {
            footViewHolder.hideProgress();
            return true;
        }
    }
    //显示
    public boolean showFootViewHolder(){
        if(footViewHolder==null) return false;
        else {
            footViewHolder.showProgress();
            return true;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position)==TYPE_FOOTER){
            final FootViewHolder viewHolder = (FootViewHolder)holder;
            footViewHolder = viewHolder;
//            viewHolder.progressBar.hide();

            new Thread(new Runnable(){
                @Override
                public void run() {
                    HttpUtils.sendPostMessage("utf-8", 1,context);

                }
            }).start();

        }
        else{
            MyViewHolder viewHolder= (MyViewHolder) holder;
            BookBitmap bookBitmap = bookBitmaps.get(position);
            viewHolder.rlCover.setBackground(bookBitmap.getCoverDraw());
            viewHolder.tvTitle.setText(bookBitmap.getName());
            float hot = (float) ((int)bookBitmap.getHot()/10000+((int)bookBitmap.getHot()/1000)*0.1);
            viewHolder.tvHot.setText(hot+"万");
            float words =  (float) ((int)bookBitmap.getWords()/10000+((int)bookBitmap.getWords()/1000)*0.1);
            viewHolder.tvWords.setText(words+"万");
            Author author = bookBitmap.getAuthor();
            if(author!=null){
                viewHolder.tvAuthor.setText(bookBitmap.getAuthor().getNick());
            }



        }
    }


    @Override
    public int getItemCount() {
        return bookBitmaps.size()+1;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        public RelativeLayout rlCover;//封面
        public TextView tvTitle;//标题
        public TextView tvAuthor;//作者
        public TextView tvWords;//字数
        public  TextView tvHot;//热度
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            rlCover = (RelativeLayout)itemView.findViewById(R.id.rl_cover);
            tvTitle = (TextView)itemView.findViewById(R.id.tv_title);
            tvHot = (TextView)itemView.findViewById(R.id.tv_hot);
            tvWords = (TextView)itemView.findViewById(R.id.tv_words);
            tvAuthor = (TextView)itemView.findViewById(R.id.tv_author);

        }
    }

    public class FootViewHolder extends RecyclerView.ViewHolder{
        private ContentLoadingProgressBar progressBar;
        public  void hideProgress(){progressBar.hide();}

        public  void showProgress(){progressBar.show();}

        @RequiresApi(api = Build.VERSION_CODES.M)
        public FootViewHolder(View itemView) {
            super(itemView);
            progressBar=(ContentLoadingProgressBar)itemView.findViewById(R.id.pb_progress);
            hideProgress();
        }
    }



}
