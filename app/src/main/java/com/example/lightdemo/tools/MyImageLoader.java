package com.example.lightdemo.tools;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by keven on 2019/5/28.
 */
public class MyImageLoader {
    private LruCache<String, Bitmap> mLruCache;

    /**
     * 构造函数
     */
    public MyImageLoader() {
        //设置最大缓存空间为运行时内存的 1/8
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory ;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //计算一个元素的缓存大小
                return value.getByteCount();
            }
        };

    }

    /**
     * 添加图片到 LruCache
     *
     * @param key
     * @param bitmap
     */
    public void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    /**
     * 从缓存中获取图片
     *
     * @param key
     * @return
     */
    public Bitmap getBitmap(String key) {
        return mLruCache.get(key);
    }

    /**
     * 从缓存中删除指定的 Bitmap
     *
     * @param key
     */
    public void removeBitmapFromMemory(String key) {
        mLruCache.remove(key);
    }
}
