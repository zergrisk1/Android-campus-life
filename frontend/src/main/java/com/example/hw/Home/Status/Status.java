package com.example.hw.Home.Status;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Status implements Parcelable {
    public String status_id, creator_id, creator_username, type, title, text,location,media;
    public Date date_created;
    public int like;

    public Status(String status_id, String creator_id, String creator_username, String type, String title, String text,
                  Date date_created, int like)
    {
        this.status_id = status_id;
        this.creator_id = creator_id;
        this.creator_username = creator_username;
        this.type = type;
        this.title = title;
        this.text = text;
        this.date_created = date_created;
        this.like = like;
    }

    protected Status(Parcel in) {
        status_id = in.readString();
        creator_id = in.readString();
        creator_username = in.readString();
        type = in.readString();
        title = in.readString();
        text = in.readString();
        like = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status_id);
        dest.writeString(creator_id);
        dest.writeString(creator_username);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(text);
        dest.writeInt(like);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    @Override
    public String toString() {
        return title;
    }

    public String getDate() {
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = spf.format(this.date_created);
        return date;
    }
}
