package com.example.hw.Home.Status;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

public class CommentItemAdapter extends BaseAdapter {
    ArrayList<Comment> comment_list = new ArrayList<Comment>();
    private LayoutInflater layoutInflater;
    private Context context;
    String user_id,status_id;
//    private View.OnClickListener mOnClickListener;

    public CommentItemAdapter(Context aContext, ArrayList<Comment> comment_list, String userid , String statusid) {
        this.context = aContext;
        this.comment_list =  comment_list;
        this.user_id=userid;
        this.status_id=statusid;
//        this.mOnClickListener = onClickListener;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return  comment_list.size();
    }

    @Override
    public Object getItem(int i) {
        return  comment_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.comment_list_item_layout, null);
            holder = new ViewHolder();
            holder.username = (TextView) view.findViewById(R.id.comment_item_creatorname);
            holder.content = (TextView) view.findViewById(R.id.comment_item_content);
            holder.date_created = (TextView) view.findViewById(R.id.comment_item_date);
            holder.delete_txt =  (TextView) view.findViewById(R.id.delete_comment);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        Comment comment = this.comment_list.get(i);
        holder.username.setText(comment.creator_username);
        holder.content.setText(comment.content);
        holder.date_created.setText(comment.getDate());
        if(user_id.equals(comment.creator_user_id)){
            holder.delete_txt.setText("删除");
        }else{
            holder.delete_txt.setText("");
        }
        holder.delete_txt.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if(holder.delete_txt.getText().toString().equals("删除")){
                        Bundle extras = new Bundle();
                        extras.putString("status_id", status_id);
                        extras.putString("user_id",user_id);
                        extras.putString("comment_id",comment.comment_id);
                        Intent intent  = new Intent(v.getContext(), CommentDeleteActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtras(extras);
                        context.startActivity(intent);
                        }
                    }
                });
        return view;
    }

    static class ViewHolder {
        TextView username,content, date_created,delete_txt;
    }
}
