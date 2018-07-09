package com.rahulgaur.bloggersblog.notification;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class NotificationID {
    @Exclude
    public String NotificationID;

    public <T extends NotificationID> T withID(@NonNull final String id) {
        this.NotificationID = id;
        return (T) this;
    }
}
