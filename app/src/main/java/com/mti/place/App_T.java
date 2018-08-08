/*
 * Created by Tareq Islam on 8/7/18 8:07 PM
 *
 *  Last modified 8/7/18 8:07 PM
 */

package com.mti.place;

import android.app.Application;

import com.orhanobut.hawk.Hawk;

/***
 * Created by Tareq on 07,August,2018.
 */
public class App_T extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Hawk.init(this)
                    .build();

    }

    //Check App is virgin or not
   public static boolean onVirgin(){
       if( Hawk.get("virgin",true)){
           //Do first run stuff here then set first run as false
           Hawk.put("virgin",false);
           return true;
       }else{
           return false;
       }

    }
}
