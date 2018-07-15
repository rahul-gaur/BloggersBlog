package com.rahulgaur.bloggersblog.notification.Remote;

import com.rahulgaur.bloggersblog.notification.notificationServices.MyResponse;
import com.rahulgaur.bloggersblog.notification.notificationServices.Sender;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA9dNx_hQ:APA91bG650cuT1xEVYNqsya-FUOxyHIBeuebwXAzDBIX3IsnlPkfwNxRP2OX-XzLDnR-RuD16oIApx1c4ODyOqXVn1EFERZvSLl5imP7LDwzSTwKyveFJRBXO0AwzK0VDA9lxSnDMtMC"
    })
    @POST("fcm/send")
    retrofit2.Call<MyResponse> sendNotification(@Body Sender body);


}
