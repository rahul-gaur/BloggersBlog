package com.rahulgaur.bloggersblog.ThemeAndSettings;

public class BlockList extends BlockID {
    String user_id;

    public BlockList() {
    }

    public BlockList(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
