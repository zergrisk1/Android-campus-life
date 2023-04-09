package com.example.hw.Post;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DraftDeleteActivity extends AppCompatActivity {
    private static final String TAG = DraftDeleteActivity.class.getSimpleName();
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private String KEYS = "KEYS";
    private String key;
    private String user_id;
    Button caceldeletebut,deletebut;
    SharedPreferences pref;


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        key = extras.getString("key");
        user_id = extras.getString("user_id");
        setContentView(R.layout.activity_delete_draft);
        caceldeletebut = findViewById(R.id.cancelDraftcomment);
        deletebut = findViewById(R.id.deleteDraft);
        caceldeletebut.setOnClickListener((new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        }));
        deletebut.setOnClickListener((new View.OnClickListener() {
            public void onClick(View v) {
                pref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                if (pref != null && pref.contains(KEYS)) {
                    @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = pref.edit();
                    editor.remove(key);
                    editor.apply();
                    finish();
                }
            }
        }));

    }

}
