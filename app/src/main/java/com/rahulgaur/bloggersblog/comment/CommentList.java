package com.rahulgaur.bloggersblog.comment;

import java.util.Date;

public class CommentList extends CommentID {
    private String message, user_id, post_user_id, postID;
    private Date timestamp;

    public CommentList() {
    }

    public CommentList(String message, String user_id, String post_user_id, String postID, Date timestamp) {
        this.message = message;
        this.user_id = user_id;
        this.post_user_id = post_user_id;
        this.postID = postID;
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

    public String getPost_user_id() {
        return post_user_id;
    }

    public void setPost_user_id(String post_user_id) {
        this.post_user_id = post_user_id;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
