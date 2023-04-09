package com.example.hw;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();
    private Button registerButton;
    private EditText usernameInput, emailInput, passwordInput, passwordInput2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerButton = findViewById(R.id.register_button_2);
        registerButton.setOnClickListener(this::register);

        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input_register);
        passwordInput = findViewById(R.id.password_input_register);
        passwordInput2 = findViewById(R.id.password_input_register_2);
    }

    public void register(View v) {
        String username = usernameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String password2 = passwordInput2.getText().toString();

        Log.d(LOG_TAG, username + " " + email + " " + password + " " + password2);

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(v.getContext(), "用户名、邮箱和密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        if (!(password.equals(password2))) {
            Toast.makeText(v.getContext().getApplicationContext(), "密码不匹配", Toast.LENGTH_LONG).show();
            return;
        }

        String jsonStr = "{\"username\":\""+ username + "\",\"password\":\""+ password + "\",\"email\":\""+ email +"\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "register";

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
                            LoginActivity.saveUserInfo(user_id);
                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            intent.putExtra("user_id", user_id);

                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(v.getContext(), "登录成功", Toast.LENGTH_LONG).show();
                                }
                            });

                            v.getContext().startActivity(intent);
                        } else {
                            String message = jObject.getString("message");
                            String showToast = "";
                            if (message.equals("username")) {
                                showToast = "用户名违法，长度应为6-10";
                            } else if (message.equals("email")) {
                                showToast = "邮箱违法";
                            } else if (message.equals("password")) {
                                showToast = "密码违法，长度应为6-18，且含大小写和数字";
                            } else if (message.equals("Email occupied!")) {
                                showToast = "邮箱已注册，请登录";
                            }
                            String finalShowToast = showToast;
                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(v.getContext(), finalShowToast, Toast.LENGTH_LONG).show();
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
}