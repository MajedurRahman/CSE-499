package com.design.capstone.cse_499.NotificationHandler;

import android.util.Log;
import android.widget.Toast;

import com.design.capstone.cse_499.Application.MyApplication;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;

/**
 * Created by androidbash on 12/14/2016.
 */

//This will be called when a notification is received while your app is running.
public class MyNotificationReceivedHandler  implements OneSignal.NotificationReceivedHandler {
    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;
        String customKey;



        Toast.makeText(MyApplication.getContext(),notification.androidNotificationId+"", Toast.LENGTH_SHORT).show();



        Log.e("Receviednotificationid", " " + notification.androidNotificationId+"");

      if (data != null) {
            //While sending a Push notification from OneSignal dashboard
            // you can send an addtional data named "customkey" and retrieve the value of it and do necessary operation
            customKey = data.optString("customkey", null);
            if (customKey != null)
                Log.i("OneSignalExample", "customkey set with value: " + customKey);
        }

    }
}