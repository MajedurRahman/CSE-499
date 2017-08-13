package com.design.capstone.cse_499.NotificationHandler;

import android.content.Intent;
import android.util.Log;


import com.design.capstone.cse_499.Activity.AnotherActivity;
import com.design.capstone.cse_499.Activity.MapActivity;
import com.design.capstone.cse_499.Application.MyApplication;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

/**
 * Created by androidbash on 12/14/2016.
 */

public class MyNotificationOpenedHandler  implements OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;


        if (actionType == OSNotificationAction.ActionType.ActionTaken) {
            Log.e("OneSignalExample", "Button pressed with id: " + result.action.actionID);
            if (result.action.actionID.equals("accept")) {

                Intent intent = new Intent(MyApplication.getContext(), AnotherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApplication.getContext().startActivity(intent);


            } else if (result.action.actionID.equals("cancel")) {

            }


        }

        if (data!=null&&data.optString("tap").equals("tap")){

            Intent intent = new Intent(MyApplication.getContext(), MapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getContext().startActivity(intent);

        }

    }
}