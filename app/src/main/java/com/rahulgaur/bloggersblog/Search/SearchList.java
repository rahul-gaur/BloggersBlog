package com.rahulgaur.bloggersblog.Search;

public class SearchList {
    String user_profile;
    String user_name;

    SearchList() {
    }

    public SearchList(String user_profile, String user_name) {
        this.user_profile = user_profile;
        this.user_name = user_name;
    }

    public String getUser_profile() {
        return user_profile;
    }

    public void setUser_profile(String user_profile) {
        this.user_profile = user_profile;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}
