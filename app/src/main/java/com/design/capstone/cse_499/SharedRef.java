package com.design.capstone.cse_499;

import android.content.Context;
import android.content.SharedPreferences;

import com.design.capstone.cse_499.Model.User;

/**
 * Created by Majedur Rahman on 8/7/2017.
 */

public class SharedRef  {
SharedPreferences sharedPreferences;

    public SharedRef(Context context){

        sharedPreferences = context.getSharedPreferences("userData",Context.MODE_PRIVATE);
    }


    public void saveUserData(User user){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("fName" ,user.getFullName());
        editor.putString("email" , user.getEmail());
        editor.putString("pass", user.getPassword());
        editor.putString("phone" , user.getPhoneNumber());
        editor.putString("userName",user.getUserName());
        editor.commit();
    }

    public User getUser(){

       String  fName= sharedPreferences.getString("fName","No Data");
        String email = sharedPreferences.getString("email", "No Data");
        String password = sharedPreferences.getString("pass", "No data");
        String phoneNumber = sharedPreferences.getString("phone","No Data");
        String userName = sharedPreferences.getString("userName", "No Data");

        User userWithData = new User();
        userWithData.setFullName(fName);
        userWithData.setPhoneNumber(phoneNumber);
        userWithData.setUserName(userName);
        userWithData.setEmail(email);
        userWithData.setPassword(password);
        return userWithData;
    }
}
