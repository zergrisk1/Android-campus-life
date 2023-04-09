package com.example.hw.Profile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hw.Home.Status.Status;
import com.example.hw.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class StatusItemAdapter extends BaseAdapter {
    private ArrayList<Status> status_list;
    private LayoutInflater layoutInflater;
    private Context context;

    public StatusItemAdapter(Context aContext, ArrayList<Status> status_list) {
        this.context = aContext;
        this.status_list = status_list;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return status_list.size();
    }

    @Override
    public Object getItem(int i) {
        return status_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.status_list_item_layout, null);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.status_item_title);
            holder.text = (TextView) view.findViewById(R.id.status_item_text);
            holder.date_created = (TextView) view.findViewById(R.id.status_item_date);
            holder.creator = (TextView) view.findViewById(R.id.status_item_creator);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Status status = this.status_list.get(i);
        holder.title.setText(status.title);
        holder.text.setText(status.text);
        holder.date_created.setText(status.getDate());
        holder.creator.setText(status.creator_username);

        return view;
    }

    static class ViewHolder {
        TextView title, text, date_created, creator;
    }
}
