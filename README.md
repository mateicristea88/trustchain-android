TrustChain Android [![Build Status](https://travis-ci.org/klikooo/CS4160-trustchain-android.svg?branch=master)](https://travis-ci.org/klikooo/CS4160-trustchain-android) [![codecov](https://codecov.io/gh/klikooo/CS4160-trustchain-android/branch/master/graph/badge.svg)](https://codecov.io/gh/klikooo/CS4160-trustchain-android)
==================

TrustChain Android is a native Android app implementing the TU Delft style blockchain, called TrustChain. This app provides an accessible way to understand and to use TrustChain. The app is build as part of a Blockchain Engineering course of the TU Delft. It is meant as a basic building block to experiment with blockchain technology. The app also demonstrates a network overlay for peer to peer communication, and a way to communicate with the chip in the Dutch ID-card/passport. This documentation should get you started in the workings of the app, however for thorough understanding, reading other documentation and looking at the source code is a necessity.

We have tried to make the code clear. However, this app was not build by Android experts so please don't hold any mistakes or weird structures for Android against us. Instead, please let us know what could be improved, or provide a fix yourself by submitting a pull request.

Documentation
=============
The documentation for this project can be found at [ReadTheDocs](http://trustchain.readthedocs.org).

Machine Learning
=============
We have added the basic structure of distributed machine learning. For now we take some 2d points, try to fit a line through them. We don't send any data about the specific points to other entities: we create a model, we update it locally (with linear regression) and send the model to a random peer that has its own data points. So far, we do it locally (having issues with using trustchain for sending messages) but we virtually separate our data. 

<img src="https://user-images.githubusercontent.com/11733226/75343056-ea27bc00-5897-11ea-96ee-efca931ed632.png" height="500">

