package com.rahulgaur.bloggersblog.notification.notificationServices;

import com.rahulgaur.bloggersblog.notification.Remote.APIService;
import com.rahulgaur.bloggersblog.notification.Remote.RetrofitClient;

public class Common {
    public static String currentToken = "";

    private static String baseURL = "https://fcm.googleapis.com/";

    public static APIService getFCMCLient(){
        return RetrofitClient.getClient(baseURL).create(APIService.class);
    }
}
