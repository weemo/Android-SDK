# Weemo Android SDK Changelog


## 5.2.544 **`Aug 13 2014`**

### New Features

#### SDK
- Centralized method to get the statyus of the WeemoEngine `Weemo.getEngineStatus()`
- Start or Resume a call without video
- DataChannel available to send/reply data
- Screen Share In has its own View: `WeemoScreenShareInFrame`
- Video In scale mode: Fit/Crop
- SIP id is delivered through `CallSipIdEvent`
- Introduce `WeemoContact` objects
- New APIs to detach/remove views
- Simplification in the Authentication process and events
- Event fired when the outcoming video size changes
- Flash / Auto-Focus settings
- Detect WebRTC calls with `WeemoCall.isWebRTC()`

#### Helper
- Button to change the Video scale mode
- Button to send a message through DataChannel
- Received messages are displayed and can be replied
- The Screen Share In is displayed below the Call View
- Token fetching mechanism

### Improvements

#### SDK
- **Enh:** Fail Fast approach
- **Enh:** EventBus improvements, dead events detection
- **Enh:** Improved bitmap fetcher
- **Enh:** Improved the mechanism of reconnection during connectivity issues
- **Enh:** A WeemoCall can be either outbound or inbound
- **Fix:** Potential deadlock in the connection/authentication process

#### Helper
- **Fix:** The Floating Call Window stays inside its container
- **Enh:** The ongoing notification of a call is displaying the call duration
- **Enh:** Simplification of the CallFragment, split responsibilities into separate controllers

### Deprecation
- use `CallReceivingVideoChangedEvent` instead of `ReceivingVideoChangedEvent`
- use `CallReceivingScreenShareChangedEvent` instead of `ReceivingScreenShareChangedEvent`
- use `WeemoVideoInFrame.setOnTouchListener(OnWeemoVideoInFrameTouchListener)` instead of `WeemoVideoInFrame.setOnVideoInFrameTouchListener(OnWeemoVideoInFrameTouchListener)`
- use `WeemoCall.getContactDisplayName(int)` instead of `WeemoCall.getContactDisplayName()`
- use `WeemoCall.getVideoIn(int)` instead of `WeemoCall.getVideoIn()`
- use `WeemoCall.getVideoInProfile(int)` instead of `WeemoCall.getVideoInProfile()`
- use `WeemoCall.getVideoInSize(int)` instead of `WeemoCall.getVideoInSize()`
- use `WeemoCall.setVideoIn(WeemoVideoInFrame, int)` instead of `WeemoCall.setVideoIn(WeemoVideoInFrame)`

- use `AuthenticatedEvent` instead of `CanCreateCallChangedEvent`
- use `WeemoEngine.disconnect()` instead of `Weemo.disconnect()`
- use `Weemo.getEngineStatus() == Status.AUTHENTICATED` instead of `WeemoEngine.canCreateCall()`
- use `Weemo.getEngineStatus() == Status.AUTHENTICATED` instead of `WeemoEngine.isAuthenticated()`


## 5.1.366 **`Jun 27 2014`**

### New Features

#### SDK
- ScreenShare In
- ScreenShare Out of any View
- PSTN calls interruption
- Call duration available
- Permissions are now required at `WeemoEngine.initialize()` time. If a permission is missing, the app will be notified with a custom RuntimeException.
- Advanced video encoding parameters can be modified with `WeemoEngine.overrideEncodingParametersAdvanced(int, int, int)`
- Event fired when the incoming video or screenshare size changes
- Audio route is part of the SDK

#### Helper
- Full UI & UX update
- Call View can be either in fullscreen or in floating mode
- Call View can be dragged when being in floating mode
- Call control clicks can be listened
- Call duration is displayed when a call ends
- Notification with quick actions for incoming and ongoing calls
- Quick actions can be triggered on the main screen (retry a `WeemoEngine.getStatus(String)`, or call a contact if he/she is available)
- User can logout and login again without exiting the helper
- The AppID is now at the root of the Helper, inside `AndroidManifest.xml` file
- A Webview can be shared using the ScreenShare API

### Improvements

#### SDK
- **Fix:** Video thread can be stopped too late and cause errors on edge cases
- **Enh:** SDK refactoring
- **Enh:** Switching the audio out route is now part of the SDK and can be switched with `WeemoCall.setAudioRoute(AudioRoute)`

#### Helper
- **Enh:** Helper refactoring
- **Enh:** Call controls have a state: normal/activated
- **Enh:** Fullscreen/Floating button instead of HD/SD
- **Enh:** Remove unused drawables
- **Enh:** Javadoc available as a jar file

### Deprecation
- use `WeemoEngine.authenticate(Context, String, UserType)` instead of `WeemoEngine.authenticate(String, UserType, int)`
- use `WeemoCall.audioStop()` instead of `WeemoCall.audioMute()`
- use `WeemoCall.audioStart()` instead of `WeemoCall.audioUnMute()`
- use `CallReceivingVideoChangedEvent` instead of `ReceivingVideoChangedEvent` class


## 5.1.289 **`May 20 2014`**

### New Features

#### SDK
- Bitmap filtering can be switched on/off to improve video quality with `WeemoCall.setVideoInFilterBitmap(boolean)`
- Max fps, bitrate and downscaling can be modified with `WeemoEngine.overrideEncodingParameters(int, int, int)`

### Improvements

#### Helper
- **Fix:** Call button is re-enabled after a network lost
- **Enh:** Call controls are hidden when the call starts



## 5.1.285 **`May 14 2014`**

### New Features

#### SDK
- OSS Data
- Internal Log level can be modified with `Weemo.setLogLevel(int)`

#### Helper
- Background mode is triggered by a countdown timer

### Improvements

#### SDK
- **Fix:** Video In won't start on some edge cases
- **Enh:** Improved SDK instrumentation

#### Helper
- **Fix:** Background behaviour
- **Enh:** Popups wording

### Deprecation
- Removed `Weemo.getVersion()`, use `Weemo.getVersionFull(Context)` instead



## 5.1.279 **`May 13 2014`**

### New Features

#### SDK
- Support for WebRTC
- Internal Log mechanism for better instrumentation

#### Helper
- New CallView, with speakers and SD/HD buttons
- An icomming call will wake the device up
- The Report mechanism detect if it's a Crash or a Report

### Improvements

#### SDK
- **Fix:** Issues when the application is in background
- **Fix:** The initial orientation of the CallView is now correct
- **Fix:** The CallView could remains black
- **Enh:** The SDK is more friendly with Garbage Collector
- **Enh:** Updated documentation

#### Helper
- **Fix:** Reconnection Popup can no longer stack on top of each others `MS-73`
- **Enh:** Add a ring tone in Android SDK Helper `MS-5`
- **Enh:** Update CallView interface

### Known issues

- ***MS-13***  
  **Description:** Crash when flipping video source on/off
  **Workaround:**  Fixed in Helper

- ***MS-15***  
  **Description:** No video when the device is laid flat on the table 
  **Workaround:** Removing alpha channel used by Bitmaps

- ***MS-26***  
  **Description:** Change camera button should be disabled when device has only one camera 
  **Workaround:** The Host application manage the visibility of the buttons

- ***MS-68***
  **Description:** Crash while orientation changes during a call
  **Workaround:** Fixed in SDK



## 4.2.1 **`Dec 19 2013`**

### New Features

#### SDK
 - Added `AuthenticatedEvent.Error.BAD_USERID` fired when trying to authenticate with a UID that does not comply with the [Weemo naming rule](https://github.com/weemo/Release-4.x/wiki/WeemoDriver-Naming#uid)

### Improvements

#### SDK
- Both video views are now backed by `TextureView` instead of `SurfaceView`, which makes them more stable and integrable into the Android view system.
- Various optimizations and stability fixes.

#### Helper
- Various stability fixes.

### Known issues

- ***MS-8***  
  **Description:** No Display Name sent during a call creation.  
  **Workaround:** Manage DisplayName by Host application


- ***MS-11***  
  **Description:** Connecting a new device with same credentials force the authentication, the first device could not be contacted  
  **Workaround:** Manage accounts by host application



## 4.2.0 **`Nov 18 2013`**

### New Features

#### SDK
- Addition of two optional methods to improve battery life: `goToBackground` and `goToForeground`.

#### Helper
- New Helper optimized for 10-inch tablets (the call window is now a fragment).


### Improvements

#### SDK
- Removed `activityStart` and `activityStop`.
- `VideoOutPreviewFrame` now uses device orientation
- Camera does not stop on background when the device supports it
- `destroy` is replaced by `disconnect`
