package com.example.hw.Post;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.MainActivity;
import com.example.hw.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String LOG_TAG = PostActivity.class.getSimpleName();
    private static final String TAG = PostActivity.class.getSimpleName();
    private String KEYS = "KEYS";
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private EditText postTitle, postMsg;
    private ImageButton postButton;
    private ImageView profile_pic_edit;
    private AppCompatActivity activity;
    private SharedPreferences pref;
    private Button btnCamera;
    private String mCurrentFilePath;
    private String cur_location;
    private File file = null;
    private String posttype;
    private MediaRecorder mediaRecorder;
    private String audioFileName;
    private String user_id, draft_id;
    private double cur_lat = 10;
    private double cur_lon = 19;
    private HashSet<String> draftset;
    private ArrayList<Draft> draftArrayList = new ArrayList<Draft>();
    private Draft draft;
    String img_src;
    public static final int REQUEST_TAKE_PHOTO = 100;
    public static final int REQUEST_SELECT_PICTURE = 200;
    public static final int REQUEST_SELECT_AUDIO = 250;
    public static final int REQUEST_SELECT_VIDEO = 300;
    public static final int REQUEST_TAKE_VIDEO = 400;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private boolean isRecording = false;
    private boolean isPosted = false;
    private Uri audioUri = null; // 오디오 파일 uri

    public PostActivity() {
        // require a empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Destroy");
        if (draft == null && !isPosted) {
            saveDraft();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Pause");
        //saveDraft();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        setContentView(R.layout.fragment_post);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        user_id = extras.getString("user_id");
        draft = extras.getParcelable("draft");
        if (draft == null) {
            posttype = extras.getString("type");
        } else {
            posttype = draft.type;
        }
        btnCamera = (Button) findViewById(R.id.postPhotoButton);
        profile_pic_edit = findViewById(R.id.postPic_edit);
        profile_pic_edit.setOnClickListener(this::chooseImage);
        if (posttype.equals("txtandimg")) {
            profile_pic_edit.setVisibility(View.VISIBLE);
            profile_pic_edit.setOnClickListener(this::chooseImage);
            btnCamera.setText("拍照");
        } else if (posttype.equals("audio")) {
            profile_pic_edit.setVisibility(View.VISIBLE);
            profile_pic_edit.setImageResource(R.drawable.post_audio_dark);
            profile_pic_edit.setOnClickListener(this::chooseAudio);
            btnCamera.setText("录音");
        } else if (posttype.equals("video")) {
            profile_pic_edit.setVisibility(View.VISIBLE);
            profile_pic_edit.setImageResource(R.drawable.post_video_dark);
            profile_pic_edit.setOnClickListener(this::chooseVideo);
            btnCamera.setText("录像");
        }

        postTitle = (EditText) findViewById(R.id.postTitle);
        postMsg = (EditText) findViewById(R.id.postMsg);
        postButton = (ImageButton) findViewById(R.id.postButton);
        postButton.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        draft = extras.getParcelable("draft");
        if (draft == null) {
            posttype = extras.getString("type");
        } else {
            posttype = draft.type;
            postTitle.setText(draft.title);
            postMsg.setText(draft.text);
            if (!draft.mediapath.equals("null")) {
                if (posttype.equals("txtandimg")) {
                    file = new File(draft.mediapath);
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap = rotateImage(bitmap, 90);
                    profile_pic_edit.setImageBitmap(bitmap);
                } else if (posttype.equals("audio")) {
                    file = new File(draft.mediapath);
                } else if (posttype.equals("video")) {
                    file = new File(draft.mediapath);
                }
            }
        }
        pref = getSharedPreferences("Draft", 0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO) {
            File file = new File(mCurrentFilePath);
            this.file = file;
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(mCurrentFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Bitmap rotatedBitmap = null;
                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }
                profile_pic_edit.setImageBitmap(rotatedBitmap);
            }
        } else if (requestCode == REQUEST_TAKE_VIDEO) {
            File file = new File(mCurrentFilePath);
            Log.d(TAG, "onActivityResult: REQUEST_TAKE_VIDEO" + mCurrentFilePath);
            this.file = file;
        } else if (requestCode == REQUEST_SELECT_PICTURE) {
            if (data == null) {
            } else {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    profile_pic_edit.setImageURI(selectedImageUri);
                    Log.d(TAG, "onActivityResult: REQUEST_SELECT_PICTURE" + data.getData());
                    // Get actual file URI
                    String wholeID = DocumentsContract.getDocumentId(selectedImageUri);
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String sel = MediaStore.Images.Media._ID + "=?";

                    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);

                    int columnIndex = cursor.getColumnIndex(column[0]);

                    if (cursor.moveToFirst()) {
                        img_src = cursor.getString(columnIndex);
                    }
                    cursor.close();

                    // Upload
                    file = new File(img_src);
                }
            }

        } else if (requestCode == REQUEST_SELECT_VIDEO) {
            if (data == null) {
            } else {
                Uri selectedVideoUri = data.getData();
                if (selectedVideoUri != null) {
                    String wholeID = DocumentsContract.getDocumentId(selectedVideoUri);
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Video.Media.DATA};
                    String sel = MediaStore.Video.Media._ID + "=?";

                    Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);

                    int columnIndex = cursor.getColumnIndex(column[0]);

                    if (cursor.moveToFirst()) {
                        img_src = cursor.getString(columnIndex);
                    }
                    cursor.close();
                    file = new File(img_src);
                    Log.d(TAG, "onActivityResult: REQUEST_SELECT_VIDEO" + img_src);
                }
            }
        } else if (requestCode == REQUEST_SELECT_AUDIO) {
            if (data == null) {
            } else {
                Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    String wholeID = DocumentsContract.getDocumentId(selectedUri);
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Audio.Media.DATA};
                    String sel = MediaStore.Audio.Media._ID + "=?";

                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);

                    int columnIndex = cursor.getColumnIndex(column[0]);

                    if (cursor.moveToFirst()) {
                        img_src = cursor.getString(columnIndex);
                    }
                    cursor.close();
                    file = new File(img_src);
                }
            }
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void chooseImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, REQUEST_SELECT_PICTURE);
    }

    private boolean checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    private void chooseAudio(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, REQUEST_SELECT_AUDIO);
    }

    private void chooseVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, REQUEST_SELECT_VIDEO);
    }

    // 保存草稿，只能保存一个
    public void saveDraft() {
        String title = postTitle.getText().toString();
        String msg = postMsg.getText().toString();
        pref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (title.equals("") && msg.equals("") && file == null) {
            Log.d(TAG, "saveDraft: nodraft");
        } else {
            String s;
            if (file == null) {
                s = "null";
            } else {
                s = file.getPath();
            }
            String jsonStr = "{\"title\":\"" + title + "\",\"type\":\"" + posttype
                    + "\",\"text\":\"" + msg + "\",\"text\":\"" + msg + "\",\"media\":\"" + s + "\"}";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String id = formatter.format(new Date());
            editor.putString(id, jsonStr);
            if (!pref.contains(KEYS)) {
                Set tmpSet = new HashSet() {
                };
                editor.putStringSet(KEYS, tmpSet);
                editor.apply();
            }
            if (pref == null && pref.contains(KEYS)) {
                Toast.makeText(getApplicationContext(), "pref null", Toast.LENGTH_SHORT).show();
            } else {
                draftset = (HashSet<String>) pref.getStringSet(KEYS, null);
                if (!draftset.contains(id)) {
                    draftset.add(id);
                }
                editor.putStringSet(KEYS, draftset);
                editor.apply();
                Toast.makeText(getApplicationContext(), "草稿保存成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getCurrentLocation() {
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted) {
            //Log.d("permission", "1");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//마지막 위치 받아오기
            Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            loc_Current = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (loc_Current == null && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                loc_Current = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                cur_lat = loc_Current.getLatitude();
                cur_lon = loc_Current.getLongitude();
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(
                            cur_lat,
                            cur_lon,
                            7);
                    cur_location = addresses.get(0).getAddressLine(0);
                } catch (IOException ioException) {
                    Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
                } catch (IllegalArgumentException illegalArgumentException) {
                    Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
                    if (addresses == null || addresses.size() == 0) {
                        Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
                    }

                }
            }


        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // {Some Code}
                }
            }
        }
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.postButton:
                clickPost(file);
                break;
            case R.id.postPhotoButton: {
                if (posttype.equals("txtandimg"))
                    capture();
                else if (posttype.equals("audio")) {
                    recordAudio();
                } else if (posttype.equals("video")) {
                    recordVideo();
                }
            }
            break;
            default:
                //Log.d(LOG_TAG, "No match");
                break;
        }
    }

    public void recordAudio() {
        if (isRecording) {
            isRecording = false;
            btnCamera.setText("开始录音");
            stopRecording();
        } else {
            if (checkAudioPermission()) {
                isRecording = true;
                postButton.setVisibility(View.INVISIBLE);
                btnCamera.setText("结束录音");
                String recordPath = getExternalFilesDir("/").getAbsolutePath();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                audioFileName = recordPath + "/" + "RecordExample_" + timeStamp + "_" + "audio.mp3";
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(audioFileName);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    mediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaRecorder.start();   // Recording is now started
            } else {
                Toast.makeText(activity, "请先获取录音权限", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void stopRecording() {
        // 녹음 종료 종료
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        postButton.setVisibility(View.VISIBLE);
        this.file = new File(audioFileName);
    }

    public void recordVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File tempDir = getCacheDir();
        File videoFile = null;
        //임시촬영파일 세팅

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String videoFileName = "Capture_" + timeStamp + "_"; //ex) Capture_20201206_
            File tempfile = File.createTempFile(
                    videoFileName,  /* 파일이름 */
                    ".mp4",         /* 파일형식 */
                    tempDir      /* 경로 */

            );
            mCurrentFilePath = tempfile.getAbsolutePath();

            videoFile = tempfile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (videoFile != null) {
            Uri videoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    videoFile);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            startActivityForResult(takeVideoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    public void capture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File tempDir = getCacheDir();
        File photoFile = null;

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String imageFileName = "Capture_" + timeStamp + "_"; //ex) Capture_20201206_
            File tempfile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    tempDir

            );
            mCurrentFilePath = tempfile.getAbsolutePath();

            photoFile = tempfile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    private void clickPost(File file) {
        String title = postTitle.getText().toString();
        getCurrentLocation();
        //cur_location = "sss";
        String type;
        String msg = postMsg.getText().toString();
        if (title.isEmpty() || msg.isEmpty()) {
            Toast.makeText(getApplicationContext(), "动态标题或内容不能为空", Toast.LENGTH_LONG).show();
        } else {
            if (posttype.equals("txtandimg")) {
                // Log.d("here1", posttype);
                if (file == null) {
                    type = "TEXT";
                    file = new File("null");
                } else {
                    type = "IMAGE";
                }
                // Log.d("type=", type);

            } else if (posttype.equals("audio")) {
                type = "AUDIO";
            } else if (posttype.equals("video")) {
                type = "VIDEO";
            } else {
                // Log.e("typeError", posttype);
                return;
            }
            String requestUrl = getResources().getString(R.string.backend_url) + "create-status";

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
                RequestBody requestBody;
                if (type.equals("TEXT")) {
                    if (cur_location == null) {
                        requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("user_id", user_id)
                                .addFormDataPart("type", type)
                                .addFormDataPart("title", title)
                                .addFormDataPart("text", msg)
                                .build();
                    } else {
                        requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("user_id", user_id)
                                .addFormDataPart("type", type)
                                .addFormDataPart("title", title)
                                .addFormDataPart("text", msg)
                                .addFormDataPart("location", cur_location)
                                .build();
                    }
                } else {
                    if (cur_location == null) {
                        requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("user_id", user_id)
                                .addFormDataPart("type", type)
                                .addFormDataPart("title", title)
                                .addFormDataPart("text", msg)
                                .addFormDataPart("media", file.getName(),
                                        RequestBody.create(MediaType.parse("image/*"), file))
                                .build();
                    } else {
                        requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("user_id", user_id)
                                .addFormDataPart("type", type)
                                .addFormDataPart("title", title)
                                .addFormDataPart("text", msg)
                                .addFormDataPart("media", file.getName(),
                                        RequestBody.create(MediaType.parse("image/*"), file))
                                .addFormDataPart("location", cur_location)
                                .build();
                    }
                }
                Request request = new Request.Builder()
                        .url(requestUrl).post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                        final String responseStr = response.body().string();
                        try {
                            JSONObject jObject = new JSONObject(responseStr);
                            boolean status = jObject.getBoolean("status");
                            if (status) {
                            } else {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


            Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_LONG).show();
            postTitle.getText().clear();
            postMsg.getText().clear();

            switchContent(title, msg);
            pref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            Intent intent = new Intent("STATUSLIST-OBTAINED");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            if(draft != null){
                if (pref != null && pref.contains(KEYS)) {
                    @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = pref.edit();
                    editor.remove(draft.key);
                    editor.apply();
                    isPosted = true;
                    finish();
                }
            }else{
                isPosted = true;
                finish();
            }


        }
    }

    // 用以在点击“发布”后跳转到动态列表页面，通过switchHome来添加动态
    private void switchContent(String title, String msg) {
        if (activity == null) {
            return;
        } else if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.switchHome(title, msg);
        }
    }
}