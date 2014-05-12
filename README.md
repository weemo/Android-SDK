# Weemo Android SDK

This repository contains the Weemo Android SDK. More information about Weemo SDKs can be found at [http://www.weemo.com/](http://www.weemo.com/)


Here are some other pages to help your implementation:

- API Reference on [http://docs.weemo.com/release/5.1/sdk/android/](http://docs.weemo.com/release/5.1/sdk/android/)


## Changelog

[The changelog is available here](CHANGELOG.md).


## Get the library

The libraries are located in the [libs directory](https://github.com/weemo/Android-SDK/tree/master/libs). Note that this repository is in form of a Android library project. You don't have to use it but it can come in handy if you want to use git submodules.

## SDK Architecture

![arch](http://docs.weemo.com/img/android_arch.png)


## Supported Platforms

Weemo Android SDK requires Android 4.0.3 (ICS) or more recent, with an ARM device that has at least one camera.


## Licences

The Weemo Android SDK relies on [VP8](http://www.webmproject.org) and [Opus](http://www.opus-codec.org/), which are packaged within the library.

Their licences can be found in their respective web site.

## Notes

- The SDK uses ARM Neon code and cannot be used in the emulator.
- The SDK was compiled against SDK 19.

## SDK Helper

SDK Helper is a sample application implementing the Weemo Android SDK. To demonstrate Weemo Technologies you can follow this quick-tutorial to run a Weemo Application:

[SDK Helper - Quick tutorial](https://github.com/weemo/Android-SDK-Helper)

## Setup

#### Weemo libraries

The SDK public interface is available in Java. It's core is a compiled native library.

To add the SDK to your project, after creating a new Android Project, put both the file `WeemoAndroidSDK.jar` and the directory `armeabi-v7a` in the `lib` directory of your project.


#### Permissions

In your application's AndroidManifest.xml, add the following permission declarations :

	<!-- Video conferencing -->
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.RECORD_AUDIO" />
	<uses-permission android:name="android.permission.CAMERA" />
	<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
	<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

	<!-- Crash reporting, OPTIONAL -->
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.READ_LOGS" />
	<uses-permission android:name="android.permission.READ_PHONE_STATE" />

#### Null sanity cheks in the SDK: FindBugs

<b><i>This is optional. Using Findbugs is recommended but absolutely not mandatory!</i></b>

FindBugs™ is a program to find bugs in Java programs. It looks for instances of "bug patterns" - code instances that are likely to be errors.

The SDK uses FindBugs as a JSR-305 implementation to detect nullability errors. It works by:
 * Defining that every methods in the SDK packages will return non-null values
 * Annotating SDK methods that can return null with @CheckForNull
This way, FindBugs can verify that you check @CheckForNull methods return value for nullability before using it. This is a very handy way to impose sanity checks and rigor in the code. It also allows to avoid a lot of `NullPointerException`.

While the SDK uses FindBugs, it is not mandatory to have it installed in your IDE to use it. We recommend it as a general good practice, but it is not a hard dependency.

If you wish to use it, follow those steps:
 * Install [FindBugs](http://findbugs.sourceforge.net/) in Eclipse.
 * In your project's properties > Java Build Path > Libraries, add the FINDBUGS_ANNOTATIONS variable.
 * For each package in your project, create a `package-info.java` file that only contains the package declaration annotated with `@com.weemo.sdk.EverythingIsNonNullByDefault`.
 * Enjoy the sweet pain of FindBugs Telling you your not doing it right...


## General application flow

1. The host application authenticates with its application server.
2. The host application creates the Weemo singleton. Every action related to the Weemo Cloud goes through Weemo objects, and every event is fired through those objects.
3. The SDK singleton connects to our cloud. 
4. Once connected, the application tries to authenticate the user.
5. The verification of the authentication is not mandatory during the POC/trial phase.
6. Once connected, the application communicates with our cloud.

<p align="center"><img src="http://docs.weemo.com/img/ios_03_appflow.png"></p>


## Events

To correctly use the Weemo Android SDK, you must understand its event model.

Weemo implements an event bus that is very similar to [Google's Guava event bus](https://code.google.com/p/guava-libraries/wiki/EventBusExplained).

#### Event Bus

The event bus is a singleton object that can be retrieved at any time with `Weemo.getEventBus()`, independently of the state of the Weemo SDK. It is the central part of the Weemo event model.

The event bus is responsible for :
 * Keeping track of registered listeners
 * Dispatching the events to the listener

To register an object listener, simply call `Weemo.getEventBus().register(listener)` with `listener` being the listener object to register. To unregister a listener, simply call `Weemo.getEventBus().unregister(listener)`.

<p align="center">
<img src="http://docs.weemo.com/img/android_bus.png" width="700px"/>
</p>

You can (and probably will) register multiple listeners at the same time. You can (and probably will) listen multiple times for the same event in different listeners.

#### Listener

A listener is an object that publicly exposes one or more methods that will be called when an event is fired.

A event method must:
 * Be public
 * Be annotated with @WeemoEventListener
 * Take one parameter of the event type

Example:
```
  @WeemoEventListener
  public void method(ConnectedEvent e) { } // This will catch all connection events
```

A method can catch a specific event (such as above) or catch a more generic type of event:
```
  @WeemoEventListener
  public void method(WeemoCallEvent e) { } // This will catch all events related to an ongoing call
```

Refer to the [Weemo Event Bus documentation](http://docs.weemo.com/sdk/5.1/android/classcom_1_1weemo_1_1sdk_1_1event_1_1_weemo_event_bus.html) for a more thorough explanation.

#### Activity or fragment listener

While this model is very efficient and very easy to use, you must keep in mind the Android object lifecycle model. It is important NOT to prevent activity and fragment from being recycled. Therefore :

 * It is highly discouraged to use anonymous classes as listeners as you won't be able to unregister them and will therefore create memory leaks.
 * It is recommended to use directly fragments and activities as listeners. You should register them in `onCreate` and unregister them in `onDestroy`. Failing to unregister them will prevent them from being recycled and therefore create memory leaks.

Refer to the SDK-Helper code for good practices and examples of event handling.

#### Event errors

Events that can fail all inherit from `WeemoGlobalResultEvent`. Those events each contain an `enum` that lists all of their possible errors.

These events can be treated :
 * As an error of a particular event: use a `switch` on the `Error` `enum` of this event,
 * As a global Weemo error: use a `switch` on the `error.getCode()` value. All error codes are defined in the `WeemoError.Code` class.

If a `WeemoGlobalResultEvent` has a `null` `error.getError()` return, then it means the operation reported by this event has succeeded.


## Initialization

#### Creating Weemo Singleton

The Weemo cloud services are accessible through a singleton which is accessible with the `Weemo.getInstance()` method. While Weemo is not initialized, this method will return `null`.

To initialize the Weemo singleton, call `Weemo.initialize(appId, Context)` with a String appid and a context (usually the activity calling it).

Upon initialization and connection to the Weemo cloud platform, a `ConnectedEvent` will be fired reflecting the success or the failure of the connection. If (and only if) the connection has succeded then `Weemo.getInstance()` will return a non-null value.

#### User authentication

After a successful initialization of the Weemo SDK, you are ready to authenticate your user into our Allocation system:

<p align="center"><img src="http://docs.weemo.com/img/android_auth_cf.png"></p>

User authentication takes place with: 
```
Weemo.instance().authenticate(String userId, UserType type);
```
Where `type` is `Weemo.UserType.INTERNAL` or `Weemo.UserType.EXTERNAL`.

The `Weemo.authenticate` method starts the authentication process.

Your application is notified of the result of the authentication an `AuthenticatedEvent`. If this event contains a `null` error, then you are correctly authenticated.

After being authenticated, the HostApp <b>should</b> set a display name. This is a user friendly name, that will be presented to remote users when the user is calling said remote user.
```
weemo.setDisplayName(displayName);
```

#### Monitoring the connection

Once you are connected, you <b>should</b> monitor the `CanCreateCallChangedEvent` which is fired when you can't create a call anymore or when you can now create a call.

This event's error is very instructive, it can be :
 * `CanCreateCallChangedEvent.Error.NETWORK_LOST` in which case the Weemo engine will try to retrieve the connection and fire another `CanCreateCallChangedEvent` when connection is back online.
 * `CanCreateCallChangedEvent.Error.DESTROYED` which indicates that the Weemo engine has been destroyed (by you, this is a normal event)
 * `CanCreateCallChangedEvent.Error.SYSTEM_ERROR` which means... a system error.

When a `CanCreateCallChangedEvent` is fired with an error, you <b>shouldn't</b> try to create a call until a new `CanCreateCallChangedEvent` without an error (indicating you can create a call) is fired.

#### Background management

You can optimize weemo engine to consume less battery when your application is in the background. This is NOT mandatory. To optimize the battery consumption, use `goToBackground` and `goToForeground`.

Refer to the SDK-Helper code for good practices and examples of event background management.


## Create and Receive a call

#### Call Flow

**Create a call:**
<p align="center"><img src="http://docs.weemo.com/img/android_outgoing_cf.png"></p>

**Receive a call:**
<p align="center"><img src="http://docs.weemo.com/img/android_incoming_cf.png"></p>

#### Creating a call

Before creating an outgoing call, the remote user availability can be checked by using
```
Weemo.instance().getStatus(remoteContactID);
```

As this query is asynchronous, it's result is given by the `StatusEvent` whose `canBeCalled()` method will tell you if the remote contact can indeed be called.

You <b>shouldn't</b> create a call without checking the remote contact status first.

Once you know the remote contact can be called, you create a call to this contact with:
```
Weemo.instance().createCall(contactId);
```

When the call is created, your application will receive a `CallCreatedEvent`. This event does <b>not</b> indicates that the call has successfully started, you must wait for a `CallStatusChangedEvent`.

When the call rings in the remote contact's device, your application will receive a `CallStatusChangedEvent` whose `getCallStatus()` method returns `WeemoCall.CallStatus.PROCEEDING`.

If the call fails to reach the remote contact, your application will receive a `CallStatusChangedEvent` whose `getCallStatus()` method returns `WeemoCall.CallStatus.ENDED`.

#### Receiving an incoming call

When receiving an incoming call, your application will first receive a `CallCreatedEvent`.

Shortly after, your application will receive a `CallStatusChangedEvent` whose `getCallStatus()` method returns `WeemoCall.CallStatus.RINGING`. You should then present to your user a UI asking if he answers the call.

 * If the user answers: call `WeemoCall.resume()`
 * If the user declines: call `WeemoCall.hangup()`

#### CallCreatedEvent

This event only tells you that a call has been created and who is the remote contact. However, since the call's `getCallStatus()` method will always return `WeemoCall.CallStatus.CREATED`, your application cannot know if it is an incoming or outcoming call.

In the call workflow, you don't have to handle this event since a `CallStatusChangedEvent` will be fired shortly after and will instruct you of the call direction.

Knowing that a call has been created but is not yet `RINGING` or `PROCEEDING` can be used by, for example, a service to display a specific notification when a call is going on. You can refer to the SDK-Helper's `ConnetctedService` service to see an example of this usecase.

#### Controlling a call

The controls of the WeemoCall object are self explanatory.
Both the Video and Audio streams are started upon call pickup.


## Audio control

The audio IN (audio played that comes from the remote contact) in Weemo SDK for Android uses [`android.media.AudioManager`](http://developer.android.com/reference/android/media/AudioManager.html). The Weemo SDK for Android does not exposes any method to control audio played.

With [`android.media.AudioManager`](http://developer.android.com/reference/android/media/AudioManager.html), you can:
 * Set the volume.
 * Use audio effects.
 * Mute audio IN.
 * Change out route (earphones / speakers).


## GUI Integration

The Activity whose view displays the call controls is here called the `callActivity`.

#### Call controls

In your application, this `callActivity` is entirely customizable as it is your responsibility to create it. You choose which controls and which views are available to the user. You can freely re-use the code helper's `callActivity`.

The typical `callActivity` implementation should at least provide a button for hanging up the call by calling the `WeemoCall.hangup()` method.

#### Video views

A `callActivity` <b>should</b> provide two views that are used to render the video IN (video received from the remote contact) and video OUT preview (video captured from your camera and sent to the remote contact).

 * Video IN: The view must be of type `com.weemo.sdk.view.WeemoVideoInFrame`. It must be assigned to the call with `WeemoCall.setVideoIn(view)`.
 * Video OUT: The view must be of type `com.weemo.sdk.view.WeemoVideoOutPreviewFrame`. It must be assigned to the call with `WeemoCall.setVideoOut(view)`.

Note that :
 * For both IN, and OUT, only one view can be used at a time. Setting a new `WeemoVideoInFrame` will cancel the previous `WeemoVideoInFrame`. The same goes for `WeemoVideoOutPreviewFrame`.
 * You cannot send video without setting <b>and displaying</b> a `WeemoVideoOutPreviewFrame`. In Android, the camera can only be accessed through it's preview. A trick to make the preview display disappear is to set it's dimension to the minimum allowed: 2x2. However, if the Android view system stops drawing it, Weemo won't send video OUT.
 * Both views are subclasses of `android.widget.FrameLayout` which means that you can put subviews inside it. Those subviews will appear on top of the video.
 * You <b>must not</b> resize the inner video view of these frame nor try to access it in any way. If you want the video smaller, you must resize the frame.

#### Orientation

The `callActivity` should be landscape only with rotation blocked as it is the simplest way to handle device orientation. If it is the case, you need to call `WeemoVideoInFrame.setDisplayFollowDeviceOrientation(true)` so that the display will be rotated according to the device orientation.

If you do not block rotation in your `callActivity`, then it is your responsibility to handle view rotations.


## Known Limitations
Some features available in the JavaScript API are not currently available in the Android version of the SDK. Such features include:
 * Multi-party calls.
 * Pausing / resuming calls.
 * Screen sharing.

