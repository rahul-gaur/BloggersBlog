package com.rahulgaur.bloggersblog.blogPost;

public class User {
    public String thumb_image, name;

    public User() {
    }

    public User(String image, String name) {
        this.thumb_image = image;
        this.name = name;
    }

    public String getImage() {
        return thumb_image;
    }

    public void setImage(String image) {
        this.thumb_image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
