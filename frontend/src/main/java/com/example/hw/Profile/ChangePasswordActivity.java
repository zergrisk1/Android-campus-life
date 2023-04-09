package com.example.hw.Profile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hw.LoginActivity;
import com.example.hw.MainActivity;
import com.example.hw.R;
import com.example.hw.RegisterActivity;

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

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String LOG_TAG = ChangePasswordActivity.class.getSimpleName();
    private Button savePWButton;
    private EditText oldPWInput, newPWInput, newPWInput2;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        Log.d(LOG_TAG, user_id);

        oldPWInput = findViewById(R.id.oldPWinput);
        newPWInput = findViewById(R.id.newPWinput);
        newPWInput2 = findViewById(R.id.newPWinput2);

        savePWButton = findViewById(R.id.saveNewPW);
        savePWButton.setOnClickListener(this::savePW);
    }

    private void savePW(View view) {
        String old_pw = oldPWInput.getText().toString();
        String new_pw = newPWInput.getText().toString();
        String new_pw2 = newPWInput2.getText().toString();

        if (old_pw.isEmpty() || new_pw.isEmpty() || new_pw2.isEmpty()) {
            Toast.makeText(view.getContext(), "密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        if (!(new_pw.equals(new_pw2))) {
            Toast.makeText(view.getContext().getApplicationContext(), "密码不匹配", Toast.LENGTH_LONG).show();
            return;
        }

        String jsonStr = "{\"user_id\":\""+ user_id + "\",\"old_password\":\""+ old_pw + "\",\"new_password\":\""+ new_pw +"\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "change-password";

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
                            ChangePasswordActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(view.getContext(), "密码修改成功", Toast.LENGTH_LONG).show();
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                }
                            });
                        } else {
                            String message = jObject.getString("message");
                            String showToast = "";
                            if (message.equals("Password incorrect!")) {
                                showToast = "旧密码不匹配";
                            } else if (message.equals("password")) {
                                showToast = "密码违法，长度应为6-18，且含大小写和数字";
                            }
                            String finalShowToast = showToast;
                            ChangePasswordActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(view.getContext(), finalShowToast, Toast.LENGTH_LONG).show();
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