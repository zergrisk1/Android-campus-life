package com.example.hw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private Button loginButton, registerButton;
    private EditText emailInput, passwordInput;
    private static SharedPreferences pref;

    // 保存登录状态
    public static void saveUserInfo(String user_id) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("user_id", user_id);
        editor.commit();
    }

    // 加载登录状态
    public void loadUserInfo() {
        if (pref.contains("user_id")) {
            String user_id = pref.getString("user_id", "");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", user_id);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(this::login);
        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(this::register);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);

        pref = getSharedPreferences("User", 0);
        loadUserInfo();
    }

    public void login(View v){
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(v.getContext(), "邮箱和密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        String jsonStr = "{\"email\":\""+ email + "\",\"password\":\""+ password +"\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "login";

        try{
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr);
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {}
                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean status = jObject.getBoolean("status");
                        if (status) {
                            String user_id = jObject.getString("user_id");
                            Log.d(LOG_TAG, user_id);
                            saveUserInfo(user_id);
                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            intent.putExtra("user_id", user_id);

                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(v.getContext(), "登录成功", Toast.LENGTH_LONG).show();
                                }
                            });

                            v.getContext().startActivity(intent);
                        } else {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(v.getContext(), "邮箱或密码错误", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void register(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
    }
}