package com.example.hw.Home.Status;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
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


public class MusicService extends Service {

    private static final String LOG_TAG = MusicService.class.getSimpleName();
    // Media player
    String name;
    String downloadUrl;
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
    private MusicBinder mBinder = new MusicBinder();

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
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
        Log.d(LOG_TAG,"onStartCommand");
        name = intent.getStringExtra("name");
        String audio_url;
        audio_url = getResources().getString(R.string.backend_url) + "audio/" + name;
        this.downloadUrl = audio_url;
        Log.d(LOG_TAG, this.downloadUrl);
        downloadFile();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(LOG_TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Destroying...");
        super.onDestroy();
    }
    public void downloadFile(){
        downloadFile(this.downloadUrl, this.name);
    }

    public void downloadFile(String fileURL, String fileName) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(fileURL);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,
                File.separator + "/download_tmp" + File.separator + fileName);

        //Enqueue a new download and same the referenceId
        long downloadReference = downloadManager.enqueue(request);
        Log.d(LOG_TAG, "Download complete");
    }

}