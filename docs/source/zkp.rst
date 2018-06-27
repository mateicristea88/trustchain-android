.. _zkp:


***********************************************
Claims, attestation and zero knowledge proofs
***********************************************
The idea of trustchain app is to implement claims, attestation and zero knowledge proofs as in the ipv8 python implementation, see https://tools.ietf.org/html/draft-pouwelse-trustchain-01#section-4.2 for more details. Claims and attestation have been implemented in the current trustchain app, however zero knowledge proofs have not because of several problems, which will be explained in the following sections.



Zero knowledge proofs
==================================

Zero knowledge proofs can be used to proof a claim without revealing the true value in this claim. A small example: suppose Alice wants to buy liquor at the liquor store, which can only be bought when you are 18 years or older. However, Alice does not want to reveal her age, but still wants to buy the liquor. This is a situation where a zero knowledge proof is extremely useful. A zero knowledge proof allows Alice to show that her age is in a certain range (this is also known as a range proof), which does not reveal her age.

Implementation
==================================
Three different implementations of zero knowledge proofs have been tried or 

Bulletproof
--------------------
#A well known rule of crypto is: don't implement crypto yourself if you are not an expert. Bulletproofs are zero knowledge proofs which require no trusted setup (https://crypto.stanford.edu/bulletproofs/). There exists a `Java implementation <https://github.com/bbuenz/BulletProofLib>`_, which has been used for this project. However, there were some difficulties to get it to work on Android. The source code of the Bulletproof library is written in Java 1.9, which is not supported by Android. To overcome this issue several parts of the code have successfully been rewritten to Java 1.8. However, Java 1.8 on Android is only supported by Android 8.0 or higher. There exists some tools/libraries to port Java 1.8 byte and/or source code to Java 1.8 byte and/or source code, for example `retrostreams <https://github.com/retrostreams/android-retrostreams>`_.  Unfortunately, these tools/libraries could not port all Java 1.8 features to a lower version of Java. It would be possible to rewrite the Bulletproof library to Java 1.7, but it depends heavily on the `SimpleReact <https://github.com/aol/cyclops-react/wiki/SimpleReact-overview>`_ library which uses Java 1.8 as well. This makes the Bulletproof only suitable for Android devices that run Android Oreo or higher. This is around 5% of all Android devices at the time of writing (https://www.digitaltrends.com/mobile/android-distribution-news/) , so it was chosen to not use the Bulletproof library. 

Next to the Java implementation of the Bulletproof library, there exists a c++ implementation. With JNI (Java Native Interface) it would be possible to use this implementation in the app. However, since all of us have little to no experience with this. little knowledge of the Bulletproof library, and there was not much documentation it was decided to invest time in other methods. 

.. After a few weeks, it was shown that the Bulletproof library is only fast for small numbers (or small in the space footprint).

Ipv8 Android Application
-------------------------
The `ipv8 app <https://github.com/qstokkink/ipv8-android-app/>`_ provides a REST interface for zero knowledge proofs. If this would be used, it means that users should install an additional app (the ipv8 app) to run the trustchain app, which is not desirable. Next to this, building the ipv8 app is not straightforward. The buildscripts use an old gradle experimental format (the plugin com.android.model.application). Building the app without modifying the buildscripts results in build errors on the latest version of Android Studio. These errors can be fixed by changing some things in the buildscript and source code, but ultimately did not build a fully functional app. Because of these two reasons it was decided to not use the ipv8 app.


Other implementations
------------------------
Since the above options were not viable it was decided to look to other implementations of zero knowledge proofs. However, there are not much zero knowledge proof libraries written in Java. The ones that are written in Java miss proper documentation and are not used by others (to our knowledge). Since not the libraries are not used, the security of these libraries is questioned.

It was decided to leave zero knowledge proofs out of the application for now, until a better library comes around.
