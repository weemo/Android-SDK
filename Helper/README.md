### Overview

This project was made with Eclipse 



### Step 1 - Check Android plug-in

Make sure you have Android plug-in install with Eclipse

### Step 2 - Install Findbugs Eclipse plug-in



### Step 3 - Import the project in Eclipse 

Import the Helper project in Eclipse as an "Existing Android Project"


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
