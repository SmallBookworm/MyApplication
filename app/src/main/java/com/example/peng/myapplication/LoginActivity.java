package com.example.peng.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 *登录超时未处理
 * Created by wanwan on 15-6-2.
 */
public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences mySharedPreferences;
        final EditText login_editText_identifyingcode= (EditText) findViewById(R.id.login_editText_identifyingcode);
        final EditText login_editText_user= (EditText) findViewById(R.id.login_editText_user);
        Button loginButton= (Button) findViewById(R.id.login_button_login);
        /**提取sharedPreferences储存数据*/
        mySharedPreferences = getSharedPreferences("user", Activity.MODE_PRIVATE);
        String burl = mySharedPreferences.getString("username", "");
        if (!burl.equals("")) {
            login_editText_user.setText(burl);
        }
        burl = mySharedPreferences.getString("password", "");
        if (!burl.equals("")) {
            login_editText_identifyingcode.setText(burl);
        }

        /**登录**/
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username= String.valueOf(login_editText_user.getText());
                String password= String.valueOf(login_editText_identifyingcode.getText());
                int a=0;
                if (username.equals("")){
                    Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    a++;
                }
                if (password.equals("")){
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    a++;
                }
                if(a==0) {
//                sharedPreferences储存
                    mySharedPreferences.edit().putString("username", String.valueOf(login_editText_user.getText())).apply();
                    mySharedPreferences.edit().putString("password", String.valueOf(login_editText_identifyingcode.getText())).apply();
                    //跳转
                    Intent intent = new Intent();
                    intent.putExtra("username",username);
                    intent.putExtra("password",password);
                    intent.setClass(LoginActivity.this,LoginingActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }
}

