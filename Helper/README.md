### Overview

This project was made with Eclipse, to be sure of the compatibility of the SDK Helper we highly recommand to use this IDE.   



### Step 1 - Check Android Developement Tools

Make sure you have Android plug-in install with Eclipse.

Android Development Tools (ADT) is a plugin for the Eclipse IDE that is designed to give you a powerful, integrated environment in which to build Android applications.

ADT extends the capabilities of Eclipse to let you quickly set up new Android projects, create an application UI, add packages based on the Android Framework API, debug your applications using the Android SDK tools, and even export signed (or unsigned) .apk files in order to distribute your application.

If you need to install ADT, please check the [official ADT website](http://developer.android.com/sdk/installing/installing-adt.html)


### Step 2 - Install Findbugs Eclipse plug-in

FindBugs™ is a program to find bugs in Java programs. It looks for instances of "bug patterns" --- code instances that are likely to be errors.

If you don't have Findbugs on your Eclipse, please download-it and intall-it. 

Follow instruction on projcet website:  [http://findbugs.sourceforge.net/](http://findbugs.sourceforge.net/)


### Step 3 - Import the project in Eclipse 

Import the Helper project in Eclipse as an "Existing Android Project"

<p align="center">
<img src="http://docs.weemo.com/img/android_import.png">

</p>

### Step 4 -  Configure your mobileAppId 

To configure you mobileWebId regarding the configuration of your Profile in Weemo Portal, please edit the configuration file: 
/res/values/weemo_conf.xml

Replace your "ENTER YOUR KEY HERE" by your mobileAppId provided by Weemo:

```
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools" tools:ignore="TypographyEllipsis">

    <string name="weemo_mobileAppId">ENTER YOUR KEY HERE</string>
    
</resources>
```

### Step 5 - Run the project (cannot run in an emulator)
