/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.comp262_finalproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // this is got fired when the app is in the background
    //Firebase notifications behave differently depending on the foreground/background state of the receiving app.
    // If we want foregrounded apps to receive notification messages or data messages,
    // we’ll need to write code to handle the onMessageReceived callback.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){

        //for testing purpose
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // call broadcast the message data to the main activity
        sendBroadCast(remoteMessage.getNotification().getBody(), remoteMessage.getData());
    }

    private void sendBroadCast(String messageBody, Map<String, String> remoteMessageData) {

        //testing in the console
        Log.d(TAG, "Sending Broadcast ...");

        //create a new intent with the action
        Intent intent = new Intent("NotificationReceived");

        //create a new Bundle
        Bundle data = new Bundle();

        //grab necessary data and extract their values from the Firebase message sent
        Double lat = Double.parseDouble(remoteMessageData.get("lat"));
        Double lng = Double.parseDouble(remoteMessageData.get("lng"));
        String des = messageBody;

        //put them in a bundle
        data.putDouble("lat", lat);
        data.putDouble("lng", lng);
        data.putString("description", des);

        // put all of them in the intent
        intent.putExtras(data);

        //send it to BroadcastReceiver where it listens to action of "NotificationReceived"
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


}
