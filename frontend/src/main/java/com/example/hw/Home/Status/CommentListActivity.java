package com.example.hw.Home.Status;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.hw.MainActivity;
import com.example.hw.Profile.OtherUserProfileActivity;
import com.example.hw.Profile.PersonalPageActivity;
import com.example.hw.Profile.StatusItemAdapter;
import com.example.hw.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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

public class CommentListActivity  extends AppCompatActivity{
    private String user_id, status_id;
    ImageView empty_tray;
    TextView empty_txt,delete_txt;
    ListView commentListView;
    ProgressBar spinner;
    Button write_comment;
    ArrayList<Comment> comment_list = new ArrayList<Comment>();

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CommentList", "onStart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        user_id = extras.getString("user_id");
        status_id = extras.getString("status_id");
        setContentView(R.layout.activity_commentlist);
        commentListView = findViewById(R.id.comment_list_view);
        empty_tray = findViewById(R.id.empty_tray_comment_list);
        empty_txt = findViewById(R.id.empty_txt_comment_list);
        spinner = findViewById(R.id.progressBar_comment_list);
        write_comment = findViewById(R.id.write_comment);
        write_comment.setOnClickListener(this::writeComment);

//        getCommentList();

    }

    // Resume时register receiver，会捕捉到"LIST-OBTAINED"的broadcast，用以列表获取完成时通知
    @Override
    public void onResume() {
        getCommentList();
        commentListView.setVisibility(View.VISIBLE);
        empty_tray.setVisibility(View.INVISIBLE);
        empty_txt.setVisibility(View.INVISIBLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("COMMENTLIST-OBTAINED"));
        super.onResume();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "LIST-OBTAINED" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            int status_count = intent.getIntExtra("comment_count", 0);
            if (status_count == 0) {
                commentListView.setVisibility(View.INVISIBLE);
                empty_tray.setVisibility(View.VISIBLE);
                empty_txt.setVisibility(View.VISIBLE);
            }

            spinner.setVisibility(View.GONE);
            commentListView.setAdapter(new CommentItemAdapter(getApplicationContext(), comment_list,user_id,status_id));
            commentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("user_id_self", user_id);
                    extras.putString("user_id_other", comment_list.get(i).creator_user_id);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });
        }
    };

    private void writeComment(View v){
        Bundle extras = new Bundle();
        extras.putString("status_id", status_id);
        extras.putString("user_id",user_id);
        Intent intent  = new Intent(v.getContext(), WriteCommentActivity.class);
        intent.putExtras(extras);
        v.getContext().startActivity(intent);
    }

    private void getCommentList(){
        String jsonStr = "{\"status_id\":\"" + status_id + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-comment";

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
                        Log.d("commentlist", jObject.toString());
                        if (query_status) {
                            comment_list.clear();
                            JSONArray commentArray = jObject.getJSONArray("comment_list");
                            int count = 0;
                            for (int i = 0; i < commentArray.length(); i++) {
                                count++;
                                JSONObject comment_tmp = commentArray.getJSONObject(i);

                                String comment_id = comment_tmp.getString("comment_id");
                                String creator_username = comment_tmp.getString("username");
                                String creator_userid = comment_tmp.getString("user_id");
                                String content = comment_tmp.getString("content");
                                String date_created = comment_tmp.getString("date_created");
                                Date created_date;

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                created_date = sdf.parse(date_created);
                                Comment comment = new Comment(comment_id,content,creator_username,creator_userid,created_date);
                                comment_list.add(i, comment);

                            }
                            Intent intent = new Intent("COMMENTLIST-OBTAINED");
                            intent.putExtra("comment_count", count);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        } else {
                            CommentListActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "获取失败", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

//class MyDButton implements View.OnClickListener {
//    String user_id,comment_id,status_id,creator_id;
//    Context context;
//
//    public MyDButton( String user_id,String comment_id,String creator_id,String status_id, Context context){
//        this.user_id=user_id;
//        this.status_id=status_id;
//        this.creator_id=creator_id;
//        this.comment_id=comment_id;
//        this.context=context;
//    }
//
//    @Override
//    public void onClick(View v) {
//        String jsonStr = "{\"user_id\":\"" + user_id + "\",\"comment_id\":\"" + comment_id
//                + "\",\"status_id\":\"" + status_id + "\"}";
//        String requestUrl = context.getResources().getString(R.string.backend_url) + "delete-comment";
//
//        try {
//            OkHttpClient client = new OkHttpClient();
//            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
//
//            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr);
//            Request request = new Request.Builder()
//                    .url(requestUrl)
//                    .post(body)
//                    .build();
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                }
//
//                @Override
//                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
//                    final String responseStr = response.body().string();
//                    try {
//                        JSONObject jObject = new JSONObject(responseStr);
//                        boolean query_status = jObject.getBoolean("status");
//                        Log.d("delete_comment", jObject.toString());
//                        if (query_status) {
//
//                        } else {
//                            Log.d("delete_comment", "false");
//                        }
//                    } catch (JSONException  e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//}
//}