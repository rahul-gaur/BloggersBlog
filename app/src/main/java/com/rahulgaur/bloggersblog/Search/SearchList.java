package com.rahulgaur.bloggersblog.Search;

public class SearchList extends SearchID{
    String thumb_image;
    String name;
    String user_id;

    SearchList() {
    }

    public SearchList(String thumb_image, String name, String user_id) {
        this.thumb_image = thumb_image;
        this.name = name;
        this.user_id = user_id;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}