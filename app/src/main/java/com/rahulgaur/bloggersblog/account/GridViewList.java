package com.rahulgaur.bloggersblog.account;

import com.google.firebase.firestore.FirebaseFirestore;

public class GridViewList extends com.rahulgaur.bloggersblog.blogPost.BlogPostID {
    private String imageURL;
    private String blogPostID;


    public GridViewList() {
    }

    public GridViewList(String imageURL, String blogPostID) {
        this.imageURL = imageURL;
        this.blogPostID = blogPostID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getBlogPostID() {
        return blogPostID;
    }

    public void setBlogPostID(String blogPostID) {
        this.blogPostID = blogPostID;
    }
}