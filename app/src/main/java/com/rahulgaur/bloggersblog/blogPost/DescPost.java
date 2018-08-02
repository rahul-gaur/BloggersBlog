package com.rahulgaur.bloggersblog.blogPost;

import java.util.Date;

public class DescPost extends BlogPostID {
    String desc, user_id;
    Date timestamp;

    DescPost(){
    }

    public DescPost(String desc, String user_id, Date timestamp) {
        this.desc = desc;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
