package com.example.comp262_finalproject;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.comp262_finalproject.databinding.ActivityMapsBinding;
import com.google.firebase.messaging.FirebaseMessaging;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final String TAG = "MainActivity";

    private BroadcastReceiver NotificationReceiver;

    String channelId ;
    String channelName;

    double lat;
    double lng;
    String description;

    private MarkerOptions markerOpts;
    private Marker currentMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //This fragment is the simplest way to place a map in an application
        //It's a wrapper around a view of a map to automatically handle the necessary life cycle needs.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


         //register a channel for the notification
        //when the app is in foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            channelId = getString(R.string.default_notification_channel_id);
            channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =  getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {

            //extract data payload when the message is sent while the app is in the background
                lat = Double.parseDouble((String) getIntent().getExtras().get("lat"));
                lng = Double.parseDouble((String) getIntent().getExtras().get("lng"));

        }

        //create a Broadcast Receiver
        //A broadcast receiver (receiver) is an Android component which allows you to register for system or application events.
        //All registered receivers for an event are notified by the Android runtime once this event happens.
        NotificationReceiver = new BroadcastReceiver() {

            //If the event for which the broadcast receiver has registered happens,
            // the onReceive() method of the receiver is called by the Android system.
            //After the onReceive() of the receiver class has finished,
            // the Android system is allowed to recycle the receiver.
            @Override
            public void onReceive(Context context, Intent intent) {
                //process the push notification data
                //when the app is backgrounded

                //for test ing purpose
                Log.d(TAG, "Called from Broadcast Receiver: (data follows)");


                //check and listen if actions are the same when the broadcast got sent from the MyFirebaseMessageService
                if (intent.getAction().equals("NotificationReceived")) {
                    //check  if there's any data payload
                    if (intent.getExtras() != null) {

                        //extract data payload: lat, lng, one-line description
                        lat = intent.getExtras().getDouble("lat");
                        lng = intent.getExtras().getDouble("lng");
                        description = intent.getExtras().getString("description");

                        //-------------- set location marker ---------------------

                        //after having data from extracting from the intent
                        //set new location using lat, lng
                        LatLng location = new LatLng(lat,lng);

                        //create, add, return a marker object with the given MarkerOptions
                        markerOpts = new MarkerOptions()
                                .position(location)       //set position to a new location
                                .title(description)       //set title from one line description
                                .snippet("Lat: " + lat + ", Long: " + lng);

                        //add marker to the new location
                        currentMarker = mMap.addMarker(markerOpts);

                        //show info window
                        currentMarker.showInfoWindow();

                        //Move the map to show the marker
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f));
                    }
                }
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //if there's no data payload received from the notification
        //set a default lat/lng to Saskpolytech location
        if(lat == 0 && lng == 0 ){
            lat = 50.4078;
            lng = -104.5816;
            description = "Saskpolytech";
        }

        //here we should have lat, lng from extracting

        //new Location created
        LatLng l = new LatLng(lat, lng);

        //create, add, return a marker object with the given MarkerOptions

        markerOpts = new MarkerOptions()
                .position(l)
                .title("Marker at Regina")
                .snippet("Lat: " + lat + ", Long: " + lng);

        currentMarker = mMap.addMarker(markerOpts);

        //show info window
        //currentMarker.showInfoWindow();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 13f));

    }

    @Override
    protected void onResume() {

        super.onResume();

        //Register a receive for any local broadcasts that match the given IntentFilter.
        LocalBroadcastManager.getInstance(this).registerReceiver(NotificationReceiver,new IntentFilter("NotificationReceived"));

    }

    @Override
    protected void onPause() {

        super.onPause();

        //Unregister a previously registered BroadcastReceiver.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(NotificationReceiver);

    }
}