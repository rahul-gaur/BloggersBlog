package com.rahulgaur.bloggersblog.comment.like_sheet;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class LikeID {
    @Exclude
    public String LikeID;

    public <T extends LikeID> T withID(@NonNull final String id) {
        this.LikeID = id;
        return (T) this;
    }
}
