package com.example.hw.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.MultiTapKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hw.Home.Status.ImageService;
import com.example.hw.LoginActivity;
import com.example.hw.MainActivity;
import com.example.hw.R;
import com.example.hw.RegisterActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = EditProfileActivity.class.getSimpleName();
    private Button saveProfileButton;
    private ImageView profile_pic_edit;
    private EditText username_edit, description_edit;
    private String user_id, username_user, desc_user, profile_pic_user;
    String img_src;

    int SELECT_PICTURE = 200;

    // Resume时register receiver，会捕捉到"IMAGE-DOWNLOADED"的broadcast，用以图片下载完成时通知
    @Override
    public void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "IMAGE-DOWNLOADED".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("IMAGE-DOWNLOADED"));
        super.onResume();
    }

    // 图片下载完成时，将本地存储的图片添加到imageView中
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "IMAGE-DOWNLOADED" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Image downloaded");
            File imgFile = new File(getResources().getString(R.string.image_loc) + profile_pic_user);
            profile_pic_edit.setImageURI(Uri.fromFile(imgFile));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        user_id = extras.getString("user_id");
        username_user = extras.getString("username");
        desc_user = extras.getString("description");
        profile_pic_user = extras.getString("profile_pic");
        Log.d(LOG_TAG, user_id);

        profile_pic_edit = findViewById(R.id.profilePic_edit);
        // Download image, start ImageService to download pic if image does not already exist
        if (!profile_pic_user.equals("null")) {
            File imgFile = new File(getResources().getString(R.string.image_loc) + profile_pic_user);
            if (imgFile.exists()) {
                profile_pic_edit.setImageURI(Uri.fromFile(imgFile));
            } else {
                Intent imgIntent = new Intent(getBaseContext(), ImageService.class);
                imgIntent.putExtra("image_type", "profile");
                imgIntent.putExtra("image_name", profile_pic_user);
                startService(imgIntent);
            }
        }
        profile_pic_edit.setOnClickListener(this::chooseImage);

        username_edit = findViewById(R.id.username_edit);
        username_edit.setText(username_user);

        description_edit = findViewById(R.id.profileDesc_edit);
        if (!desc_user.equals("null")) description_edit.setText(desc_user);

        saveProfileButton = findViewById(R.id.saveProfileButton);
        saveProfileButton.setOnClickListener(this::saveProfile);
    }

    private void saveProfile(View v) {
        username_user = username_edit.getText().toString();
        desc_user = description_edit.getText().toString();

        if (username_user.isEmpty()) {
            Toast.makeText(v.getContext(), "用户名不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        String jsonStr = "{\"user_id\":\""+ user_id + "\",\"username\":\""+ username_user +
                "\",\"description\":\""+ desc_user + "\"}";
        String requestUrl = getResources().getString(R.string.backend_url) + "save-userinfo";

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
                            Log.d(LOG_TAG, "success");
                            EditProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(v.getContext(), "修改成功", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent();
                                    Bundle extras = new Bundle();
                                    extras.putString("username", username_user);
                                    extras.putString("description", desc_user);
                                    extras.putString("profile_pic", profile_pic_user);
                                    intent.putExtras(extras);

                                    setResult(Activity.RESULT_OK, intent);
                                    finish();
                                }
                            });
                        } else {
                            String message = jObject.getString("message");
                            String showToast = "";
                            if (message.equals("username")) {
                                showToast = "用户名违法，长度应为6-10";
                            } else if (message.equals("description")) {
                                showToast = "用户简介违法，长度应小于256";
                            }
                            String finalShowToast = showToast;
                            EditProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(v.getContext(), finalShowToast, Toast.LENGTH_LONG).show();
                                }
                            });
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

    // Choose from gallery
    private void chooseImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, SELECT_PICTURE);
    }

    // After done choosing from gallery
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    profile_pic_edit.setImageURI(selectedImageUri);

                    // Get actual file URI
                    String wholeID = DocumentsContract.getDocumentId(selectedImageUri);
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String sel = MediaStore.Images.Media._ID + "=?";

                    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{ id }, null);

                    int columnIndex = cursor.getColumnIndex(column[0]);

                    if (cursor.moveToFirst()) {
                        img_src = cursor.getString(columnIndex);
                        Log.d(LOG_TAG, img_src);
                    }
                    cursor.close();

                    // Upload
                    File file = new File(img_src);
                    uploadImage(file);
                }
            }
        }
    }

    // Upload to backend
    private void uploadImage(File file) {
        Log.d(LOG_TAG, "Uploading image");
        String requestUrl = getResources().getString(R.string.backend_url) + "change-profile-pic";

        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", user_id)
                        .addFormDataPart("image", file.getName(),
                                RequestBody.create(MediaType.parse("image/*"), file))
                        .build();
                Request request = new Request.Builder()
                        .url(requestUrl).post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {};

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                        final String responseStr = response.body().string();
                        try {
                            JSONObject jObject = new JSONObject(responseStr);
                            boolean status = jObject.getBoolean("status");
                            if (status) {
                                profile_pic_user = jObject.getString("profile_pic");
                                EditProfileActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                EditProfileActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }
}