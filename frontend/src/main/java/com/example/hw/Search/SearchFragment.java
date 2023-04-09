package com.example.hw.Search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.Home.Status.ImageService;
import com.example.hw.Home.Status.Status;
import com.example.hw.Home.Status.StatusActivity;
import com.example.hw.MainActivity;
import com.example.hw.Profile.StatusItemAdapter;
import com.example.hw.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

public class SearchFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = SearchFragment.class.getSimpleName();
    private AppCompatActivity activity;
    private String user_id;
    private EditText searchTitle, searchText, searchCreator;
    private ImageButton searchTitleButton, searchTextButton, searchCreatorButton, searchTypeButton;
    private RadioGroup searchTypeGroup;
    private RadioButton searchTypeRadio;
    private ListView searchListView;
    private ImageView empty_tray;
    private TextView empty_txt;
    private ProgressBar spinner;
    private View view_tmp;
    ArrayList<Status> status_list = new ArrayList<Status>();
    static final int SEARCH_TITLE = 100, SEARCH_TEXT = 101, SEARCH_CREATOR = 102, SEARCH_TYPE = 103;

    public SearchFragment(){
        // require a empty public constructor
    }

    // Resume时register receiver，会捕捉到"LIST-OBTAINED"的broadcast，用以列表获取完成时通知
    @Override
    public void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "IMAGE-DOWNLOADED".
        Log.d(LOG_TAG, "Resume");
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                mMessageReceiver, new IntentFilter("LIST-OBTAINED"));
        super.onResume();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "LIST-OBTAINED" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "List obtained");
            int status_count = intent.getIntExtra("status_count", 0);
            spinner.setVisibility(View.GONE);
            if (status_count == 0) {
                searchListView.setVisibility(View.INVISIBLE);
                empty_tray.setVisibility(View.VISIBLE);
                empty_txt.setVisibility(View.VISIBLE);
                return;
            }

            searchListView.setVisibility(View.VISIBLE);
            empty_tray.setVisibility(View.INVISIBLE);
            empty_txt.setVisibility(View.INVISIBLE);
            searchListView.setAdapter(new StatusItemAdapter(activity, status_list));
            searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(activity, StatusActivity.class);
                    Bundle extras = new Bundle();
                    String status_id = status_list.get(i).status_id;
                    String type = status_list.get(i).type;
                    String title = status_list.get(i).title;
                    String text = status_list.get(i).text;
                    extras.putString("status_id", status_id);
                    extras.putString("user_id", user_id);
                    extras.putString("EXTRA_TYPE", type);
                    extras.putString("EXTRA_TITLE", title);
                    extras.putString("EXTRA_TEXT", text);
                    extras.<Status>putParcelable("EXTRA_STATUS",status_list.get(i));
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        view_tmp = v;

        // Get user_id from MainActivity
        activity = (AppCompatActivity)v.getContext();
        MainActivity mainActivity = (MainActivity) activity;
        user_id = mainActivity.user_id;

        searchTitle = (EditText) v.findViewById(R.id.searchTitle);
        searchTitleButton = (ImageButton) v.findViewById(R.id.searchTitleButton);
        searchTitleButton.setOnClickListener(this);

        searchText = (EditText) v.findViewById(R.id.searchText);
        searchTextButton = (ImageButton) v.findViewById(R.id.searchTextButton);
        searchTextButton.setOnClickListener(this);

        searchCreator = (EditText) v.findViewById(R.id.searchCreator);
        searchCreatorButton = (ImageButton) v.findViewById(R.id.searchCreatorButton);
        searchCreatorButton.setOnClickListener(this);

        searchTypeGroup = (RadioGroup) v.findViewById(R.id.searchTypeGroup);
        searchTypeButton = (ImageButton) v.findViewById(R.id.searchTypeButton);
        searchTypeButton.setOnClickListener(this);

        searchListView = (ListView) v.findViewById(R.id.searchResultList);
        empty_tray = (ImageView) v.findViewById(R.id.empty_tray_search);
        empty_txt = (TextView) v.findViewById(R.id.empty_txt_search);
        spinner = (ProgressBar) v.findViewById(R.id.progressBar_search);
        spinner.setVisibility(View.INVISIBLE);

        return v;
    }

    public void onClick(final View v) {
        switch(v.getId()) {
            case R.id.searchTitleButton:
                Log.d(LOG_TAG, "Search by title");
                search(SEARCH_TITLE);
                break;
            case R.id.searchTextButton:
                Log.d(LOG_TAG, "Search by content");
                search(SEARCH_TEXT);
                break;
            case R.id.searchCreatorButton:
                Log.d(LOG_TAG, "Search by creator");
                search(SEARCH_CREATOR);
                break;
            case R.id.searchTypeButton:
                Log.d(LOG_TAG, "Search by type");
                search(SEARCH_TYPE);
                break;
            default:
                Log.d(LOG_TAG, "No match");
                break;
        }
    }

    private void search(int search_type) {
        String search_content, jsonStr, requestUrl;
        switch(search_type) {
            case SEARCH_TITLE:
                search_content = searchTitle.getText().toString();
                jsonStr = "{\"user_id\":\""+ user_id + "\",\"title\":\""+ search_content + "\"}";
                requestUrl = getResources().getString(R.string.backend_url) + "query-status-title";
                break;
            case SEARCH_TEXT:
                search_content = searchText.getText().toString();
                jsonStr = "{\"user_id\":\""+ user_id + "\",\"text\":\""+ search_content + "\"}";
                requestUrl = getResources().getString(R.string.backend_url) + "query-status-text";
                break;
            case SEARCH_CREATOR:
                search_content = searchCreator.getText().toString();
                jsonStr = "{\"user_id\":\""+ user_id + "\",\"creator\":\""+ search_content + "\"}";
                requestUrl = getResources().getString(R.string.backend_url) + "query-status-creator";
                break;
            case SEARCH_TYPE:
                int selectedId = searchTypeGroup.getCheckedRadioButtonId();
                searchTypeRadio = (RadioButton) view_tmp.findViewById(selectedId);
                if (searchTypeRadio.getId() == R.id.searchTypeText) {
                    search_content = "TEXT";
                } else if (searchTypeRadio.getId() == R.id.searchTypeImage) {
                    search_content = "IMAGE";
                } else if (searchTypeRadio.getId() == R.id.searchTypeAudio) {
                    search_content = "AUDIO";
                } else if (searchTypeRadio.getId() == R.id.searchTypeVideo) {
                    search_content = "VIDEO";
                } else return;

                jsonStr = "{\"user_id\":\""+ user_id + "\",\"type\":\""+ search_content + "\"}";
                requestUrl = getResources().getString(R.string.backend_url) + "query-status-type";
                break;
            default:
                return;
        }

        if (search_content.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "搜索内容不能为空", Toast.LENGTH_LONG).show();
        }
        else {
            status_list.clear();
            searchListView.setAdapter(null);
            spinner.setVisibility(View.VISIBLE);

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
                            boolean queryStatus = jObject.getBoolean("status");
                            if (queryStatus) {
                                JSONArray statusArray = jObject.getJSONArray("status_list");
                                int count = 0;
                                for (int i = 0; i < statusArray.length(); i++)
                                {
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
                                    status_list.add(i, status);
                                }
                                Intent intent = new Intent("LIST-OBTAINED");
                                intent.putExtra("status_count", count);
                                LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
                            } else {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "获取失败", Toast.LENGTH_LONG).show();
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
}