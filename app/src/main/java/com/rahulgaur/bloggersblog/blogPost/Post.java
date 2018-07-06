package com.rahulgaur.bloggersblog.blogPost;

import java.util.Date;

public class Post extends BlogPostID {
    public String image_url, thumb_image_url, desc, user_id;
    public Date timestamp;

    public Post() {
    }

    public Post(String image_url, String thumb_image_url, String desc, String user_id, Date timestamp) {

        this.image_url = image_url;
        this.thumb_image_url = thumb_image_url;
        this.desc = desc;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_image_url() {
        return thumb_image_url;
    }

    public void setThumb_image_url(String thumb_image_url) {
        this.thumb_image_url = thumb_image_url;
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
