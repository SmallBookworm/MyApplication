package com.example.peng.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by peng on 15/10/30.
 */
public class UserMainActivity extends Activity {
    Button changePassword;
    Button primaryAssessment;
    Button schoolTometable;
    Button examinationResults;
    Button curriculaVariable;
    MainMyHandler handler;
    Thread ioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acrivity_usermain);
        handler = new MainMyHandler(this);
        changePassword = (Button) findViewById(R.id.changePassword);
        primaryAssessment = (Button) findViewById(R.id.primaryAssessment);
        schoolTometable = (Button) findViewById(R.id.schoolTometable);
        examinationResults = (Button) findViewById(R.id.examinationResults);
        curriculaVariable = (Button) findViewById(R.id.curricula_variable);
        final String[] s = this.getIntent().getStringArrayExtra("cookies");

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ioThread != null) {
                    handler.obtainMessage(2, "正在跳转，(*▔＾▔*)").sendToTarget();
                    return;
                }
                switch (v.getId()) {
                    case R.id.primaryAssessment:
                        ioThread = new Thread(new primaryAssessment(s[0], s[1]));
                        ioThread.start();
                        break;
                    case R.id.schoolTometable:
                        ioThread = new Thread(new form("http://eelab.hitwh.edu.cn/xk/CKKB.asp", s[0], s[1], s[2]));
                        ioThread.start();
                        break;
                    case R.id.examinationResults:
                        ioThread = new Thread(new web("http://eelab.hitwh.edu.cn/xk/CJCX.asp", s[0], s[1], s[2]));
                        ioThread.start();
                        break;
                    case R.id.curricula_variable:
                        ioThread = new Thread(new web("http://eelab.hitwh.edu.cn/xk/YKH.asp?lb=2", s[0], s[1], s[2]));
                        ioThread.start();
                        break;
                    case R.id.changePassword:
                        Intent intent = new Intent();
                        intent.putExtra("key", s[0]);
                        intent.putExtra("value", s[1]);
                        intent.setClass(UserMainActivity.this, EditpasswordActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
        changePassword.setOnClickListener(onClickListener);
        primaryAssessment.setOnClickListener(onClickListener);
        schoolTometable.setOnClickListener(onClickListener);
        examinationResults.setOnClickListener(onClickListener);
        curriculaVariable.setOnClickListener(onClickListener);
    }

    static class MainMyHandler extends Handler {
        WeakReference<UserMainActivity> mActivity;

        MainMyHandler(UserMainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UserMainActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 2:
                    Toast.makeText(theActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class primaryAssessment implements Runnable {
        public String key;
        public String value;

        public primaryAssessment(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public void run() {
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect("http://eelab.hitwh.edu.cn/xk/YKH.asp?lb=1").cookie(key, value).get();
                Elements experiment = doc.select("A.juhuang_1");
                Intent intent = new Intent();
                int length = experiment.size();
                intent.putExtra("length", length);
                Element element;
                String id;
                for (int i = 0; i < length; i++) {
                    element = experiment.get(i);
                    id = String.valueOf(i);
                    intent.putExtra("text" + id, element.text());
                    //Log.e("sb", id + element.text());
                    intent.putExtra("href" + id, element.attr("href"));
                    //Log.e("sb", id + element.attr("href"));
                }
                intent.putExtra("key", key);
                intent.putExtra("value", value);
                intent.setClass(UserMainActivity.this, PrimaryAssessmentActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
                handler.obtainMessage(2, "获取失败").sendToTarget();
            } finally {
                ioThread = null;
            }
        }
    }

    public class web implements Runnable {
        public String url;
        public String key;
        public String value;
        public String domain;

        public web(String url, String key, String value,String domain) {
            this.url = url;
            this.key = key;
            this.value = value;
            this.domain=domain;
        }

        @Override
        public void run() {
            Intent intent = new Intent();
            intent.putExtra("url", url);
            intent.putExtra("key", key);
            intent.putExtra("value", value);
            intent.putExtra("domain", domain);
            intent.setClass(UserMainActivity.this, WebActivity.class);
            startActivity(intent);
            ioThread = null;
        }
    }
    public class form implements Runnable {
        public String url;
        public String key;
        public String value;
        public String domain;

        public form(String url, String key, String value,String domain) {
            this.url = url;
            this.key = key;
            this.value = value;
            this.domain=domain;
        }

        @Override
        public void run() {
            Intent intent = new Intent();
            intent.putExtra("url", url);
            intent.putExtra("key", key);
            intent.putExtra("value", value);
            intent.putExtra("domain", domain);
            intent.setClass(UserMainActivity.this, FormActivity.class);
            startActivity(intent);
            ioThread = null;
        }
    }
}
