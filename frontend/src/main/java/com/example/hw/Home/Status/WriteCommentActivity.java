package com.example.hw.Home.Status;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WriteCommentActivity  extends AppCompatActivity {
    private String user_id, status_id;
    EditText edit_txt;
    ImageButton post_comment;


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("PostComment", "onStart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PostComment", "onCreate!!!!!!!!!!!!!!!!!!!");
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        user_id = extras.getString("user_id");
        status_id = extras.getString("status_id");
        setContentView(R.layout.activity_write_comment);
        edit_txt = findViewById(R.id.comment_content_edittext);
        post_comment = (ImageButton) findViewById(R.id.postCommentButton);
        post_comment.setOnClickListener((new View.OnClickListener() {
            public void onClick(View v) {
                postComment(v);
            }
        }));

    }

    private void postComment(View view){
        String comment = edit_txt.getText().toString();
        if (comment.isEmpty()) {
            Toast.makeText(getApplicationContext(), "评论内容不能为空", Toast.LENGTH_LONG).show();
        }else{
            String jsonStr = "{\"user_id\":\"" + user_id + "\",\"status_id\":\"" + status_id
                    + "\",\"content\":\"" + comment + "\"}";
            String requestUrl = getResources().getString(R.string.backend_url) + "add-comment";

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

                @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr);
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                        final String responseStr = response.body().string();
                        try {
                            JSONObject jObject = new JSONObject(responseStr);
                            boolean query_status = jObject.getBoolean("status");
                            if (query_status) {
                                Log.d("Postcomment", jObject.toString());
                                finish();
                            } else {
                                WriteCommentActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "发布失败", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
