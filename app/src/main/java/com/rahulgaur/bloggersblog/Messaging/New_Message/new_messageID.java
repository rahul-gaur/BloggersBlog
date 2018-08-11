package com.rahulgaur.bloggersblog.Messaging.New_Message;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class new_messageID {
    @Exclude
    public String new_messageID;

    public <T extends new_messageID> T withID(@NonNull final String id){
        this.new_messageID = id;
        return (T) this;
    }
}
