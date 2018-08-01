package com.rahulgaur.bloggersblog.account.Followers;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class FollowersID {
    @Exclude
    public String FollowersID;

    public <T extends FollowersID> T withID(@NonNull final String id) {
        this.FollowersID = id;
        return (T) this;
    }
}
