package com.rahulgaur.bloggersblog.comment;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class CommentID {
    @Exclude
    public String CommentID;
    public <T extends CommentID> T withID(@NonNull final String id){
        this.CommentID = id;
        return (T) this;
    }
}