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

public class LikeListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Context context;
    String user_id;
    private ArrayList<String> username_list = new ArrayList<String>();
    private ArrayList<String> user_id_list = new ArrayList<String>();
//    private View.OnClickListener mOnClickListener;

    public  LikeListAdapter(Context aContext, ArrayList<String> username_list,ArrayList<String> user_id_list, String userid) {
        this.context = aContext;
        this.username_list =  username_list;
        this.user_id=userid;
        this.user_id_list=user_id_list;
//        this.mOnClickListener = onClickListener;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return  username_list.size();
    }

    @Override
    public Object getItem(int i) {
        return  username_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.like_list_item_layout, null);
            holder = new ViewHolder();
            holder.username = (TextView) view.findViewById(R.id.like_username);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        String username = this.username_list.get(i);
        holder.username.setText(username);
        return view;
    }

    static class ViewHolder {
        TextView username;
    }
}