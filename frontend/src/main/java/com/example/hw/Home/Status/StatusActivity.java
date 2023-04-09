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
        import android.media.MediaPlayer;
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
        import android.widget.MediaController;
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

public class StatusActivity extends AppCompatActivity {
    private static final String LOG_TAG = StatusActivity.class.getSimpleName();
    private TextView titleView, msgView, urlText, mapText, creatornameView;
    private ImageButton shareButton, backButton,commentButton;
    private ImageView imageView,LikeListButton;
    private Button followButton;
    private String user_id, status_id;
    private Status status;
    private Switch likeSwitch;
    MediaController mediaController;
    private VideoView videoview;
    private ArrayList<String> like_username_list = new ArrayList<String>();
    private ArrayList<String> like_user_id_list = new ArrayList<String>();
    private MediaPlayer mediaPlayer;

    // For audio
    boolean isPlay_audio = false;
    boolean isPause_audio = false;

    boolean firstliked = false;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    // 保存状态，实际不需要
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("getTitle", titleView.getText().toString());
        outState.putString("getMessage", msgView.getText().toString());
    }

    // Pause时unregister receiver，用以图片下载完成时通知
    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    // Resume时register receiver，会捕捉到"IMAGE-DOWNLOADED"的broadcast，用以图片下载完成时通知
    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "IMAGE-DOWNLOADED".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("IMAGE-DOWNLOADED"));
        registerReceiver(onCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        super.onResume();
    }

    // 图片下载完成时，将本地存储的图片添加到imageView中
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "IMAGE-DOWNLOADED" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("IMAGE-DOWNLOADED")) {
                File imgFile = new File(getResources().getString(R.string.image_loc) + status.media);
                imageView.setImageURI(Uri.fromFile(imgFile));
            }
        }
    };

    private BroadcastReceiver onCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(LOG_TAG, "VIDEO");
            Toast.makeText(getApplicationContext(), "下载成功", Toast.LENGTH_LONG).show();

            if(status.type.equals("IMAGE"))
            {
                Log.d(LOG_TAG, "onReceive: "+status.type);
                File imgFile = new File(getResources().getString(R.string.image_loc) + status.media);
                Log.d(LOG_TAG, "onReceive: "+imgFile.getPath());
                imageView.setImageURI(Uri.fromFile(imgFile));
            }else if(status.type.equals("AUDIO"))
            {
                File file = new File(getResources().getString(R.string.music_loc) + status.media);
                videoview = (VideoView)findViewById(R.id.videoview2);
                videoview.setVideoPath(file.getPath());
//                File file = new File(getResources().getString(R.string.music_loc) + status.media);
//                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(file.getPath()));
//                mediaPlayer.start();
            }else if(status.type.equals("VIDEO")){
                File file = new File(getResources().getString(R.string.video_loc) + status.media);
                videoview = (VideoView)findViewById(R.id.videoview);
                videoview.setVideoPath(file.getPath());
            }

//            mediaController.show();
            //videoview.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        status = extras.<Status>getParcelable("EXTRA_STATUS");
        status_id = extras.getString("status_id");
        user_id = extras.getString("user_id");
        String type = extras.getString("EXTRA_TYPE");
        String title = extras.getString("EXTRA_TITLE");
        String msg = extras.getString("EXTRA_TEXT");
        getStatusInfo(status.type);
        // 通过intent传入的type判断需要执行什么
        // 3 Types of contents
        if (type.equals("AUDIO")) {
            Log.d(LOG_TAG, "Music");
            setContentView(R.layout.activity_status_music);
        } else if (type.equals("VIDEO")) {
            Log.d(LOG_TAG, "Video");
            setContentView(R.layout.activity_status_video);
            MainActivity.verifyStoragePermissions(this);
        } else {
            setContentView(R.layout.activity_status);
            imageView = findViewById(R.id.image);
            Log.d(LOG_TAG, status_id);

        }
        creatornameView = findViewById(R.id.creatorName);
        creatornameView.setText(status.creator_username);
        creatornameView.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                Bundle extras = new Bundle();
                extras.putString("user_id_self", user_id);
                extras.putString("user_id_other", status.creator_id);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
        followButton = findViewById(R.id.followed);
        //chaxun followed
        followButton.setOnClickListener(this::followCreator);
        Log.d("userid", user_id);
        Log.d("creatorid", status.creator_id);
        if (user_id.equals(status.creator_id)) {
            followButton.setText("(自己)");
        } else {
            String jsonStr = "{\"user_id\":\"" + user_id + "\"," + "\"user_id_followed\":\"" + status.creator_id + "\"}";
            String requestUrl = getResources().getString(R.string.backend_url) + "check-follow";
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
                        Log.d("chechfollow", "fail");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                        final String responseStr = response.body().string();
                        try {
                            Log.d("chechfollow", "respone");
                            JSONObject jObject = new JSONObject(responseStr);
                            boolean res_status = jObject.getBoolean("status");
                            Log.d("chechfollow", jObject.toString());
                            if (res_status) {
                                Log.d("chechfollow", "true");
                                boolean followed = jObject.getBoolean("following");
                                if (followed) {
                                    StatusActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            followButton.setText("已关注");
                                        }
                                    });
                                }
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
        likeSwitch = findViewById(R.id.likeSwitch);
        LikeListButton = findViewById(R.id.LikeListButton);
        LikeListButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LikeListActivity.class);
                Bundle extras = new Bundle();
                extras.putStringArrayList("user_id_list", like_user_id_list);
                extras.putStringArrayList("username_list", like_username_list);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
        likeSwitch.setText(Integer.toString(status.like));
        String jsonStr = "{\"user_id\":\"" + user_id + "\"," + "\"status_id\":\"" + status.status_id + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-like";
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
                    Log.d("chechliked", "fail");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        Log.d("chechliked", "respone");
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean res_status = jObject.getBoolean("status");
                        Log.d("chechliked", jObject.toString());
                        if (res_status) {
                            boolean ifliked = jObject.getBoolean("liked");
                            StatusActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    firstliked = ifliked;
                                    if (ifliked) {
                                        likeSwitch.setChecked(true);
                                    } else {
                                        likeSwitch.setChecked(false);
                                    }
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

        likeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (firstliked) {
                    Log.d("like-unlike", "firstlike!!!");
                    firstliked = false;
                    return;
                }
                String jsonStr = "{\"user_id\":\"" + user_id + "\"," + "\"status_id\":\"" + status.status_id + "\"}";
                String requestUrl = getResources().getString(R.string.backend_url) + "like-unlike";
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
                            Log.d("like-unlike", "fail");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                            final String responseStr = response.body().string();
                            try {
                                JSONObject jObject = new JSONObject(responseStr);
                                boolean res_status = jObject.getBoolean("status");
                                Log.d("like-unlike", jObject.toString());
                                if (res_status) {
                                    StatusActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            liked_unliked(isChecked, true);
                                        }
                                    });
                                } else {
                                    StatusActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            liked_unliked(isChecked, false);
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
        });
        commentButton = findViewById(R.id.commentButton);
        commentButton.setOnClickListener(this::clickComment);
        titleView = findViewById(R.id.titleView2);
        titleView.setText(title);

        msgView = findViewById(R.id.msgView);
        msgView.setText(msg);

        urlText = findViewById(R.id.url);
        urlText.setText(getResources().getString(R.string.text_url));
        urlText.setOnClickListener(this::clickURL);

        mapText = findViewById(R.id.map);
        mapText.setText("");
        mapText.setOnClickListener(this::clickMap);

        shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(this::shareText);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this::goBack);

        if (savedInstanceState != null) {
            titleView.setText(savedInstanceState.getString("getTitle"));
            msgView.setText(savedInstanceState.getString("getMessage"));
        }
    }
    public void clickComment(View v){
        Bundle extras = new Bundle();
        extras.putString("status_id", status.status_id);
        extras.putString("user_id",user_id);
        Intent intent  = new Intent(v.getContext(), CommentListActivity.class);
        intent.putExtras(extras);
        v.getContext().startActivity(intent);
    }

    public void liked_unliked(boolean isChecked, boolean ls) {
        int like_count = Integer.parseInt(String.valueOf(likeSwitch.getText()));
        if (isChecked) {
            //选中状态
            if (ls) {
                Log.d("like-unlike", "LikeSuuss1");
                like_count = like_count + 1;
                likeSwitch.setText(Integer.toString(like_count));
            } else {
                likeSwitch.setChecked(false);
            }
        } else {
            //未选中状态
            if (ls) {
                Log.d("like-unlike", "LikeSuuss2");
                like_count = like_count - 1;
                likeSwitch.setText(Integer.toString(like_count));
            } else {
                likeSwitch.setChecked(true);
            }
        }
    }

    public void followCreator(View view) {
        String followedstr = followButton.getText().toString();
        if (followedstr.equals("(自己)")) {
            return;
        }
        String jsonStr = "{\"user_id\":\"" + user_id + "\"," + "\"user_id_followed\":\"" + status.creator_id + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "follow-unfollow";
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
                    Log.d("follow", "fail");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean res_status = jObject.getBoolean("status");
                        Log.d("follow", jObject.toString());
                        if (res_status) {
                            StatusActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (followedstr.equals("关注")) {
                                        followButton.setText("已关注");
                                    } else {
                                        followButton.setText("关注");
                                    }
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

    private void isVideo(){
        mediaController = new MediaController(this);
        videoview = (VideoView)findViewById(R.id.videoview);
        mediaController.setAnchorView(videoview);
        videoview.setMediaController(mediaController);
        //mediaController.show();
        File file = new File(getResources().getString(R.string.video_loc) + status.media);
        if (file.exists()) {
            Log.d(LOG_TAG,"video already exists");
            videoview.setVideoPath(file.getPath());
            videoview.start();
            //mediaController.show();
        } else {
            //downloadVideo(status.media);
            Intent intent = new Intent(getBaseContext(), VideoService.class);
            intent.putExtra("type", "status");
            intent.putExtra("name", status.media);
            startService(intent);
        }
    }
    private void isMusic(){
        mediaController = new MediaController(this);
        videoview = (VideoView)findViewById(R.id.videoview2);
        mediaController.setAnchorView(videoview);
        videoview.setMediaController(mediaController);
        //mediaController.show();
        File file = new File(getResources().getString(R.string.music_loc) + status.media);
        if (file.exists()) {
            Log.d(LOG_TAG,"video already exists");
            videoview.setVideoPath(file.getPath());
            videoview.start();
            //mediaController.show();
        } else {
            //downloadVideo(status.media);
            Intent intent = new Intent(getBaseContext(), MusicService.class);
            intent.putExtra("type", "status");
            intent.putExtra("name", status.media);
            startService(intent);
        }
//        File file = new File(getResources().getString(R.string.video_loc) + status.media);
//        if (file.exists()) {
//            Log.d(LOG_TAG, "video already exists");
//            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(status.media));
//            mediaPlayer.start();
//            //mediaController.show();
//        } else {
//            //downloadVideo(status.media);
//            Intent intent = new Intent(getBaseContext(), MusicService.class);
//            intent.putExtra("type", "status");
//            intent.putExtra("name", status.media);
//            startService(intent);
//        }
    }
    private void isImage(){
        File file = new File(getResources().getString(R.string.image_loc) + status.media);
        // If image does not exist, start a service to download
        if (file.exists()) {
            imageView.setImageURI(Uri.fromFile(file));
        } else {
            Intent imgIntent = new Intent(getBaseContext(), ImageService.class);
            imgIntent.putExtra("image_type", "status");
            imgIntent.putExtra("image_name", status.media);
            startService(imgIntent);
        }
    }
    private void getStatusInfo(String type) {
        Log.d("id=", status_id);
        String jsonStr = "{\"status_id\":\"" + status_id + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-status";

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
                        Log.d("ss", jObject.toString());
                        if (query_status) {
                            Log.d("d", "s");
                            status.media = jObject.getString("media");
                            status.location = jObject.getString("location");
                            status.like = jObject.getInt("like");
                            Log.d(LOG_TAG, status.location);
                            Log.d("media_image", status.media);
                            like_user_id_list.clear();
                            like_username_list.clear();
                            JSONArray LikedArray = jObject.getJSONArray("like_users");
                            for (int i = 0; i < LikedArray.length(); i++) {
                                JSONObject liked_tmp = LikedArray.getJSONObject(i);

                                String user_id = liked_tmp.getString("user_id");
                                String username = liked_tmp.getString("username");
                                like_username_list.add(i, username);
                                like_user_id_list.add(i, user_id);
                            }

                            StatusActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(type.equals("TEXT")){
                                        isImage();
                                    }else if(type.equals("IMAGE")){
                                        isImage();
                                    }else if(type.equals("AUDIO")){
                                        isMusic();
                                    }else if(type.equals("VIDEO")){
                                        isVideo();
                                    }
                                    mapText.setText(status.location);

                                }
                            });
                        } else {
                            StatusActivity.this.runOnUiThread(new Runnable() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startMusicPlayer(View view) {
        MainActivity.verifyStoragePermissions(this);
        Intent intent = new Intent(getBaseContext(), MusicService.class);
        if (!isPlay_audio) {
            intent.putExtra("isPlay", true);
            startService(intent);
        } else {
            stopService(intent);
        }
        isPlay_audio = !isPlay_audio;
    }

    public void pauseMusicPlayer(View view) {
        MainActivity.verifyStoragePermissions(this);
        Intent intent = new Intent(getBaseContext(), MusicService.class);
        if (!isPause_audio) {
            intent.putExtra("isPlay", true);
            intent.putExtra("isPause", true);
            startService(intent);
        } else {
            intent.putExtra("isPause", false);
            startService(intent);
        }
        isPause_audio = !isPause_audio;
    }
    public void clickURL(View view) {
        // Get the URL text.
        String url = urlText.getText().toString();
        Log.d(LOG_TAG, url);

        // Parse the URI and create the intent.
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        // Find an activity to hand the intent and start that activity.
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d("ImplicitIntents", "Can't handle this!");
        }
    }

    public void clickMap(View view) {
        // Get the string indicating a location. Input is not validated; it is
        // passed to the location handler intact.
        String loc = mapText.getText().toString();
        Log.d(LOG_TAG, loc);

        // Parse the location and create the intent.
        Uri addressUri = Uri.parse("geo:0,0?q=" + loc);
        Intent intent = new Intent(Intent.ACTION_VIEW, addressUri);

        startActivity(intent);
    }

    public void shareText(View view) {
        String txt = msgView.getText().toString();
        Log.d(LOG_TAG, txt);
        String mimeType = "text/plain";
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(R.string.share_text_with)
                .setText(txt)
                .startChooser();
    }
    public void goBack(View view) {
        finish();
        super.onBackPressed();
    }
}