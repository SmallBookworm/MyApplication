package com.example.peng.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by wanwan on 15-6-3.
 */
public class EditpasswordActivity extends Activity {
    EditText password;
    EditText rePassword;
    Button sure;
    Intent intent;
    MainMyHandler handler;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editpassword);
        handler=new MainMyHandler(this);
        sure = (Button) findViewById(R.id.editpassword_button_do);
        password = (EditText) findViewById(R.id.editpassword_editText_newpassword);
        rePassword = (EditText) findViewById(R.id.editpassword_editText_surenewpassword);
        intent = getIntent();
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thread==null) {
                    thread = new Thread(new io(password.getText().toString(), rePassword.getText().toString()));
                    thread.start();
                }else
                    Toast.makeText(EditpasswordActivity.this,"正在修改", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class MainMyHandler extends Handler {
        WeakReference<EditpasswordActivity> mActivity;

        MainMyHandler(EditpasswordActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            EditpasswordActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 1:
                    Toast.makeText(theActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class io implements Runnable {
        String password;
        String rePassword;

        public io(String password, String rePassword) {
            this.password = password;
            this.rePassword = rePassword;
        }

        @Override
        public void run() {
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect("http://eelab.hitwh.edu.cn/xk/XGMM.asp?tj=tj")
                        .data("password", password).data("repassword", rePassword)
                        .cookie(intent.getStringExtra("key"), intent.getStringExtra("value"))
                        .get();
                String erro = doc.select("td[valign=\"top\"][height=\"215\"]").first().text();
                if(erro.equals(""))
                    handler.obtainMessage(1,"密码输入错误").sendToTarget();
                else
                handler.obtainMessage(1,erro).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
thread=null;
        }
    }
}
