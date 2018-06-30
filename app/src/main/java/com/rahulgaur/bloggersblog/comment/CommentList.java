package com.rahulgaur.bloggersblog.comment;

import java.util.Date;

public class CommentList extends com.rahulgaur.bloggersblog.blogPost.BlogPostID {
    private String message, user_id;
    private Date timestamp;

    public CommentList(){

    }

    public CommentList(String message, String user_id, Date timestamp) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
