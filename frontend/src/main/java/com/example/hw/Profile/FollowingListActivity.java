package com.example.hw.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hw.Home.Status.ImageService;
import com.example.hw.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class User {
    private String user_id, username;

    public User(String user_id, String username) {
        this.user_id = user_id;
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }

    public String getUser_id() { return user_id; }
}

public class FollowingListActivity extends AppCompatActivity {
    private static final String LOG_TAG = FollowingListActivity.class.getSimpleName();
    private String user_id_self, user_id_other;
    private ImageView empty_tray;
    private TextView empty_txt;
    ProgressBar spinner;
    ListView followingList;
    ArrayList<User> following = new ArrayList<User>();

    // Resume时register receiver，会捕捉到"LIST-OBTAINED"的broadcast，用以列表获取完成时通知
    @Override
    public void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "IMAGE-DOWNLOADED".
        Log.d(LOG_TAG, "Resume");
        followingList.setVisibility(View.VISIBLE);
        empty_tray.setVisibility(View.INVISIBLE);
        empty_txt.setVisibility(View.INVISIBLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("LIST-OBTAINED"));
        super.onResume();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "LIST-OBTAINED" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "List obtained");
            int user_count = intent.getIntExtra("user_count", 0);
            if (user_count == 0) {
                followingList.setVisibility(View.INVISIBLE);
                empty_tray.setVisibility(View.VISIBLE);
                empty_txt.setVisibility(View.VISIBLE);
            }

            spinner.setVisibility(View.GONE);
            ArrayAdapter<User> arr;
            arr = new ArrayAdapter<User>(getApplicationContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, following);
            followingList.setAdapter(arr);
            followingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                    String user_id_other_2 = following.get(i).getUser_id();
                    intent.putExtra("user_id_self", user_id_self);
                    intent.putExtra("user_id_other", user_id_other_2);
                    startActivity(intent);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_list);

        Intent intent = getIntent();
        user_id_self = intent.getStringExtra("user_id_self");
        user_id_other = intent.getStringExtra("user_id_other");

        followingList = findViewById(R.id.following_list_view);
        empty_tray = findViewById(R.id.empty_tray);
        empty_txt = findViewById(R.id.empty_txt);
        spinner = findViewById(R.id.progressBar_following);

        getFollowingList();

    }

    private void getFollowingList() {
        String jsonStr = "{\"user_id\":\""+ user_id_other + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-following";

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
                            JSONArray userArray = jObject.getJSONArray("following");
                            int count = 0;
                            for (int i = 0; i < userArray.length(); i++)
                            {
                                count++;
                                JSONObject user_tmp = userArray.getJSONObject(i);

                                String user_id_tmp = user_tmp.getString("user_id");
                                String username_tmp = user_tmp.getString("username");

                                User user = new User(user_id_tmp, username_tmp);
                                following.add(i, user);
                            }
                            Intent intent = new Intent("LIST-OBTAINED");
                            intent.putExtra("user_count", count);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        } else {
                            FollowingListActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "获取失败", Toast.LENGTH_LONG).show();
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