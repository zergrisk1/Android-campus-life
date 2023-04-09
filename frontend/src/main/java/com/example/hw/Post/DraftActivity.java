package com.example.hw.Post;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hw.Home.Status.CommentItemAdapter;
import com.example.hw.MainActivity;
import com.example.hw.Post.Draft;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.Profile.EditProfileActivity;
import com.example.hw.Profile.OtherUserProfileActivity;
import com.example.hw.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DraftActivity extends AppCompatActivity {
    private static final String TAG = DraftActivity.class.getSimpleName();;
    private String KEYS = "KEYS";
    private String user_id;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private Set draftset;
    private ArrayList<Draft> draftList = new ArrayList<Draft>();
    private TextView empty_txt;
    private ListView draftListView;
    ProgressBar spinner;
    private ImageView empty_tray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.getApplicationContext();
        Log.d(TAG, "onCreate: ");
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        user_id = extras.getString("user_id");
        setContentView(R.layout.activity_draft_list);
        draftListView = findViewById(R.id.draft_list_view);
        draftListView.setVisibility(View.VISIBLE);
        empty_tray = findViewById(R.id.empty_tray_draft_list);
        empty_txt = findViewById(R.id.empty_txt_draft_list);
        getDraftList();
    }

    SharedPreferences pref;
    @Override
    public void onResume() {
        draftList.clear();
        getDraftList();
        draftListView.setVisibility(View.VISIBLE);
        empty_tray.setVisibility(View.INVISIBLE);
        empty_txt.setVisibility(View.INVISIBLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("DRAFTLIST-OBTAINED"));

        super.onResume();
    }
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status_count = intent.getIntExtra("draft_count", 0);
            if (status_count == 0) {

            }
            else{
                //spinner.setVisibility(View.GONE);
                draftListView.setVisibility(View.VISIBLE);
                DraftItemAdapter adapter = new DraftItemAdapter(getApplicationContext(),draftList,user_id);
                draftListView.setAdapter(adapter);
                draftListView.setVisibility(View.VISIBLE);
                draftListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                        Bundle extras = new Bundle();
                        extras.putParcelable("draft", draftList.get(i));
                        extras.putString("user_id",user_id);
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                });
            }

        }
    };

public void getDraftList(){
    LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, new IntentFilter("DRAFTLIST-OBTAINED"));
    pref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
    if (pref == null){
        Toast.makeText(getApplicationContext(), "pref null", Toast.LENGTH_SHORT).show();
    }
    if (pref != null && pref.contains(KEYS)) {

        draftset = pref.getStringSet(KEYS, null);
        SharedPreferences.Editor editor = pref.edit();
        int count=0;
        for(Object key:draftset)
        {
            String Jsonstr = pref.getString(key.toString(),"");
            try {
                count++;
                JSONObject jObject = new JSONObject(Jsonstr);
                String title = jObject.getString("title");
                String text = jObject.getString("text");
                String type = jObject.getString("type");
                String media = jObject.getString("media");
                Draft draft = new Draft(text,title,type,media,key.toString());
                draftList.add(draft);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent("DRAFTLIST-OBTAINED");
        intent.putExtra("draft_count", count);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //getDraftList();
        draftListView.setVisibility(View.VISIBLE);
        empty_tray.setVisibility(View.INVISIBLE);
        empty_txt.setVisibility(View.INVISIBLE);


    } else {
        draftListView.setVisibility(View.INVISIBLE);
        empty_tray.setVisibility(View.VISIBLE);
        empty_txt.setVisibility(View.VISIBLE);
        Toast.makeText(this, "你没有草稿！", Toast.LENGTH_SHORT).show();
    }
}

}
