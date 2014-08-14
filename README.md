# Weemo Android SDK

This repository contains the **Weemo Android SDK**.  
More information about Weemo Android SDK can be found at [www.weemo.com](http://www.weemo.com/)

Here are some other pages to help your implementation:
 - **Changelog** [here](CHANGELOG.md)
 - **Documentation** [here](http://docs.weemo.com/release/5.1/sdk/android)

## Get the library

The library is located in the [libs](libs) folder.  
It contains the native library `armeabi-v7a/libWeemoSDK.so` and a jar file `WeemoAndroidSDK.jar`.

## Get the Helper

The [Weemo Android Helper](https://github.com/weemo/Android-SDK-Helper) is a demo application implementing the Weemo Android SDK.  
It demonstrate Weemo Technologies on an Android application.

## SDK Architecture

![arch](http://docs.weemo.com/img/android_arch.png)

## Supported Platforms

### Hardware
- ARM device (armeabi-v7a)
- One camera at least

### Software
- Android 4.0 - Ice Cream Sandwich (API Level 14) or more recent

## Licences

The Weemo Android SDK relies on [VP8](http://www.webmproject.org) and [Opus](http://www.opus-codec.org/), which are packaged within the library.  
Their licences can be found in their respective web site.

## Setup

#### Weemo libraries

The SDK public interface is available in Java. It's core is a compiled native library.  
To add the SDK to your project, after creating a new Android Project, put both the file `WeemoAndroidSDK.jar` and the directory `armeabi-v7a` in the `libs` directory of your project.
Eclipse will automatically recognize the library and will add it to the Build Path.


#### Permissions

In order to run, this library requires some permissions to be declared by the Host application.  
If a permission is missing, the library will stop and log the missing permissions in the logcat.  
In your application's `AndroidManifest.xml` file, add the following permissions:

``` xml
<manifest >
    <!-- Internet access -->
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- PSTN calls -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <!-- Video capture -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <!-- Audio capture -->
    <uses-permission android:name="android.permission.CAMERA" />
    <!-- Change audio route -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
</manifest>
```


## General application flow

1. The host application authenticates with its application server. In addition to your business use cases, the server backends gets a ```Token``` from the Weemo back office (5) and sends it to the client application.
2. The host application creates the Weemo singleton. Every action related to the Weemo Cloud goes through Weemo objects, and every event is fired through those objects.
3. The SDK singleton connects to our cloud. 
4. Once connected, the application tries to authenticate the user.
5. The verification of the authentication is not mandatory during the POC/trial phase.
6. Once connected, the application communicates with our cloud.

<p align="center"><img src="http://docs.weemo.com/img/ios_03_appflow.png"></p>


## Events

To correctly use the Weemo Android SDK, you must understand its event model.  
Weemo Android SDK implements an event bus that is very similar to [Google's Guava event bus](https://code.google.com/p/guava-libraries/wiki/EventBusExplained).

#### Event Bus

The event bus is a singleton object that can be retrieved at any time with `Weemo.getEventBus()`, independently of the state of the Weemo Android SDK.  
It is the central part of the Weemo event model.

The event bus is responsible for:
- Keeping track of registered listeners
- Dispatching the events to the registered listeners

To register a listener, use `Weemo.getEventBus().register(listener)` with `listener` being the listener object to register.  
To unregister a listener, use `Weemo.getEventBus().unregister(listener)`.

<p align="center"><img src="http://docs.weemo.com/img/android_bus.png" width="700px"/></p>

You can register multiple listeners at the same time.
You can listen multiple times for the same event in different listeners.

#### Listener

A listener is an object that publicly exposes one or more methods that will be called when an event is fired.

An event method must:
- Be public
- Be annotated with @WeemoEventListener
- Take one parameter of the event type

Example:
``` java
  @WeemoEventListener
  public void method(ConnectedEvent e) { /* This will catch all connection events */ } 
```

A method can catch a specific event (such as above) or catch a more generic type of event:
``` java
  @WeemoEventListener
  public void method(WeemoCallEvent e) { /* This will catch all events related to an ongoing call */ }
```

Refer to the [Weemo Event Bus documentation](http://docs.weemo.com/release/5.1/sdk/android/classcom_1_1weemo_1_1sdk_1_1event_1_1_weemo_event_bus.html) for a more thorough explanation.

#### Activity or Fragment listener

While this model is very efficient and very easy to use, you must keep in mind the Android object lifecycle model.  
It is important NOT to prevent Activity and Fragment from being recycled.  
Therefore it is recommended to use directly Fragments and Activities as listeners.  
You should register them in `onCreate` and unregister them in `onDestroy`.  
Failing to unregister them will prevent them from being recycled and therefore create memory leaks.

Refer to the SDK-Helper code for good practices and examples of event handling.

#### Event errors

Events that can fail all inherit from `WeemoGlobalResultEvent`. Those events contains an `enum` that lists all of their possible errors.

These events can be treated:
- As an error of a particular event: use a `switch` on the `Error` `enum` of this event,
- As a global Weemo error: use a `switch` on the `error.getCode()` value. All error codes are defined in the `WeemoError.Code` class.


## Initialization
#### Prerequisites

Make sure that your application has received a token from your backend server when starting the authentication process.
If you use one of our Authentication Client, the code returns you a json string containing the Token.
If you use our SDK helper that is on Github, it contains the code to retrieve a token calling directly an authentication Client stored on your backend.

#### Creating Weemo Singleton

The Weemo cloud services are accessible through a singleton which is accessible with the `Weemo.getInstance()` method.  
While Weemo is not initialized, this method will return `null`.  
To initialize the Weemo singleton, call `Weemo.initialize(String appId, Context ctx)`.  
Upon initialization and connection to the Weemo cloud platform, a `ConnectedEvent` will be fired reflecting the success or the failure of the connection.  
If, and only if, the connection has succeded then `Weemo.getInstance()` will return a non-null value.

#### User authentication

After a successful initialization of the Weemo Android SDK, you are ready to authenticate your user into our Allocation system:

<p align="center"><img src="http://docs.weemo.com/img/android_auth_cf.png"></p>

User authentication takes place with: `Weemo.instance().authenticate(Context ctx, String userId, UserType type)`  
Where `userId`is the token received from your backend server and `type` is either `Weemo.UserType.INTERNAL` or `Weemo.UserType.EXTERNAL`.  

The authenticate method starts the authentication process.  
Your application is notified of the authentication result with an `AuthenticatedEvent`.  
If the call `event.isSuccess()` returns `true`, then you are correctly authenticated.  

After being authenticated, the Host application <b>should</b> set a display name.  
This is a user friendly name, that will be presented to remote users.
`Weemo.instance().setDisplayName(String name);`

#### Monitoring the connection

Once you are connected, you <b>should</b> monitor the `CanCreateCallChangedEvent` which is fired when you can't create a call anymore or when you can now create a call.

This event's error is very instructive, it can be:
- `CanCreateCallChangedEvent.Error.NETWORK_LOST` in which case the Weemo engine will try to retrieve the connection and fire another `CanCreateCallChangedEvent` when connection is back online.
- `CanCreateCallChangedEvent.Error.CLOSED` which indicates that the Weemo engine has been disconnected.
- `CanCreateCallChangedEvent.Error.SIP_NOK` which means something went wrong on the server side.

When a `CanCreateCallChangedEvent` is fired with an error, you <b>shouldn't</b> try to create a call until a new `CanCreateCallChangedEvent` without an error (indicating you can create a call) is fired.

#### Background management

You can optimize the Weemo engine to consume less battery when your application is in the background. This is NOT mandatory.  
To optimize the battery consumption, use `Weemo.instance().goToBackground()` and `Weemo.instance().goToForeground()`.  
Refer to the Weemo Android Helper code for good practices and examples of event background management.


## Create and Receive a call

#### Call Flow


#### Creating an outgoing call

<p align="center"><img src="http://docs.weemo.com/img/android_outgoing_cf.png"></p>

Before creating an outgoing call, the remote user availability can be checked by using `Weemo.instance().getStatus(String contactID);`.  
As this call is asynchronous, its result is given by the `StatusEvent` whose `canBeCalled()` method will tell you if the remote contact can indeed be called.  

To create a call, use this method `Weemo.instance().createCall(String contactID);`.  
When the call is created, your application will receive a `CallCreatedEvent`.  
This event does <b>not</b> indicates that the call has successfully started, you must wait for a `CallStatusChangedEvent`.

When the call rings on the remote's device, your application will receive a `CallStatusChangedEvent` whose `getCallStatus()` method returns `WeemoCall.CallStatus.PROCEEDING`.  
If the call fails to reach the remote contact, your application will receive a `CallStatusChangedEvent` whose `getCallStatus()` method returns `WeemoCall.CallStatus.ENDED`.

#### Receiving an incoming call

<p align="center"><img src="http://docs.weemo.com/img/android_incoming_cf.png"></p>

When receiving an incoming call, your application will first receive a `CallCreatedEvent`.  
Shortly after, your application will receive a `CallStatusChangedEvent` whose `getCallStatus()` method returns `WeemoCall.CallStatus.RINGING`.  
You should then present to your user a UI asking if he/she wants to accept the call.
- If the user answers: call `WeemoCall.resume()`
- If the user declines: call `WeemoCall.hangup()`

#### CallCreatedEvent

This event only tells you that a call has been created and who is the remote contact.  
However, since the call's `getCallStatus()` method will always return `WeemoCall.CallStatus.CREATED`, your application cannot know if it's an incoming or outcoming call.  
In the call workflow, you don't have to handle this event since a `CallStatusChangedEvent` will be fired shortly after and will instruct you of the call direction.  
Knowing that a call has been created but is not yet `RINGING` or `PROCEEDING` could be used by a service to display a specific notification when a call is going on.  
You can refer to the Android Weemo Helper's `WeemoService` service to see an example of this usecase.

#### Controlling a call

The controls of the WeemoCall object are self explanatory.
Both the Video and Audio streams are started upon call pickup.


## UI Integration

#### Video views

The Host application <b>should</b> provide two views that are used to render the video IN (video received from the remote contact) and video OUT preview (video captured from your camera and sent to the remote contact).
- Video IN: The view must be of type `com.weemo.sdk.view.WeemoVideoInFrame`. It must be assigned to the contact with `WeemoContact.setView(WeemoVideoInFrame frame)`.
- Video OUT: The view must be of type `com.weemo.sdk.view.WeemoVideoOutPreviewFrame`. It must be assigned to the call with `WeemoCall.setVideoOut(WeemoVideoOutPreviewFrame frame)`.

Note that:
- For both IN, and OUT, only one view can be used at a time. Setting a new `WeemoVideoInFrame` will cancel the previous one. The same goes for `WeemoVideoOutPreviewFrame`.
- You cannot send video without setting <b>and displaying</b> a `WeemoVideoOutPreviewFrame`. In Android, the camera can only be accessed through it's preview. A trick to make the preview display disappear is to set it's dimension to the minimum allowed: 2x2. However, if the Android view system stops drawing it, Weemo won't send video OUT.
- Both views are subclasses of `android.widget.FrameLayout` which means that you can put subviews inside it. Those subviews will appear on top of the video.
- You <b>must not</b> resize the inner video view of these frame nor try to access it in any way. If you want the video smaller, you must resize the frame.


#### Screen sharing

The Host application can share any View it can access by calling the following method: `screenShareStart(View view)`.  
The shared View on the remote device will be displayed in the `WeemoVideoInFrame` provided with `WeemoCall.setScreenShareIn(WeemoScreenShareInFrame frame)`.

Note that:
- Only one View can be shared at a time. Setting a new View will cancel the previous one.
- The shared View must have a size greater than 0px * 0px
- The shared View must be in the layout


#### Call controls

The Android Weemo Helper use a custom `WeemoCallControls` layout that extends `LinearLayout`.  
It displays multiple buttons mapped to specific actions.  
This allows the user to interract with the call.


## Known Limitations
Some features available in the JavaScript API are not currently available in the Android version of the SDK. Such features include:
- Multi-party calls.
- Pausing / resuming calls.
