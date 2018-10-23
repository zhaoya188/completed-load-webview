package com.vali.clwebview;

import android.content.Context;
import android.util.AttributeSet;

public class TestWebView extends CLWebView {

    private static final String TAG = "TestWebView";

    public TestWebView(Context context) {
        super(context);
    }

    public TestWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public String onGetDescription() {
        return TAG;
    }

}
