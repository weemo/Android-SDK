# Weemo Android SDK and SDK Helper

This repository contains the Weemo Android SDK. More information about Weemo SDKs can be found at [http://www.weemo.com/](http://www.weemo.com/)


## Weemo Android SDK

The use cases have been minimized in order to reduce the integration effort while creating a functional model for the POC that covers most of the use cases for audio and video during 1:1 calls.
Basic implemented functions into this example are: 

- Initialization and Cloud authentification
- Send or Receive a call by UID  
- Stop or Start video
- Mute or Unmute audio
- Support Android multi-tasking 

For implementation details, please refer to the [project wiki](https://github.com/weemo/Android-SDK/wiki) and to the detailed [API Reference](http://docs.weemo.com/sdk/android/) documentation.


## Changelog

[The changelog is available here](CHANGELOG.md).


## Get the library

The libraries are located in the [lib directory](https://github.com/weemo/Android-SDK/tree/master/SDK). Note that this repository is in form of a Android library project. You don't have to use it but it can come in handy if you want to use git submodules.

## SDK Helper

SDK Helper is a sample application implementing the Weemo Android SDK. To demonstrate Weemo Technologies you can follow this quick-tutorial to run a Weemo Application:

[SDK Helper - Quick tutorial](https://github.com/weemo/Android-SDK-Helper)

## About Weemo

The Weemo Video Cloud is a solution specifically targeted at application software vendors providing real-time video communications embedded within any web or mobile application. The solution is particularly well suited for business software applications such as Enterprise Social Networks, CRM, HCM, Customer Service, eHealth, Education and E-learning, as well as for Contact Management and Collaboration.

Weemo relies on standard protocols and open source technologies to deliver a carrier class service. The solution provides constant interoperability with existing or future communication-oriented devices and network infrastructures.

Weemo provides both the client technology and integration means and a worldwide cloud infrastructure. These building blocks are designed to work together as one easy-to-use solution. The application vendor, provider of the web application to be integrated with Weemo, will integrate this SDK to his application to allow audio and video communication.
