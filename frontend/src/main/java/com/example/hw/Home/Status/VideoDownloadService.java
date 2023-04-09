package com.example.hw.Home.Status;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.hw.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class VideoDownloadService extends Service {

    private static final String LOG_TAG = VideoDownloadService.class.getSimpleName();
    // Media player
    String name;
    String downloadUrl;
    public MediaPlayer mediaPlayer;
    // Used to pause/resume MediaPlayer
    private int resumePosition;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            downloadFile();
            stopSelf(msg.arg1);
        }
    }
    private VideoBinder mBinder = new VideoBinder();

    public class VideoBinder extends Binder {
        VideoDownloadService getService() {
            return VideoDownloadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        // Create player
        Log.d(LOG_TAG, "Media Player created");
        mediaPlayer = new MediaPlayer();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Movies/nevergonna.mp4";
        try {
            mediaPlayer.setDataSource(this, Uri.parse(path));
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }
    public void downloadFile(){
        downloadFile(this.downloadUrl, this.name);
    }

    public void downloadFile(String fileURL, String fileName) {
        try {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/download_tmp");
            if (root.exists() && root.isDirectory()) {

            } else {
                Log.d(LOG_TAG, "Directory created");
                root.mkdir();
            }
            Log.d(LOG_TAG, root.getPath());
            Log.d(LOG_TAG, fileURL);
            URL u = new URL(fileURL);
            URLConnection c = u.openConnection();
            c.connect();
            int fileSize = c.getContentLength();
            Log.d(LOG_TAG, String.valueOf(fileSize));

            // Download file
            InputStream input = new BufferedInputStream(u.openStream(), 8192);

            // Output stream
            FileOutputStream output = new FileOutputStream(root + "/" + fileName);

            byte data[] = new byte[1024];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            Toast.makeText(this, "视频下载完成", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("VIDEO-DOWNLOADED");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

}