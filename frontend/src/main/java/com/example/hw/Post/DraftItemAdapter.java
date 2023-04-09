package com.example.hw.Post;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.hw.R;

import java.util.ArrayList;

public class DraftItemAdapter extends BaseAdapter {
    private static final String TAG = DraftItemAdapter.class.getSimpleName();
    ArrayList<Draft> draft_list = new ArrayList<Draft>();
    private String user_id;
    private LayoutInflater layoutInflater;
    private Context context;
//    private View.OnClickListener mOnClickListener;

    public DraftItemAdapter(Context aContext, ArrayList<Draft> comment_list,String userid) {
        Log.d(TAG, "DraftItemAdapter: ");
        this.user_id = userid;
        this.context = aContext;
        this.draft_list =  comment_list;
//        this.mOnClickListener = onClickListener;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return  draft_list.size();
    }

    @Override
    public Object getItem(int i) {
        return  draft_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d(TAG, "getView: hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.draft_list_item_layout, null);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.draft_item_title);
            holder.text = (TextView) view.findViewById(R.id.draft_item_text);
            holder.type = (TextView) view.findViewById(R.id.draft_item_type);
            holder.delete_txt =  (TextView) view.findViewById(R.id.draft_delete);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        Draft draft = this.draft_list.get(i);
        if(draft.title.equals("")){
            holder.title.setText("<空题目>");
        }else{
            holder.title.setText(draft.title);
        }
        if(draft.text.equals("")){
            holder.text.setText("<空内容>");
        }else{
            holder.text.setText(draft.text);
        }


        holder.type.setText(draft.type);
        if(draft.type.equals("txtandimg")){
            holder.type.setText("image");
        }
        holder.delete_txt.setText("删除");
        holder.delete_txt.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Bundle extras = new Bundle();
                        extras.putString("key", draft.key);
                        extras.putString("user_id",user_id);
                        Intent intent  = new Intent(v.getContext(), DraftDeleteActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtras(extras);
                        context.startActivity(intent);
                    }
                });
        return view;
    }

    static class ViewHolder {
        TextView title,text, type,delete_txt;
    }
}
