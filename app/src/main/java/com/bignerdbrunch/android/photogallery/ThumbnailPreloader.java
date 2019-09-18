package com.bignerdbrunch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailPreloader extends HandlerThread {
    private static final String TAG = "ThumbnailPreloader";

    private static final int MESSAGE_PRELOAD = 1;
    private List<GalleryItem> mItems;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;

    public ThumbnailPreloader() {
        super(TAG);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_PRELOAD) {
                    String url = (String) msg.obj;
                    Log.i(TAG, "Preload request for URL: " + url);
                    handleRequest(url);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(String url) {
        Log.i(TAG, "Got a URL: " + url);
        mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url)
                    .sendToTarget();
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_PRELOAD);
    }

    private void handleRequest(String url) {
        try {
            if (ImageCache.getBitmapFromMemory(url) != null) {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                ImageCache.setBitmapToMemory(url, bitmap);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
