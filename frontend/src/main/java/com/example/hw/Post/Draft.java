package com.example.hw.Post;

import android.os.Parcel;
import android.os.Parcelable;

public class Draft implements Parcelable {
    public String text;
    public String title;
    public String type;
    public String mediapath;
    public String key;
    public Draft(String text,String title,String type,String mediapath,String key){
        this.text = text;
        this.title = title;
        this.type = type;
        this.mediapath = mediapath;
        this.key = key;
    }

    protected Draft(Parcel in) {
        text = in.readString();
        title = in.readString();
        type = in.readString();
        mediapath = in.readString();
        key = in.readString();
    }

    public static final Creator<Draft> CREATOR = new Creator<Draft>() {
        @Override
        public Draft createFromParcel(Parcel in) {
            return new Draft(in);
        }

        @Override
        public Draft[] newArray(int size) {
            return new Draft[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeString(title);
        parcel.writeString(type);
        parcel.writeString(mediapath);
        parcel.writeString(key);
    }
}
