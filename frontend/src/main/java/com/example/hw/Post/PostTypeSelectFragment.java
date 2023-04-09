package com.example.hw.Post;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hw.MainActivity;
import com.example.hw.R;

public class PostTypeSelectFragment extends Fragment implements View.OnClickListener{
    private String type;
    private String user_id;
    private AppCompatActivity activity;
    private ImageButton btnImage, btnVideo, btnAudio, btnDraft;
    private String START_POST;

    public PostTypeSelectFragment() {
        // require a empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post_type_select, container, false);
        activity = (AppCompatActivity)v.getContext();
        MainActivity mainActivity = (MainActivity) activity;
        user_id = mainActivity.user_id;
        btnImage = (ImageButton)v.findViewById(R.id.btntypeImage);
        btnImage.setOnClickListener(this);
        btnAudio =(ImageButton)v.findViewById(R.id.btntypeAudio);
        btnAudio.setOnClickListener(this);
        btnVideo =(ImageButton)v.findViewById(R.id.btntypeVideo);
        btnVideo.setOnClickListener(this);
        btnDraft =(ImageButton)v.findViewById(R.id.btntypeDraft);
        btnDraft.setOnClickListener(this);
        return v;
    }
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btntypeImage:
                type = "txtandimg";
                break;
            case R.id.btntypeAudio:
                type = "audio";
                break;
            case R.id.btntypeVideo:
                type = "video";
                break;
            case R.id.btntypeDraft:
                type = "draft";
                break;
            default:
                break;
        }
        Intent intent;
        if(type.equals("draft")){
            intent = new Intent(getActivity(), DraftActivity.class);
        }
        else{
            intent = new Intent(getActivity(), PostActivity.class);
        }

        Bundle extras = new Bundle();
        extras.putString("type", this.type);
        extras.putString("user_id", this.user_id);

        intent.putExtras(extras);
        Log.d("click=",type);
        startActivityForResult(intent,0);
    }
    // After receiving updated user info from EditProfileActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent imgIntent = new Intent(getActivity(), PostActivity.class);
            imgIntent.putExtra("type", type);
            getActivity().startService(imgIntent);
            }
    }
}
