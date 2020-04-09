package com.example.lightdemo.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import  android.app.Service;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.lightdemo.Bean.Book;
import com.example.lightdemo.Bean.BookBitmap;
import com.example.lightdemo.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpUtils {

    private static String PATH = "https://cirno.linovel.net/demo/book/list";
    private static URL url;
    private static List<BookBitmap> bookBitmaps;
    public static Messenger serviceMessager;
    public static Context context;

    public static final int FIRST_CONNECT = 0x111;
    public static final int DATA_OK = 0x121;
    public static final int UPDATE_UI = 0x121;





    public HttpUtils() {
        super();
    }

    static{
        try {
            url = new URL(PATH);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param encode 字节编码
     * @param page 当前加载页数
     * @return 返回数据
     */
    public static List<BookBitmap>  sendPostMessage (String encode, int page,Context con){

        if(con==null){
            Log.i("MSG","无上下文嘞");
            return null;
        }
        context = con;
        page = page;
        String result = null;
        try {
            String json = "{\"cat\":\"-1\",\"page\":"+page+",\"pageSize\":\"18\"}";
            OutputStreamWriter out;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setDoInput(true);//表示从服务器获取数据
            connection.setDoOutput(true);//表示向服务器写数据
            connection.setRequestMethod("POST");
            //是否使用缓存
            connection.setUseCaches(false);
            //表示设置请求体的类型是文本类型
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.connect();

            out = new OutputStreamWriter(connection.getOutputStream(),"utf-8");//解决传参时中文乱码
            out.write(json);
            out.flush();
            InputStream inStream = connection.getInputStream();
            result = new String(readInputStream(inStream),"utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        bookBitmaps= resultToBook(result);
        loadCover();

        return bookBitmaps;
    }
    //批量加载书籍封面
    private static void loadCover(){
        initLoader();
        Bitmap bitmap=null;
        String uri ;
        if(bookBitmaps!=null){
        for(BookBitmap bookBitmap:bookBitmaps){
            uri = "https://japari.linovel.net/v1/image/jump?XDEVICE=\n" +
                    "1&path="+bookBitmap.getCover();
            BitmapDrawable drawbale = null;
            //确保图片已正确转换为Bitmap
            while (bitmap==null||drawbale==null){
            bitmap = ImageLoader.getInstance().loadImageSync(uri);
            drawbale = new BitmapDrawable(context.getResources(),bitmap);
        }

            bookBitmap.setCoverDraw(drawbale);
        }
    }
        Message msgToClient = Message.obtain(null, DATA_OK);
        //往客户端发送消息
        try {if(serviceMessager!=null){
            serviceMessager.send(msgToClient);}
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void initLoader(){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false) //设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .showImageOnLoading(R.mipmap.ic_launcher)
                .showImageOnFail(R.mipmap.ic_launcher)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)//是否考虑JPEG图像EXIF参数（旋转，翻转）
                .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3) //线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的时候的URI名称用MD5 加密
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024) // 内存缓存的最大值
                .diskCacheSize(50 * 1024 * 1024)  // 50 Mb sd卡(本地)缓存的最大值
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .defaultDisplayImageOptions(options)// 由原先的discCache -> diskCache
                .diskCache(new UnlimitedDiskCache(new File(context.getExternalCacheDir().getAbsolutePath())))//自定义缓存路径
                .imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);//全局初始化此配置

    }
    private static List<BookBitmap> resultToBook(String result){
        //将返回的JSON字符串转化为Caegory
        JSONObject jsonObject = JSON.parseObject(result);
        JSONObject dataStr =  JSON.parseObject(jsonObject.getString("data"));
        JSONArray bookStr =  JSON.parseArray(dataStr.getString("books"));
        List<Book> returnCats = new ArrayList<>();
        List<BookBitmap> returnBitBooks = new ArrayList<>();
        BookBitmap tmp = new BookBitmap(0);
        returnCats = JSONObject.parseArray(bookStr.toJSONString(),Book.class);
        for(Book book:returnCats){
            tmp = new BookBitmap(0);
            tmp.setAuthor(book.getAuthor());
            tmp.setCover(book.getCover());
            tmp.setHot(book.getHot());
            tmp.setId(book.getId());
            tmp.setWords(book.getWords());
            tmp.setName(book.getName());
            returnBitBooks.add(tmp);
        }
        return returnBitBooks;
    }
    private static byte[] readInputStream(InputStream input) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len ;
        while ((len = input.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        input.close();
        return data;
}
    public static List<BookBitmap> getBookBitmaps(){
        return bookBitmaps;
    }



public static class MessengerService extends Service
{
    //service端Messager

//service端Handler
    @SuppressLint("HandlerLeak")
    private Messenger mMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(Message msgfromClient)
        {
            Message msgToClient = Message.obtain(msgfromClient);//返回给客户端的消息
            serviceMessager = msgfromClient.replyTo;
            switch (msgfromClient.what)
            {
                //msg 客户端传来的消息
                case FIRST_CONNECT:
                    Log.e("测试","服务器收到");
                    msgToClient.what = FIRST_CONNECT;
                    try
                    {
                        msgfromClient.replyTo.send(msgToClient);
                    }catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }
                    break;
            }

            super.handleMessage(msgfromClient);
        }
    });



    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }
}
}