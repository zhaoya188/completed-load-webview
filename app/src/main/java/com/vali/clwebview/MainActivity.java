package com.vali.clwebview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(this);
    }

    private void initView(Context context) {
        CLWebView webView = (CLWebView) findViewById(R.id.testWebView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setLoadFinishListener(new CLWebView.LoadFinishListener() {
            @Override
            public void onLoadFinish(WebView webView) {
                Toast.makeText(MainActivity.this, "load finish",
                        Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl("https://github.com/zhaoya188/");
    }

}
