package com.example.peng.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by peng on 15/10/31.
 */
public class PrimaryAssessmentActivity extends Activity {
    TextView title;
    List<CheckBox> checkBoxes;
    LinearLayout experimentLinearLayout;
    Thread thread;
    MainMyHandler handler;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primaryassessment);
        experimentLinearLayout = (LinearLayout) findViewById(R.id.experimentLinearLayout);
        title = (TextView) findViewById(R.id.primary_textView);
        handler = new MainMyHandler(this);
        checkBoxes = new ArrayList<>();
        intent = this.getIntent();
        final int length = intent.getIntExtra("length", 0);
        Button all = (Button) findViewById(R.id.primary_all);
        final Button choosen = (Button) findViewById(R.id.primary_choosen);
        if (length == 0) {
            title.setText("没有预约实验或者登录超时");
            all.setVisibility(View.GONE);
            choosen.setVisibility(View.GONE);
        } else {
            CheckBox checkBox;
            for (int i = 0; i < length; i++) {
                checkBox = new CheckBox(this);
                checkBox.setText(intent.getStringExtra("text" + String.valueOf(i)));
                experimentLinearLayout.addView(checkBox);
                checkBoxes.add(checkBox);
            }
            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (CheckBox checkBox1 : checkBoxes)
                        checkBox1.setChecked(true);
                    choosen.callOnClick();
                }
            });
            choosen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (thread == null) {
                        thread = new Thread(new primaryAssessment());
                        thread.start();
                    } else {
                        handler.obtainMessage(2, "正在挖墙脚↖(▔▽▔)↗").sendToTarget();
                    }

                }
            });
        }
    }

    static class MainMyHandler extends Handler {
        WeakReference<PrimaryAssessmentActivity> mActivity;

        MainMyHandler(PrimaryAssessmentActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PrimaryAssessmentActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    CheckBox checkBox=theActivity.checkBoxes.get(msg.arg1);
                    checkBox.append((String) msg.obj);
                    checkBox.setChecked(false);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            buttonView.setChecked(false);
                        }
                    });
                    break;
                case 2:
                    Toast.makeText(theActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class primaryAssessment implements Runnable {
        @Override
        public void run() {
            String key = intent.getStringExtra("key");
            String value = intent.getStringExtra("value");
            String answerURL = "http://eelab.hitwh.edu.cn/xk/testCJ3.asp";
            int i = 0;
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    String url = "http://eelab.hitwh.edu.cn/" + intent.getStringExtra("href" + i);
                    try {
                        org.jsoup.nodes.Document doc = Jsoup.connect(url).cookie(key, value).get();
                        Element experiment = doc.select("iframe").first();
                        if (experiment != null) {
                            Map<String, String> data = new HashMap<String, String>();
                            String src = experiment.attr("src");
                            doc = Jsoup.connect("http://eelab.hitwh.edu.cn/xk/" + src).cookie(key, value).get();
                            if(doc.body().text().equals("没有预习考核题目")){
                                handler.obtainMessage(0,i, 0, "没有预习考核题目").sendToTarget();
                                i++;
                                continue;
                            }

                            data.put("id",doc.select("input[name=id]").first().attr("value"));
                            data.put("kg_ts", doc.select("input[name=kg_ts]").first().attr("value"));
                            data.put("kg_fs",doc.select("input[name=kg_fs]").first().attr("value"));
                            data.put("tgfs", doc.select("input[name=tgfs]").first().attr("value"));
                            Elements elements = doc.select("input[name~=kg_answer\\d]");//不要匹配""
                            char a = '1';
                            for (Element element : elements) {
                                String valueA = element.attr("value");
                                data.put(element.attr("name"), valueA);
                                data.put("kg" + a, transferMD5(valueA));
                                a++;
                            }
                            Jsoup.connect(answerURL).timeout(50000).cookie(key, value).data(data).get();
                            //test
                            handler.obtainMessage(0, i, 0, "已做").sendToTarget();
//                           elements= doc.select("td.juhuang_1");
//                            for (Element element : elements) {
//                                Log.e("f", element.text());
//                            }
                        } else {
                            String erro = doc.select("td[valign=\"top\"][height=\"215\"]").first().text();
                            handler.obtainMessage(0,i, 0, erro).sendToTarget();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                i++;
            }
            thread=null;
        }

        public String transferMD5(String md5) {
            if (md5.charAt(0)=='e')//e7a70fa81a5935b71a5935b7
                return "A";
            else if (md5.charAt(0)=='f')//fe57bcca6101409561014095
                return "B";
            else if (md5.charAt(0)=='0')//0cad1d412f80b84d2f80b84d
                return "C";
            else
                return "D";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(thread!=null)
            thread.stop();
    }
}
