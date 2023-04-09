package com.example.hw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.example.hw.Home.HomeFragment;
import com.example.hw.Post.PostActivity;
import com.example.hw.Post.PostTypeSelectFragment;
import com.example.hw.Profile.ProfileFragment;
import com.example.hw.Search.SearchFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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


public class MainActivity extends AppCompatActivity{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private BottomBarAdapter pagerAdapter;
    private ViewPager viewPagerMain;
    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    PostActivity postActivity = new PostActivity();
    PostTypeSelectFragment posttypeselectFragment = new PostTypeSelectFragment();
    SearchFragment searchFragment = new SearchFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    public String user_id;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        Log.d(LOG_TAG, user_id);

        this.verifyStoragePermissions(this);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                registerToken(token);
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPagerMain = findViewById(R.id.viewPagerMain);

        viewPagerMain.setOffscreenPageLimit(4);

        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());
        pagerAdapter.addFragments(homeFragment);
        pagerAdapter.addFragments(posttypeselectFragment);
        pagerAdapter.addFragments(searchFragment);
        pagerAdapter.addFragments(profileFragment);

        viewPagerMain.setAdapter(pagerAdapter);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.home:
                        viewPagerMain.setCurrentItem(0, false);
                        return true;
                    case R.id.post:
                        viewPagerMain.setCurrentItem(1, false);
                        return true;
                    case R.id.search:
                        viewPagerMain.setCurrentItem(2, false);
                        return true;
                    case R.id.profile:
                        viewPagerMain.setCurrentItem(3, false);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPagerMain.getCurrentItem();
        if (currentItem != 0){
            viewPagerMain.setCurrentItem(currentItem-1, true);
        }
        else {
            super.onBackPressed();
        }
    }

    // PostFragment调用switchHome来添加动态
    public void switchHome(String title, String msg) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
        homeFragment.addStatus(title, msg);
        viewPagerMain.setCurrentItem(0, false);
    }

    // 读取图片、视频、音频前需要调用verifyStoragePermissions来检查权限和获取权限
    public static void verifyStoragePermissions(Activity activity) {
        Log.d(LOG_TAG, "permission");
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void registerToken(String token) {
        //upload token to server
        String jsonStr = "{\"user_id\":\""+ user_id + "\",\"token\":\""+ token +"\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "register-token";

        try{
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，

            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(JSON, jsonStr);
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {}
                @Override
                public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                    final String responseStr = response.body().string();
                    try {
                        JSONObject jObject = new JSONObject(responseStr);
                        boolean status = jObject.getBoolean("status");
                        if (status) {
                            Log.d(LOG_TAG, "Saved successfully");
                        } else {
                            Log.d(LOG_TAG, "Saved failed");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}