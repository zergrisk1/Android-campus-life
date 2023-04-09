package com.example.hw.Home.Status;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Comment implements Parcelable {
    public String comment_id, content, creator_user_id,creator_username;
    public Date date_created;

    public Comment(String comment_id, String content, String creator_username, String creator_user_id, Date date_created)
    {
        this.comment_id = comment_id;
        this.content = content;
        this.creator_username = creator_username;
        this.creator_user_id = creator_user_id;
        this.date_created = date_created;
    }
    protected Comment(Parcel in) {
        comment_id = in.readString();
        content = in.readString();
        creator_username = in.readString();
        creator_user_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comment_id);
        dest.writeString(content);
        dest.writeString(creator_username);
        dest.writeString(creator_user_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public String toString() {
        return content;
    }

    public String getDate() {
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = spf.format(this.date_created);
        return date;
    }
}