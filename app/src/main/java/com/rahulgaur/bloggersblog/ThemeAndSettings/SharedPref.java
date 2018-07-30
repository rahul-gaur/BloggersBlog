package com.rahulgaur.bloggersblog.ThemeAndSettings;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    SharedPreferences sharedPreferences;

    public SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    public void setProfileBackground(String color){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("color",color);
        editor.apply();
    }

    public String loadProfilBackground(){
        return sharedPreferences.getString("color","");
    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    public Boolean loadNightModeState() {
        Boolean state = sharedPreferences.getBoolean("NightMode", false);
        return state;
    }
}