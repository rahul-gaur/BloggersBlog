package com.rahulgaur.bloggersblog.Messaging.New_Message;

public class MessageItem extends new_messageID{
    String thumb_image;
    String name;

    public MessageItem(){
    }

    public MessageItem(String thumb_image, String name) {
        this.thumb_image = thumb_image;
        this.name = name;
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
}
