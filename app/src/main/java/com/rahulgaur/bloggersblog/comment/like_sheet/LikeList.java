package com.rahulgaur.bloggersblog.comment.like_sheet;

import java.util.Date;

public class LikeList extends LikeID{
    String name, thumb_image;
    Date timestamp;

    public LikeList(){}

    public LikeList(String name, String thumb_image, Date timestamp) {
        this.name = name;
        this.thumb_image = thumb_image;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
