# Weemo Android SDK Changelog


## 4.2.0

**`18/11/2013`**


### New Features

- **SDK**: Addition of two optional methods to improve batteries life: `goToBackground` and `goToForeground`.
- **Helper**: New Helper optimized for 10 inches tablets (the call window is now a fragment).


### Improvements

- **SDK:** Removed `activityStart` and `activityStop`.
- **SDK:** `VideoOutPreviewFrame` now uses device orientation
- **SDK:** Camera do not stop on background when the device supports it
- **SDK:** `destroy` is replaced by `disconnect`


## 4.2.1

**`19/12/2013`**


### New Features

 - **SDK:** Added `AuthenticatedEvent.Error.BAD_USERID` fired when trying to authenticate with a UID that does not comply with the [Weemo naming rule](https://github.com/weemo/Release-4.x/wiki/WeemoDriver-Naming#uid)


### Improvements

- **SDK:** Both video views are now backed by `TextureView` instead of `SurfaceView`, which makes them more stable and integrable into the Android view system.
- **SDK:** Various optimizations and stability fixes.
- **Helper:** Various stability fixes.


### Known issues


- ***MS-8***  
  **Description:** No Display Name sent during a call creation.  
  **Workaround:** Manage DisplayName by Host application


- ***MS-11***  
  **Description:** Connecting a new device with same credentials force the authentication, the first device could not be contacted  
  **Workaround:** Manage accounts by host application

