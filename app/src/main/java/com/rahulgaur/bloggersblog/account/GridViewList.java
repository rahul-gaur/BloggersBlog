package com.rahulgaur.bloggersblog.account;

import com.rahulgaur.bloggersblog.blogPost.BlogPostID;

public class GridViewList extends com.rahulgaur.bloggersblog.blogPost.BlogPostID {
    private String imageURL;
    //user_id;

    public GridViewList(){}

    public GridViewList(String imageURL) {
        this.imageURL = imageURL;
        //this.user_id = user_id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
/*
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }*/
}