package com.rahulgaur.bloggersblog.notification;

import java.util.Date;

public class NotificationList extends NotificationID {
    private String message, post_id;
    private Date timestamp;

    public NotificationList() {
    }

    public NotificationList(String message, String post_id, Date timestamp) {
        this.message = message;
        this.post_id = post_id;
        this.timestamp = timestamp;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}