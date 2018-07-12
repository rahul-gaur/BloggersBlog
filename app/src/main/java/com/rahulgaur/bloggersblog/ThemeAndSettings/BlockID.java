package com.rahulgaur.bloggersblog.ThemeAndSettings;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class BlockID {
    @Exclude
    String BlockID;

    public <T extends BlockID> T withID(@NonNull final String id) {
        this.BlockID = id;
        return (T) this;
    }
}
