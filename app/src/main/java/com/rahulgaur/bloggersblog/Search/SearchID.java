package com.rahulgaur.bloggersblog.Search;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class SearchID {
    @Exclude
    public String SearchID;

    public <T extends SearchID> T withID(@NonNull final String id) {
        this.SearchID = id;
        return (T) this;
    }
}
