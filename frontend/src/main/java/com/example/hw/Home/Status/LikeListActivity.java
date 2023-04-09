package com.example.hw.Home.Status;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.localbroadcastmanager.content.LocalBroadcastManager;

        import com.example.hw.Profile.OtherUserProfileActivity;
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

public class LikeListActivity  extends AppCompatActivity {
    private String user_id, status_id,comment_id;
    private ArrayList<String> username_list = new ArrayList<String>();
    private ArrayList<String> user_id_list = new ArrayList<String>();
    Button caceldeletebut,deletebut;
    ListView likelistview;


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("PostComment", "onStart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        user_id_list = extras.getStringArrayList("user_id_list");
        username_list = extras.getStringArrayList("username_list");
        user_id = extras.getString("user_id");
        setContentView(R.layout.activity_like_list);
        likelistview = findViewById(R.id.like_list_view);
        likelistview.setAdapter(new LikeListAdapter(getApplicationContext(),username_list,user_id_list ,user_id));
        likelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                Bundle extras = new Bundle();
                extras.putString("user_id_self", user_id);
                extras.putString("user_id_other", user_id_list.get(i));
                intent.putExtras(extras);
                startActivity(intent);
            }
        });


    }
}