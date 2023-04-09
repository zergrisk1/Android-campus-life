package com.example.hw.Profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hw.Home.Status.Status;
import com.example.hw.R;

import java.util.ArrayList;

public class NotifyItemAdapter extends BaseAdapter {
    private ArrayList<Notifications> notify_list;
    private LayoutInflater layoutInflater;
    private Context context;

    public NotifyItemAdapter(Context aContext, ArrayList<Notifications> notify_list) {
        this.context = aContext;
        this.notify_list = notify_list;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return notify_list.size();
    }

    @Override
    public Object getItem(int i) {
        return notify_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        NotifyItemAdapter.ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.notify_list_item_layout, null);
            holder = new NotifyItemAdapter.ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.notify_item_title);
            holder.text = (TextView) view.findViewById(R.id.notify_item_text);
            holder.date_created = (TextView) view.findViewById(R.id.notify_item_date);
            holder.type = (ImageView) view.findViewById(R.id.notify_item_type);
            view.setTag(holder);
        } else {
            holder = (NotifyItemAdapter.ViewHolder) view.getTag();
        }

        Notifications notifications = this.notify_list.get(i);
        holder.title.setText(notifications.title);
        holder.text.setText(notifications.text);
        holder.date_created.setText(notifications.getDate());
        String type = notifications.type;
        if (type.equals("LIKE")) {
            holder.type.setImageResource(R.drawable.like_button);
        } else if (type.equals("COMMENT")) {
            holder.type.setImageResource(R.drawable.comment);
        } else if (type.equals("NEW_STATUS")) {
            holder.type.setImageResource(R.drawable.status);
        }

        return view;
    }

    static class ViewHolder {
        TextView title, text, date_created;
        ImageView type;
    }
}
