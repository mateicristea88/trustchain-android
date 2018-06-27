*************************
Installation Instructions
*************************
If you want to quickly test the basic version of TrustChain Android follow :ref:`installing-apk`, if you want to create a version of TrustChain Android with your own features follow :ref:`setting-up-project`.

.. _installing-apk:

Installing TrustChainAndroid APK
================================
The easiest way to install the app is through `Google Play <https://play.google.com/store/apps/details?id=nl.tudelft.cs4160.trustchain_android>`_.
If you want to install the :repo:`latest release <releases>` from github, download the apk attached to the release to your phone and open it to install. The current minimum version of Android needed is 21 (5.0), however not all features will be available to this version. The recommended version is 26 (8.0) or higher. To reset the apps settings (refreshing the public key) and removing the local chain, simply clear all the data of the app in the app settings.

.. _setting-up-project:

Setting up the Android Project
==============================
Follow the steps below if you want to make alterations to the project and add your own functions. If you are already familiar with developing Android native apps and GitHub, this will be trivial.

* `Download and install <https://developer.android.com/studio/index.html>`_ Android Studio
* Make yourself familiar with how Android projects are set up, by reading the `Android Getting Started Guide <https://developer.android.com/training/index.html>`_

	* Note that the guide makes use of the Layout Editor, however writing the xml files directly will give you much better control

* Clone the repository to your work station ``git clone https://github.com/wkmeijer/CS4160-trustchain-android.git``
* Import the project in Android Studio by ``File>Open`` and search for the cloned repository
* Start editing

Note that connecting to an emulator will often not work, so for proper testing you will need two phones.
