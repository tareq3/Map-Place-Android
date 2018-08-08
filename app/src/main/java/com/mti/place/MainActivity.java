/*
 * Created by Tareq Islam on 8/7/18 4:28 PM
 *
 *  Last modified 8/7/18 4:22 PM
 */

package com.mti.place;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mti.get_runtime_permissions.GetRuntimePermission;
import com.mti.pushdown_ext_onclick_single.SingleClick;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    //Constants
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;

    //member variables
    Context mContext;
    GetRuntimePermission mGetRuntimePermission;
    GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;







   SingleClick.get(findViewById(R.id.mText)).setOnSingleClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           //Todo: For using Place Api we need Gps Access or Fine Access location Permission

           //Todo: Get FineAccessLoaction Permission


           mGetRuntimePermission=new GetRuntimePermission(mContext, GetRuntimePermission.TYPE_OF_PERMISSIONS.ACCESS_FINE_LOCATION) {
               @Override
               public void setTaskCompleteAction() {
                   //Do the task you want to do right there
                   //write down your task

                   showPlaceMap();


               }
           };


       }
    });



        //

        // Build up the LocationServices API client
        // Uses the addApi method to request the LocationServices API
        // Also uses enableAutoManage to automatically when to connect/suspend the client
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        mGetRuntimePermission.onRequestPermissionsResult(requestCode,permissions,grantResults);


    }


    void showPlaceMap(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "need_location_permission_message", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            //Todo: Open the Gps PlacePicker Window
            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }



    }

private void updateUI(PlaceBuffer placeBuffer){

        ((TextView) findViewById(R.id.mText)).setText( "Name: "+ placeBuffer.get(0).getName().toString() +
                                                       "\n Address: " + placeBuffer.get(0).getAddress().toString());


}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Log.i(TAG, "No place selected");
                return;
            }

            // Extract the place information from the API
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            Toast.makeText(mContext, "place: "  + placeName +
                                          "place Address: "+ placeAddress +
                                            "place Id: "+ placeID, Toast.LENGTH_LONG).show();

            //insert new place id into db

            Hawk.put("tempPlaceId",placeID);

            getPlaceInfoFromGms();
        }
    }




    //As google terms and policy we can't store place info more than 30 day in our app
    private void getPlaceInfoFromGms() {

          final String places = Hawk.get("tempPlaceId", "null");



          PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, places);

          placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {


              @Override
              public void onResult(@NonNull PlaceBuffer places) {

                /*
                          places.get(0).getName().toString() +
                          places.get(0).getAddress().toString();//Address
*/

                    updateUI(places);
                  }
          });



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API Client Connection Successful!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "API Client Connection Suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "API Client Connection Failed!");
    }
}
