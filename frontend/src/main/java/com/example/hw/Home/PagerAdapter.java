package com.example.hw.Home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.Home.Status.Status;
import com.example.hw.MainActivity;
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

public class PagerAdapter extends FragmentStatePagerAdapter {
    private Context _context;
    private AppCompatActivity activity;
    private static final String LOG_TAG = PagerAdapter.class.getSimpleName();
    private ArrayList<String> type_list_all = new ArrayList<String>();
    private ArrayList<String> title_list_all = new ArrayList<String>();
    private ArrayList<String> msg_list_all = new ArrayList<String>();
    private ArrayList<String> statusid_list_all = new ArrayList<String>();
    private ArrayList<String> userid_list_all = new ArrayList<String>();

    private ArrayList<String> type_list_all_temp = new ArrayList<String>();
    private ArrayList<String> title_list_all_temp = new ArrayList<String>();
    private ArrayList<String> msg_list_all_temp = new ArrayList<String>();
    private ArrayList<String> statusid_list_all_temp = new ArrayList<String>();
    private ArrayList<String> userid_list_all_temp = new ArrayList<String>();

    private ArrayList<String> type_list_all_liked = new ArrayList<String>();
    private ArrayList<String> title_list_all_liked = new ArrayList<String>();
    private ArrayList<String> msg_list_all_liked = new ArrayList<String>();
    private ArrayList<String> statusid_list_all_liked = new ArrayList<String>();
    private ArrayList<String> userid_list_all_liked = new ArrayList<String>();

    private ArrayList<String> type_list_followed = new ArrayList<String>();
    private ArrayList<String> title_list_followed = new ArrayList<String>();
    private ArrayList<String> msg_list_followed = new ArrayList<String>();
    private ArrayList<String> statusid_list_followed = new ArrayList<String>();
    private ArrayList<String> userid_list_followed = new ArrayList<String>();

    private ArrayList<String> type_list_followed_temp = new ArrayList<String>();
    private ArrayList<String> title_list_followed_temp = new ArrayList<String>();
    private ArrayList<String> msg_list_followed_temp = new ArrayList<String>();
    private ArrayList<String> statusid_list_followed_temp = new ArrayList<String>();
    private ArrayList<String> userid_list_followed_temp = new ArrayList<String>();

    private ArrayList<String> type_list_followed_liked = new ArrayList<String>();
    private ArrayList<String> title_list_followed_liked = new ArrayList<String>();
    private ArrayList<String> msg_list_followed_liked = new ArrayList<String>();
    private ArrayList<String> statusid_list_followed_liked = new ArrayList<String>();
    private ArrayList<String> userid_list_followed_liked = new ArrayList<String>();


    ArrayList<Status> status_list_all = new ArrayList<Status>();
    ArrayList<Status> status_list_followed = new ArrayList<Status>();
    ArrayList<Status> status_list_all_temp = new ArrayList<Status>();
    ArrayList<Status> status_list_followed_temp = new ArrayList<Status>();
    ArrayList<Status> status_list_all_liked = new ArrayList<Status>();
    ArrayList<Status> status_list_followed_liked = new ArrayList<Status>();

    int mNumOfTabs;
    boolean likedsort = false;


    // 初始化时创建动态列表，连后端的话可以考虑在这里从后端获取动态列表，再保存到ArrayList当中
    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context c) {
        super(fm);
        this._context = c;
        Log.d(LOG_TAG, "Pager created");
        this.mNumOfTabs = NumOfTabs;
        likedsort = false;
        ArrayList<Status> status_list = new ArrayList<Status>();
        activity = (AppCompatActivity)_context;
        MainActivity mainActivity = (MainActivity) activity;
        String user_id = mainActivity.user_id;
        String jsonStr = "{\"user_id\":\"" + user_id + "\"," + "\"order_by_like\":\"" + "false" + "\"}";
        String requestUrl = _context.getResources().getString(R.string.backend_url) + "query-all-status";
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
                    Log.d("querystatus","fail");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        Log.d("querystatus","respone");
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean query_status = jObject.getBoolean("status");
                        Log.d("querystatus",jObject.toString());
                        if (query_status) {
                            Log.d("querystatus","true");
                            JSONArray statusArray = jObject.getJSONArray("status_list");
                            int count = 0;
                            for (int i = 0; i < statusArray.length(); i++) {
                                count++;
                                JSONObject status_tmp = statusArray.getJSONObject(i);

                                String status_id = status_tmp.getString("status_id");
                                String creator_id = status_tmp.getString("creator_id");
                                String creator_username = status_tmp.getString("creator_username");
                                String type = status_tmp.getString("type");
                                String title = status_tmp.getString("title");
                                String text = status_tmp.getString("text");
                                String date_tmp = status_tmp.getString("date_created");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                Date created_date = sdf.parse(date_tmp);

                                int like = status_tmp.getInt("like");

                                Status status = new Status(status_id, creator_id, creator_username, type, title, text, created_date
                                        , like);
                                type_list_all.add(type);
                                title_list_all.add(title);
                                msg_list_all.add(text);
                                statusid_list_all.add(status_id);
                                userid_list_all.add(creator_id);
                                status_list_all.add(i, status);

                                type_list_all_temp.add(type);
                                title_list_all_temp.add(title);
                                msg_list_all_temp.add(text);
                                statusid_list_all_temp.add(status_id);
                                userid_list_all_temp.add(creator_id);
                                status_list_all_temp.add(i, status);
                            }
                            Intent intent = new Intent("LIST-OBTAINED");
                            intent.putExtra("user_count", count);
                            LocalBroadcastManager.getInstance(_context.getApplicationContext()).sendBroadcast(intent);
                            ((Activity)_context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });
                        } else {
//                            PagerAdapter.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(_context.getApplicationContext(), "获取失败", Toast.LENGTH_LONG).show();
//                                }
//                            });
                        }
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //query followed
        Log.d("queryfollowstatususerid",user_id);
        String jsonStr1 = "{\"user_id\":\"" + user_id + "\"," + "\"order_by_like\":\"" + "false" + "\"}";
        String requestUrlf = _context.getResources().getString(R.string.backend_url) + "query-followed-status";
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr1);
            Request request = new Request.Builder()
                    .url(requestUrlf)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d("queryfollowstatus","fail");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        Log.d("queryfollowstatus","respone");
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean query_status = jObject.getBoolean("status");
                        Log.d("queryfollowstatus",jObject.toString());
                        if (query_status) {
                            Log.d("queryfollowstatus","true");
                            JSONArray statusArray = jObject.getJSONArray("status_list");
                            int count = 0;
                            for (int i = 0; i < statusArray.length(); i++) {
                                count++;
                                JSONObject status_tmp = statusArray.getJSONObject(i);

                                String status_id = status_tmp.getString("status_id");
                                String creator_id = status_tmp.getString("creator_id");
                                String creator_username = status_tmp.getString("creator_username");
                                String type = status_tmp.getString("type");
                                String title = status_tmp.getString("title");
                                String text = status_tmp.getString("text");
                                String date_tmp = status_tmp.getString("date_created");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                Date created_date = sdf.parse(date_tmp);

                                int like = status_tmp.getInt("like");

                                Status status = new Status(status_id, creator_id, creator_username, type, title, text, created_date
                                        , like);
                                status_list_followed.add(i, status);
                                type_list_followed.add(type);
                                title_list_followed.add(title);
                                msg_list_followed.add(text);
                                statusid_list_followed.add(status_id);
                                userid_list_followed.add(creator_id);

                                status_list_followed_temp.add(i, status);
                                type_list_followed_temp.add(type);
                                title_list_followed_temp.add(title);
                                msg_list_followed_temp.add(text);
                                statusid_list_followed_temp.add(status_id);
                                userid_list_followed_temp.add(creator_id);
                            }
                            ((Activity)_context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });

                        } else {

                        }
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //all liked
        String jsonStr2 = "{\"user_id\":\"" + user_id + "\"," + "\"order_by_like\":\"" + "true"+ "\"}";
        String requestUrl1 = _context.getResources().getString(R.string.backend_url) + "query-all-status";
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr2);
            Request request = new Request.Builder()
                    .url(requestUrl1)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d("querystatusliked","fail");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        Log.d("querystatusliked","respone");
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean query_status = jObject.getBoolean("status");
                        Log.d("querystatusliked",jObject.toString());
                        if (query_status) {
                            Log.d("querystatusliked","true");
                            JSONArray statusArray = jObject.getJSONArray("status_list");
                            int count = 0;
                            for (int i = 0; i < statusArray.length(); i++) {
                                count++;
                                JSONObject status_tmp = statusArray.getJSONObject(i);

                                String status_id = status_tmp.getString("status_id");
                                String creator_id = status_tmp.getString("creator_id");
                                String creator_username = status_tmp.getString("creator_username");
                                String type = status_tmp.getString("type");
                                String title = status_tmp.getString("title");
                                String text = status_tmp.getString("text");
                                String date_tmp = status_tmp.getString("date_created");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                Date created_date = sdf.parse(date_tmp);

                                int like = status_tmp.getInt("like");

                                Status status = new Status(status_id, creator_id, creator_username, type, title, text, created_date
                                        , like);
                                type_list_all_liked.add(type);
                                title_list_all_liked.add(title);
                                msg_list_all_liked.add(text);
                                statusid_list_all_liked.add(status_id);
                                userid_list_all_liked.add(creator_id);
                                status_list_all_liked.add(i, status);
                            }
//                            Intent intent = new Intent("LIST-OBTAINED");
//                            intent.putExtra("user_count", count);
//                            LocalBroadcastManager.getInstance(_context.getApplicationContext()).sendBroadcast(intent);
                            ((Activity)_context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });
                        } else {
//                            PagerAdapter.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(_context.getApplicationContext(), "获取失败", Toast.LENGTH_LONG).show();
//                                }
//                            });
                        }
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        query followed liked
        Log.d("queryfollowstatususerid",user_id);
        String jsonStr3 = "{\"user_id\":\"" + user_id + "\"," + "\"order_by_like\":\"" + "true" + "\"}";
        String requestUrlf1 = _context.getResources().getString(R.string.backend_url) + "query-followed-status";
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr3);
            Request request = new Request.Builder()
                    .url(requestUrlf1)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d("queryfollowstatus","fail");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        Log.d("queryfollowstatus","respone");
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean query_status = jObject.getBoolean("status");
                        Log.d("queryfollowstatus",jObject.toString());
                        if (query_status) {
                            Log.d("queryfollowstatus","true");
                            JSONArray statusArray = jObject.getJSONArray("status_list");
                            int count = 0;
                            for (int i = 0; i < statusArray.length(); i++) {
                                count++;
                                JSONObject status_tmp = statusArray.getJSONObject(i);

                                String status_id = status_tmp.getString("status_id");
                                String creator_id = status_tmp.getString("creator_id");
                                String creator_username = status_tmp.getString("creator_username");
                                String type = status_tmp.getString("type");
                                String title = status_tmp.getString("title");
                                String text = status_tmp.getString("text");
                                String date_tmp = status_tmp.getString("date_created");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                Date created_date = sdf.parse(date_tmp);

                                int like = status_tmp.getInt("like");

                                Status status = new Status(status_id, creator_id, creator_username, type, title, text, created_date
                                        , like);
                                status_list_followed_liked.add(i, status);
                                type_list_followed_liked.add(type);
                                title_list_followed_liked.add(title);
                                msg_list_followed_liked.add(text);
                                statusid_list_followed_liked.add(status_id);
                                userid_list_followed_liked.add(creator_id);
//                                statusid_list_followed.add(status_id);
//                                userid_list_followed.add(creator_id);
                            }
//                            Intent intent = new Intent("LIST-OBTAINED");
//                            intent.putExtra("user_count", count);
//                            LocalBroadcastManager.getInstance(_context.getApplicationContext()).sendBroadcast(intent);
                            ((Activity)_context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });
                        } else {
//                            PagerAdapter.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(_context.getApplicationContext(), "获取失败", Toast.LENGTH_LONG).show();
//                                }
//                            });
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

    // HomeFragment中调用addStatus来添加动态
    public void addStatus(String title, String msg) {
        type_list_all.add(0, "TEXT");
        title_list_all.add(0, title);
        msg_list_all.add(0, msg);
    }

    // 通过setArguments的方法，将之前获取的动态列表传给两个tab(all, followed)
    @Override
    public Fragment getItem(int position) {
        Bundle extras_all = new Bundle();
        Bundle extras_followed = new Bundle();
        extras_all.putStringArrayList("EXTRA_TYPE", type_list_all);
        extras_all.putStringArrayList("EXTRA_TITLE", title_list_all);
        extras_all.putStringArrayList("EXTRA_TEXT", msg_list_all);
        extras_all.putStringArrayList("EXTRA_STATUS_ID", statusid_list_all);
        extras_all.putStringArrayList("EXTRA_USER_ID", userid_list_all);
        extras_all.<Status>putParcelableArrayList("EXTRA_STATUS",status_list_all);
        extras_followed.putStringArrayList("EXTRA_TYPE", type_list_all);
        extras_followed.putStringArrayList("EXTRA_TITLE", title_list_followed);
        extras_followed.putStringArrayList("EXTRA_TEXT", msg_list_followed);
        extras_followed.putStringArrayList("EXTRA_STATUS_ID", statusid_list_followed);
        extras_followed.putStringArrayList("EXTRA_USER_ID", userid_list_followed);
        extras_followed.<Status>putParcelableArrayList("EXTRA_STATUS",status_list_followed);
        switch (position) {
            case 0:
                TabFragment tabFragment1 = new TabFragment();
                tabFragment1.setArguments(extras_all);
                return tabFragment1;
            case 1:
                TabFragment tabFragment2 = new TabFragment();
                tabFragment2.setArguments(extras_followed);
                return tabFragment2;
            default: return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public void sorted_by_liked(boolean reqlikesort) {
        Log.d("querystatusliked","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        if(reqlikesort){
            if(likedsort){
                return;
            }else{
                type_list_all.clear();
                title_list_all.clear();
                msg_list_all.clear();
                statusid_list_all.clear();
                userid_list_all.clear();
                type_list_followed.clear();
                title_list_followed.clear();
                msg_list_followed.clear();
                statusid_list_followed.clear();
                userid_list_followed.clear();
                status_list_all.clear();
                status_list_followed.clear();

                for(int i = 0; i < status_list_all_liked.size(); i++){
//                    Status temp = status_list_all_liked.get(i);
//                    Status status = new Status(temp.status_id, temp.creator_id, temp.creator_username, temp.type, temp.title, temp.text, temp.date_created
//                            , temp.like);
                    type_list_all.add(type_list_all_liked.get(i));
                    title_list_all.add(title_list_all_liked.get(i));
                    msg_list_all.add(msg_list_all_liked.get(i));
                    statusid_list_all.add(statusid_list_all_liked.get(i));
                    userid_list_all.add(userid_list_all_liked.get(i));
                    status_list_all.add(i, status_list_all_liked.get(i));
                }
                for(int i = 0; i < status_list_followed_liked.size(); i++){
//                    Status temp = status_list_all_liked.get(i);
//                    Status status = new Status(temp.status_id, temp.creator_id, temp.creator_username, temp.type, temp.title, temp.text, temp.date_created
//                            , temp.like);
                    type_list_followed.add(type_list_followed_liked.get(i));
                    title_list_followed.add(title_list_followed_liked.get(i));
                    msg_list_followed.add(msg_list_followed_liked.get(i));
                    statusid_list_followed.add(statusid_list_followed_liked.get(i));
                    userid_list_followed.add(userid_list_followed_liked.get(i));
                    status_list_followed.add(i, status_list_followed_liked.get(i));
                }
                likedsort=true;

            }
        }else{
            if(likedsort){
                type_list_all.clear();
                title_list_all.clear();
                msg_list_all.clear();
                statusid_list_all.clear();
                userid_list_all.clear();
                type_list_followed.clear();
                title_list_followed.clear();
                msg_list_followed.clear();
                statusid_list_followed.clear();
                userid_list_followed.clear();
                status_list_all.clear();
                status_list_followed.clear();

                for(int i = 0; i < status_list_all_temp.size(); i++){
//                    Status temp = status_list_all_liked.get(i);
//                    Status status = new Status(temp.status_id, temp.creator_id, temp.creator_username, temp.type, temp.title, temp.text, temp.date_created
//                            , temp.like);
                    type_list_all.add(type_list_all_temp.get(i));
                    title_list_all.add(title_list_all_temp.get(i));
                    msg_list_all.add(msg_list_all_temp.get(i));
                    statusid_list_all.add(statusid_list_all_temp.get(i));
                    userid_list_all.add(userid_list_all_temp.get(i));
                    status_list_all.add(i, status_list_all_temp.get(i));
                }
                for(int i = 0; i < status_list_followed_temp.size(); i++){
//                    Status temp = status_list_all_liked.get(i);
//                    Status status = new Status(temp.status_id, temp.creator_id, temp.creator_username, temp.type, temp.title, temp.text, temp.date_created
//                            , temp.like);
                    type_list_followed.add(type_list_followed_temp.get(i));
                    title_list_followed.add(title_list_followed_temp.get(i));
                    msg_list_followed.add(msg_list_followed_temp.get(i));
                    statusid_list_followed.add(statusid_list_followed_temp.get(i));
                    userid_list_followed.add(userid_list_followed_temp.get(i));
                    status_list_followed.add(i, status_list_followed_temp.get(i));
                }
                likedsort=false;
            }else{
                return;
            }
        }

//        type_list_all.clear();
//        title_list_all.clear();
//        msg_list_all.clear();
//        statusid_list_all.clear();
//        userid_list_all.clear();
//        type_list_followed.clear();
//        title_list_followed.clear();
//        msg_list_followed.clear();
//        statusid_list_followed.clear();
//        userid_list_followed.clear();
//        status_list_all.clear();
//        status_list_followed.clear();
//        activity = (AppCompatActivity)_context;
//        MainActivity mainActivity = (MainActivity) activity;
//        String user_id = mainActivity.user_id;
//        Log.d("",user_id);
//        String order_by_like = "false";
//        if(likedsort){
//            order_by_like = "true";
//        }



    }

}