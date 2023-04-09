package com.example.hw.Home.Status;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
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

public class ImageService extends Service{
    private static final String LOG_TAG = ImageService.class.getSimpleName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    String downloadUrl, image_name;
    public static boolean serviceState=false;

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

    @Override
    public void onCreate() {
        serviceState=true;
        HandlerThread thread = new HandlerThread("ServiceStartArguments",1);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"onStartCommand");

        String image_type = intent.getStringExtra("image_type");
        image_name = intent.getStringExtra("image_name");
        String image_url;

        if (image_type.equals("profile")) {
            image_url = getResources().getString(R.string.backend_url) + "profile-pic/" + image_name;
        } else {
            image_url = getResources().getString(R.string.backend_url) + "image/" + image_name;
        }

        this.downloadUrl = image_url;

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG,"DESTROY");
        serviceState=false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    public void downloadFile(){
        downloadFile(this.downloadUrl, this.image_name);
    }

    public void downloadFile(String fileURL, String fileName) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(fileURL);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                File.separator + "/download_tmp" + File.separator + fileName);

        //Enqueue a new download and same the referenceId
        long downloadReference = downloadManager.enqueue(request);
        Log.d(LOG_TAG, "Download complete");
    }
}
