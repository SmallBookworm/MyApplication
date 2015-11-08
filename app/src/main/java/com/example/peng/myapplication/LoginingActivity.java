package com.example.peng.myapplication;

import android.app.Activity;
import android.content.EntityIterator;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.StreamHandler;

/**
 *
 * Created by wanwan on 15-6-3.
 */
public class LoginingActivity extends Activity {
    MainMyHandler handler;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logining);
        handler = new MainMyHandler(this);
        Intent intent = this.getIntent();
        String password = intent.getStringExtra("password");
        String username = intent.getStringExtra("username");
        String url = "http://eelab.hitwh.edu.cn/xk/index.asp";
        new Thread(new login(password, username, url)).start();
    }

     static class MainMyHandler extends Handler {
        WeakReference<LoginingActivity> mActivity;
        MainMyHandler(LoginingActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            LoginingActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 2:
                    Toast.makeText(theActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class login implements Runnable {
        String password;
        String username;
        String url;

        public login(String password, String username, String url) {
            this.password = password;
            this.username = username;
            this.url = url;
        }

        @Override
        public void run() {
                //请求数据
                HttpPost httpRequest = new HttpPost(url);
                httpRequest.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
                httpRequest.setHeader("Referer", "http://eelab.hitwh.edu.cn/");
                //创建参数
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("student", username));
                params.add(new BasicNameValuePair("password", password));
                try {
                    //对提交数据进行编码
                    httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    org.apache.http.client.CookieStore cookieStore = httpClient.getCookieStore();
                    httpClient.setCookieStore(cookieStore);
                    HttpResponse httpResponse = httpClient.execute(httpRequest);
                    //  获取响应服务器的数据
                   int statusCode= httpResponse.getStatusLine().getStatusCode();
                    if ( statusCode== 200) {
                        org.jsoup.nodes.Document doc= Jsoup.parse(httpResponse.getEntity().getContent(), "gb2312", "");
                       Element element= doc.select("td[align=\"center\"][background=\"/images/2index_a048.jpg\"]").first();
                        if((element.text().equals("错误的登录名和密码 用户名： 密　码："))){
                            handler.obtainMessage(2, "错误的登录名和密码").sendToTarget();
                            Intent intent = new Intent();
                            intent.setClass(LoginingActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                        httpRequest.abort();
                        Intent intent = new Intent();
                        intent.putExtra("cookies", getCookie(httpClient.getCookieStore()));
                        intent.setClass(LoginingActivity.this, UserMainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        handler.obtainMessage(2, "服务器返回信息:"+statusCode).sendToTarget();
                        Intent intent = new Intent();
                        intent.setClass(LoginingActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(2, "登录失败").sendToTarget();
                    Intent intent = new Intent();
                    intent.setClass(LoginingActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

        }

        public String[] getCookie(org.apache.http.client.CookieStore cookieStore) {
            List<Cookie> cookies = cookieStore.getCookies();
            String[] cookieMap = new String[3];
            int i = -1;
            for (Cookie cookie : cookies) {
                String key = cookie.getName();
                String value = cookie.getValue();
//                Log.e("fuck",key);
//                Log.e("fuck",value);
                cookieMap[++i] = key;
                cookieMap[++i] = value;
                cookieMap[++i]=cookie.getDomain();
            }
            return cookieMap;
        }

    }
}
