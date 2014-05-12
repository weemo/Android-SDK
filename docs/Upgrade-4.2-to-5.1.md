## Overview

This section describes how to upgrade an application built using the
Weemo 4.2 Android SDK to the Weemo 5.1 Android SDK.

The upgrade process consists of two parts. Weemo must upgrade your AppID to support Version 5.x, and you must re-compile your application with the 5.1 Android SDK.

## Initialization

no change

    Weemo.initialize(appID, context)


## Authentication

no change

    WeemoEngine weemo = Weemo.instance();
    weemo.authenticate(userID, userType);

## Call Creation

no change

    WeemoEngine weemo = Weemo.instance();
    weemo.createCall(contactID);

## Event Notification

no change

    // example

    public class CallActivity extends Activity {

        protected void onCreate(...) {
            ...
            WeemoEngine weemo = Weemo.instance();
            WeemoEventBus bus = weemo.eventBus();
            bus.register(this):
            ...
        }
           
        @WeemoEventListener
        public void onCallStatusChanged(CallStatusChanged Event e) {
           // handle the event
        }
    }



## Miscellaneous Changes


- com.weemo.sdk.event.WeemoEvent

    new public member function: toString

- com.weemo.sdk.event.global.WeemoGlobalResultEvent

    new public member function: toString

- com.weemo.sdk.event.global.StatusEvent

    new public member function: toString
  
   
