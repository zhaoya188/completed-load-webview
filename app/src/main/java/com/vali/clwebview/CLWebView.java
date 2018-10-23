/*
 * Copyright 2018-present Vali
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.vali.clwebview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Listen for WebView page load full completed.
 */
public abstract class CLWebView extends WebView {
    private static final String TAG = "CLWebView";

    private static final int DELAY_MS_MAX = 1000;
    private static final int DELAY_MS_DRAW = 100;

    private static final long REQUEST_ID_INIT = 0;

    public CLWebView(Context context) {
        super(context);
        init();
    }

    public CLWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CLWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * to make sure to set {@link #loadFinished} to true at last
     * in {@link #DELAY_MS_MAX} ms later.
     */
    private static final int MSG_LOAD_DATA_FINISH_DEF = 0;
    /**
     * to set {@link #loadFinished} to true in {@link #DELAY_MS_DRAW} ms later.
     */
    private static final int MSG_LOAD_DATA_FINISH_DRAW = 1;

    private String description;

    /**
     * if load data finished
     * <P><P>
     * mark the status for load API {@link #loadData(String, String, String)},
     * {@link #loadDataWithBaseURL(String, String, String, String, String)},
     * {@link #loadUrl(String)}, {@link #loadUrl(String, Map)} and {@link #reload()}
     */
    private AtomicBoolean loadFinished = new AtomicBoolean(true);

    /**
     * if progress reached 1000
     * <P><P>
     * mark the status for load API {@link #loadData(String, String, String)},
     * {@link #loadDataWithBaseURL(String, String, String, String, String)},
     * {@link #loadUrl(String)}, {@link #loadUrl(String, Map)} and {@link #reload()}
     */
    private AtomicBoolean progressFinished = new AtomicBoolean(true);

    /**
     * the request id of load data
     * <P><P>
     * mark the status for load API {@link #loadData(String, String, String)},
     * {@link #loadDataWithBaseURL(String, String, String, String, String)},
     * {@link #loadUrl(String)}, {@link #loadUrl(String, Map)} and {@link #reload()}
     */
    private AtomicLong loadTimestamp = new AtomicLong(REQUEST_ID_INIT);

    private Handler handler = new MyHandler(this);

    private LoadFinishListener listener;


    /**
     * clear status for load API {@link #loadData(String, String, String)},
     * {@link #loadDataWithBaseURL(String, String, String, String, String)},
     * {@link #loadUrl(String)}, {@link #loadUrl(String, Map)} and {@link #reload()},
     * set to false.
     * <p>
     * <b>Usage:</b> call in load API
     */
    private void clearLoadStatus() {
        Log.d(TAG, "clearLoadStatus: set false, obj=" + this.hashCode());
        loadFinished.set(false);
        progressFinished.set(false);
        // generates a new load request id
        loadTimestamp.set(System.currentTimeMillis());
    }

    private void setLoadFinished(long requestId) {
        Log.d(TAG, "setLoadFinished: id=" + requestId
                + ", loadTimestamp=" + loadTimestamp.get() + ", obj=" + this.hashCode());
        if (!progressFinished.get() || requestId != loadTimestamp.get()) {
            return;
        }
        loadDataFinished();
    }

    private void loadDataFinished() {
        Log.d(TAG, "loadFinished: set true, obj=" + this.hashCode());
        loadFinished.set(true);
        // clear load request id
        loadTimestamp.set(REQUEST_ID_INIT);
        if (listener != null) {
            listener.onLoadFinish(this);
        }
    }

    /**
     * get status for load API  {@link #loadData(String, String, String)},
     * {@link #loadDataWithBaseURL(String, String, String, String, String)},
     * {@link #loadUrl(String)}, {@link #loadUrl(String, Map)} and {@link #reload()}.
     */
    public boolean getLoadFinished() {
        return loadFinished.get();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void handleLoadFinished(int what) {
        if (loadTimestamp.get() == REQUEST_ID_INIT) {
            // there is no load data actions
            //Log.w(TAG, "handleLoadFinished: there is no load data actions, obj="
            //        + this.hashCode());
            return;
        }
        Log.d(TAG, "handleLoadFinished: obj=" + this.hashCode());
        int delay = 0;
        long requestId = loadTimestamp.get();
        if (what == MSG_LOAD_DATA_FINISH_DEF) {
            delay = DELAY_MS_MAX;
        } else if (what == MSG_LOAD_DATA_FINISH_DRAW) {
            delay = DELAY_MS_DRAW;
        }
        handler.removeMessages(what);
        handler.sendMessageDelayed(Message.obtain(handler, what, (Object) requestId), delay);
    }

    private void init() {
        this.setWebChromeClient(new DefWebChromeClient());
        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        this.description = onGetDescription();
    }

    public void setLoadFinishListener(LoadFinishListener listener) {
        this.listener = listener;
    }

    /**
     * a description of this WebView
     */
    abstract public String onGetDescription();

    @Override
    public void loadDataWithBaseURL(
            @Nullable String baseUrl, String data, @Nullable String mimeType,
            @Nullable String encoding, @Nullable String historyUrl) {
        clearLoadStatus();
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadData(String data, @Nullable String mimeType, @Nullable String encoding) {
        clearLoadStatus();
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadUrl(String url) {
        clearLoadStatus();
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        clearLoadStatus();
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    public void reload() {
        clearLoadStatus();
        super.reload();
    }

    @Override
    public void stopLoading() {
        super.stopLoading();
        loadDataFinished();
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        if (!(client instanceof DefWebChromeClient)) {
            throw new IllegalArgumentException(
                    "the WebChromeClient must be an instance of " + DefWebChromeClient.class);
        }
        super.setWebChromeClient(client);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d(TAG, "onDraw: " + this.hashCode());
        if (progressFinished.get()) {
            // set load data finished in DELAY_MS_DRAW.

            // !!! Notice that the Runnable will not be exec if this WebView is not to show,
            // and this will cause the `loadFinished` being `false` all the time,
            // if you want that executing whatever, plz use `handle.post()`.
            post(new Runnable() {
                @Override
                public void run() {
                    handleLoadFinished(MSG_LOAD_DATA_FINISH_DRAW);
                }
            });
        }
    }

    class DefWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                progressFinished.set(true);
                // set load data finished in DELAY_MS_MAX as def.

                // !!! Notice that the Runnable will not be exec if this WebView is not to show,
                // and this will cause the `loadFinished` being `false` all the time,
                // if you want that executing whatever, plz use `handle.post()`.
                post(new Runnable() {
                    @Override
                    public void run() {
                        handleLoadFinished(MSG_LOAD_DATA_FINISH_DEF);
                    }
                });
            }
            Log.d(TAG, "onProgressChanged: " + newProgress
                    + ", desc=" + description + ", obj=" + view.hashCode());
        }
    }

    static class MyHandler extends Handler {
        WeakReference<CLWebView> mViewReference;

        MyHandler(CLWebView webView) {
            mViewReference = new WeakReference<>(webView);
        }

        @Override
        public void handleMessage(Message msg) {
            final CLWebView webView = mViewReference.get();
            if (webView == null) {
                return;
            }
            // handle msg
            if (msg.what == MSG_LOAD_DATA_FINISH_DEF || msg.what == MSG_LOAD_DATA_FINISH_DRAW) {
                webView.setLoadFinished((Long) msg.obj);
            }
        }
    }

    interface LoadFinishListener {
        void onLoadFinish(WebView webView);
    }

}
