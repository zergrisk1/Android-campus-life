package com.example.hw.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.Home.Status.ImageService;
import com.example.hw.R;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OtherUserProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = OtherUserProfileActivity.class.getSimpleName();
    TextView username, email, description, following_list, personal_page;
    ImageView profile_pic;
    Button follow_unfollow, block_unblock;
    String user_id_self, user_id_other, profile_pic_user, username_user, desc_user;

    @Override
    public void onResume() {
        registerReceiver(onCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        super.onResume();
    }

    private BroadcastReceiver onCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "下载成功", Toast.LENGTH_LONG).show();

            File imgFile = new File(getResources().getString(R.string.image_loc) + profile_pic_user);
            profile_pic.setImageURI(Uri.fromFile(imgFile));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        Intent intent = getIntent();
        user_id_self = intent.getStringExtra("user_id_self");
        user_id_other = intent.getStringExtra("user_id_other");

        username = findViewById(R.id.username_other);
        email = findViewById(R.id.email_other);
        description = findViewById(R.id.profileDesc_other);
        profile_pic = findViewById(R.id.profilePic_other);

        following_list = findViewById(R.id.followingListTxt_other);
        following_list.setClickable(true);
        following_list.setOnClickListener(this::jumpFollowingList);

        personal_page = findViewById(R.id.personalPageTxt_other);
        personal_page.setClickable(true);
        personal_page.setOnClickListener(this::jumpPersonalPage);

        follow_unfollow = findViewById(R.id.follow_unfollow);
        follow_unfollow.setOnClickListener(this::follow_unfollow);

        block_unblock = findViewById(R.id.block_unblock);
        block_unblock.setOnClickListener(this::block_unblock);

        getUserInfo();
    }

    private void jumpFollowingList(View v) {
        Log.d(LOG_TAG, "Following list");

        Intent intent_following = new Intent(this, FollowingListActivity.class);
        intent_following.putExtra("user_id_self", this.user_id_self);
        intent_following.putExtra("user_id_other", this.user_id_other);
        startActivity(intent_following);
    }

    private void jumpPersonalPage(View v) {
        Log.d(LOG_TAG, "PersonalPage");

        Intent intent_following = new Intent(this, PersonalPageActivity.class);
        intent_following.putExtra("user_id_self", this.user_id_self);
        intent_following.putExtra("user_id_other", this.user_id_other);
        startActivity(intent_following);
    }

    private void getUserInfo() {
        String jsonStr = "{\"user_id\":\""+ user_id_other + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-userinfo";

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
                            username_user = jObject.getString("username");
                            String email_user = jObject.getString("email");
                            desc_user = jObject.getString("description");
                            profile_pic_user = jObject.getString("profile_photo");
                            Log.d(LOG_TAG, profile_pic_user);


                            OtherUserProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    username.setText(username_user);
                                    email.setText(email_user);

                                    if (!desc_user.equals("null")) description.setText(desc_user);

                                    // Download image, start ImageService to download pic if image does not already exist
                                    if (!profile_pic_user.equals("null")) {
                                        File imgFile = new File(getResources().getString(R.string.image_loc) + profile_pic_user);
                                        if (imgFile.exists()) {
                                            profile_pic.setImageURI(Uri.fromFile(imgFile));
                                        } else {
                                            Intent imgIntent = new Intent(getApplicationContext(), ImageService.class);
                                            imgIntent.putExtra("image_type", "profile");
                                            imgIntent.putExtra("image_name", profile_pic_user);
                                            startService(imgIntent);
                                        }
                                    }

                                    Toast.makeText(getApplicationContext(), "获取成功", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            OtherUserProfileActivity.this.runOnUiThread(new Runnable() {
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

    private void follow_unfollow(View view) {
        String jsonStr = "{\"user_id\":\""+ user_id_self + "\",\"user_id_followed\":\""+ user_id_other + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "follow-unfollow";

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
                            OtherUserProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "关注/取关成功", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            OtherUserProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "关注/取关失败", Toast.LENGTH_LONG).show();
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

    private void block_unblock(View view) {
        String jsonStr = "{\"user_id\":\""+ user_id_self + "\",\"user_id_blocked\":\""+ user_id_other + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "block-unblock";

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
                            OtherUserProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "屏蔽/解除屏蔽成功", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            OtherUserProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "屏蔽/解除屏蔽失败", Toast.LENGTH_LONG).show();
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