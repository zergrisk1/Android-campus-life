package com.example.hw.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.TimeZoneNames;
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

import com.example.hw.Home.Status.CommentListActivity;
import com.example.hw.Home.Status.Status;
import com.example.hw.Home.Status.StatusActivity;
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

class Notifications {
    public String id, type, title, text, status_id;
    public Date date_created;

    public Notifications(String id, String type, String title, String text, String status_id, Date date_created) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.text = text;
        this.status_id = status_id;
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getDate() {
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = spf.format(this.date_created);
        return date;
    }
}

public class NotificationsActivity extends AppCompatActivity {
    private static final String LOG_TAG = NotificationsActivity.class.getSimpleName();
    private String user_id;
    ImageView empty_tray;
    TextView empty_txt;
    ListView notifyListView;
    ProgressBar spinner;
    ArrayList<Notifications> notify_list = new ArrayList<Notifications>();

    // Resume时register receiver，会捕捉到"LIST-OBTAINED"的broadcast，用以列表获取完成时通知
    @Override
    public void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "IMAGE-DOWNLOADED".
        Log.d(LOG_TAG, "Resume");
        notifyListView.setVisibility(View.VISIBLE);
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
            int notify_count = intent.getIntExtra("notify_count", 0);
            if (notify_count == 0) {
                notifyListView.setVisibility(View.INVISIBLE);
                empty_tray.setVisibility(View.VISIBLE);
                empty_txt.setVisibility(View.VISIBLE);
            }

            spinner.setVisibility(View.GONE);
            notifyListView.setAdapter(new NotifyItemAdapter(getApplicationContext(), notify_list));
            notifyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String status_id = notify_list.get(i).status_id;
                    String type = notify_list.get(i).type;
                    if (type.equals("COMMENT")) {
                        Bundle extras = new Bundle();
                        extras.putString("status_id", status_id);
                        extras.putString("user_id", user_id);
                        Intent intent  = new Intent(getApplicationContext(), CommentListActivity.class);
                        intent.putExtras(extras);
                        startActivity(intent);
                    } else {
                        getStatusInfo(status_id);
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        notifyListView = findViewById(R.id.notify_list_view);
        empty_tray = findViewById(R.id.empty_tray_notify);
        empty_txt = findViewById(R.id.empty_txt_notify);
        spinner = findViewById(R.id.progressBar_notify);

        getNotificationsList();
    }

    private void getNotificationsList() {
        String jsonStr = "{\"user_id\":\""+ user_id + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-notifications";

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
                        boolean query_status = jObject.getBoolean("status");
                        if (query_status) {
                            JSONArray notifyArray = jObject.getJSONArray("notifications_list");
                            int count = 0;
                            for (int i = 0; i < notifyArray.length(); i++)
                            {
                                count++;
                                JSONObject notify_tmp = notifyArray.getJSONObject(i);

                                String notify_id = notify_tmp.getString("notifications_id");
                                String type = notify_tmp.getString("type");
                                String title = notify_tmp.getString("title");
                                String text = notify_tmp.getString("text");
                                String status_id = notify_tmp.getString("status_id");
                                String date_tmp = notify_tmp.getString("date_created");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                Date created_date = sdf.parse(date_tmp);

                                Notifications notifications = new Notifications(notify_id, type, title, text, status_id, created_date);
                                notify_list.add(i, notifications);
                            }
                            Intent intent = new Intent("LIST-OBTAINED");
                            intent.putExtra("notify_count", count);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        } else {
                            NotificationsActivity.this.runOnUiThread(new Runnable() {
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getStatusInfo(String status_id) {
        String jsonStr = "{\"status_id\":\""+ status_id + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "query-status";

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
                        boolean query_status = jObject.getBoolean("status");
                        if (query_status) {
                            String creator_id = jObject.getString("creator_id");
                            String creator_username = jObject.getString("creator_username");
                            String type = jObject.getString("type");
                            String title = jObject.getString("title");
                            String text = jObject.getString("text");
                            String date_tmp = jObject.getString("date_created");

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            Date created_date = sdf.parse(date_tmp);

                            int like = jObject.getInt("like");

                            Status status = new Status(status_id, creator_id, creator_username, type, title, text, created_date
                                    , like);
                            Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("status_id", status_id);
                            extras.putString("user_id", user_id);
                            extras.putString("EXTRA_TYPE", type);
                            extras.putString("EXTRA_TITLE", title);
                            extras.putString("EXTRA_TEXT", text);
                            extras.<Status>putParcelable("EXTRA_STATUS", status);
                            intent.putExtras(extras);
                            startActivity(intent);
                        } else {
                            NotificationsActivity.this.runOnUiThread(new Runnable() {
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}