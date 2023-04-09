package com.example.hw.Home;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hw.Home.Status.Status;
import com.example.hw.MainActivity;
import com.example.hw.R;
import com.example.hw.Home.Status.StatusActivity;

import java.util.LinkedList;

public class WordListAdapter extends
        RecyclerView.Adapter<WordListAdapter.WordViewHolder> {

    private static final String LOG_TAG = WordListAdapter.class.getSimpleName();
    private final LinkedList<String> mTypeList;
    private final LinkedList<String> mWordList;
    private final LinkedList<String> mContentList;
    private final LinkedList<String> mStatusidList;
    private final LinkedList<String> mUseridList;
    private final LinkedList<Status> mStatusList;
    private final LayoutInflater mInflater;
    private AppCompatActivity activity;
    private Context context;

    class WordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView wordItemView;
        public final TextView dateItemView;
        public final TextView creatorItemView;
        public final TextView textItemView;
        final WordListAdapter mAdapter;

        public WordViewHolder(View itemView, WordListAdapter adapter) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.word);
            dateItemView = itemView.findViewById(R.id.status_item_date);
            creatorItemView = itemView.findViewById(R.id.status_item_creator);
            textItemView = itemView.findViewById(R.id.status_item_text);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();

            // Use that to access the affected item in mWordList.
            String element = mWordList.get(mPosition);
            // Change the word in the mWordList.

            mAdapter.notifyDataSetChanged();
        }
    }

    // 初始化时读入动态列表
    public WordListAdapter(Context c, LinkedList<String> typeList, LinkedList<String> wordList, LinkedList<String> contentList,LinkedList<String> statusidList,LinkedList<String> useridList,LinkedList<Status> statusList) {
        mInflater = LayoutInflater.from(c);
        this.mTypeList = typeList;
        this.mWordList = wordList;
        this.mContentList = contentList;
        this.mStatusidList = statusidList;
        this.mUseridList = useridList;
        this.mStatusList = statusList;
        this.context = c;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.wordlist_item, parent, false);
        return new WordViewHolder(mItemView, this);
    }

    // 当点击时启动“动态详情”的Activity
    @Override
    public void onBindViewHolder(WordViewHolder holder,
                                 int position) {
        // Retrieve the data for that position.
        String type = mTypeList.get(position);
        String title = mWordList.get(position);
        String msg = mContentList.get(position);
        String statusid = mStatusidList.get(position);
//        String userid = mUseridList.get(position);
        Status status = mStatusList.get(position);
        // Add the data to the view holder.
        holder.wordItemView.setText(status.title);
        holder.dateItemView.setText(status.date_created.toString());
//        holder.dateItemView.setText("asdasdsadadsds");
        holder.creatorItemView.setText(status.creator_username);
        holder.textItemView.setText(status.text);
        holder.wordItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity = (AppCompatActivity)v.getContext();
                Log.d(LOG_TAG, title);
                Bundle extras = new Bundle();

                // This part NEEDS to be change
                extras.<Status>putParcelable("EXTRA_STATUS",status);
                extras.putString("status_id", statusid);
//                extras.putString("user_id", userid);
                activity = (AppCompatActivity)context;
                MainActivity mainActivity = (MainActivity) activity;
                String user_id = mainActivity.user_id;
                extras.putString("user_id",user_id);

                extras.putString("EXTRA_TYPE", type);
                extras.putString("EXTRA_TITLE", title);
                extras.putString("EXTRA_TEXT", msg);
                Intent intent  = new Intent(v.getContext(), StatusActivity.class);
                intent.putExtras(extras);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWordList.size();
    }
}
