package com.example.hw.Home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TabFragment extends Fragment {
    private static final String LOG_TAG = TabFragment.class.getSimpleName();
    private static final String TAG = TabFragment.class.getSimpleName();
    private final LinkedList<String> statusTypeAll = new LinkedList<>();
    private final LinkedList<String> statusTitleAll = new LinkedList<>();
    private final LinkedList<String> statusMsgAll = new LinkedList<>();
    private final LinkedList<String> statusidAll = new LinkedList<>();
    private final LinkedList<String> statususeridAll = new LinkedList<>();
    private final LinkedList<Status> statusAll = new LinkedList<>();
    private ArrayList<String> type_list_all, title_list_all, msg_list_all,statusid_list_all,userid_list_all;
    private ArrayList<Status> status_list_all;
    private RecyclerView status_all;
    private WordListAdapter mAdapter;
    private Button load_button;
    private Context context;
    private View v;
    public TabFragment(){
        // require a empty public constructor
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: BroadCast Catched");
           //mAdapter = new WordListAdapter(getContext(), statusTypeAll, statusTitleAll, statusMsgAll,statusidAll,statususeridAll,statusAll);
//            status_all.setAdapter(mAdapter);
//            LinearLayoutManager llm = new LinearLayoutManager((getContext()));
//            status_all.setLayoutManager(llm);
//            loadMore(v);
        }
    };

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        mAdapter = new WordListAdapter(getContext(), statusTypeAll, statusTitleAll, statusMsgAll,statusidAll,statususeridAll,statusAll);
        status_all.setAdapter(mAdapter);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                mMessageReceiver, new IntentFilter("STATUSLIST-OBTAINED"));
        super.onResume();
    }
    // 通过getArguments从PagerAdapter获取动态列表，此处AUDIO和VIDEO只是为了测试音频和视频而写死两个动态
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_tab, container, false);
        context = container.getContext();
        statusTypeAll.clear();
        statusTitleAll.clear();
        statusMsgAll.clear();
        statusidAll.clear();
        statususeridAll.clear();
        statusAll.clear();

        Bundle extras = this.getArguments();
        if (extras != null) {
            type_list_all = extras.getStringArrayList("EXTRA_TYPE");
            title_list_all = extras.getStringArrayList("EXTRA_TITLE");
            msg_list_all = extras.getStringArrayList("EXTRA_TEXT");
            statusid_list_all = extras.getStringArrayList("EXTRA_STATUS_ID");
            userid_list_all = extras.getStringArrayList("EXTRA_USER_ID");
            status_list_all = extras.<Status>getParcelableArrayList("EXTRA_STATUS");
        }


        load_button = v.findViewById(R.id.load_button);
        load_button.setOnClickListener(this::loadMore);



        status_all = v.findViewById(R.id.recycle_all);
        mAdapter = new WordListAdapter(getContext(), statusTypeAll, statusTitleAll, statusMsgAll,statusidAll,statususeridAll,statusAll);
        status_all.setAdapter(mAdapter);
        LinearLayoutManager llm = new LinearLayoutManager((getContext()));
        status_all.setLayoutManager(llm);
        loadMore(v);
        // 划到最底时会调用loadMore显示更多动态
        status_all.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (llm.findLastCompletelyVisibleItemPosition() >= statusTitleAll.size()-3) {
                    loadMore(v);
                }
            }
        });
        return v;
    }

    // 当点击“加载更多”按钮或划到最底时调用loadMore显示更多动态
    private void loadMore(View view) {
        int len = statusTitleAll.size();
        int count = 0;
        while (len + count < title_list_all.size()) {
            statusTypeAll.addLast(type_list_all.get(len+count));
            statusTitleAll.addLast(title_list_all.get(len+count));
            statusMsgAll.addLast(msg_list_all.get(len+count));
            statusidAll.addLast(statusid_list_all.get(len+count));
            statususeridAll.addLast(userid_list_all.get(len+count));
            statusAll.addLast(status_list_all.get(len+count));
            count++;
            if (count > 9) break;
        }
        status_all.getAdapter().notifyItemInserted(len);
//        if (count == 0) {
//            Toast.makeText(getActivity().getApplicationContext(), "已没有更多动态", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getActivity().getApplicationContext(), "已加载更多动态", Toast.LENGTH_SHORT).show();
//        }
    }
    // 当点击“重新加载”按钮
    private void reload(View view) {
        status_all = view.findViewById(R.id.recycle_all);
        mAdapter = new WordListAdapter(getContext(), statusTypeAll, statusTitleAll, statusMsgAll,statusidAll,statususeridAll,statusAll);
        status_all.setAdapter(mAdapter);
        LinearLayoutManager llm = new LinearLayoutManager((getContext()));
        status_all.setLayoutManager(llm);

        loadMore(view);
    }
}