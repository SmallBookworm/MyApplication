package com.example.peng.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by peng on 15/11/8.
 */
public class WebActivity extends Activity {
    TextView webView;
    Intent intent;
    MainMyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        handler = new MainMyHandler(this);
        webView = (TextView) findViewById(R.id.web);
        webView.setMovementMethod(ScrollingMovementMethod.getInstance());
        intent = getIntent();
        new Thread(new io()).start();
    }

    static class MainMyHandler extends Handler {
        WeakReference<WebActivity> mActivity;

        MainMyHandler(WebActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WebActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 1:
                    theActivity.webView.setText(msg.obj.toString());
                    break;
                case 2:
                    theActivity.webView.setText(Html.fromHtml(msg.obj.toString()));
                    break;
            }
        }
    }

    public class io implements Runnable {
        @Override
        public void run() {
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect(intent.getStringExtra("url")).cookie(intent.getStringExtra("key"), intent.getStringExtra("value")).timeout(5000).get();
                Elements elements = doc.select("TABLE.juhuang_1");
                handler.obtainMessage(2, elements.toString()).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                handler.obtainMessage(1, "获取失败").sendToTarget();
            }

        }
    }
}
