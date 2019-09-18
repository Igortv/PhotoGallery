package com.bignerdbrunch.android.photogallery;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageCache extends LruCache<String, Bitmap> {
    private static ImageCache mImageCache;

    public synchronized static ImageCache getInstance(int cacheSize) {
        if (mImageCache == null)
            mImageCache = new ImageCache(cacheSize);

        return mImageCache;
    }

    public ImageCache(int maxSize) {
        super(maxSize);
    }

    public static Bitmap getBitmapFromMemory(String url) {
        return mImageCache.get(url);
    }

    public static void setBitmapToMemory(String url, Bitmap bitmap) {
        if (getBitmapFromMemory(url) == null) {
            mImageCache.put(url, bitmap);
        }
    }
}
