****************
Maturity of Code
****************
This project has had quite a few developers contributing to it. This has caused the codebase to become a mess in the past. After a few major refactors the codebase is much more structured now. This has the added advantage of easier identifying bugs and generally being much easier to understand. Because of the issues lined out in :ref:code-coverage and because writing tests for Android is generally quite time consuming. The code isn't automatically tested as well as would be desired. However, due to pull-based development the quality of the code and the operation of the app is tested by hand quite often by the developers.

So to put a label on it the code `(kinda) works <https://wiki.opencog.org/w/Code_Maturity_Guide>`_. It is definitely not production quality code, however it does work quite well and can demonstrate the possibilities on a small scale. The major bottleneck at the moment is the network overlay. While the TrustChain is scalable, the network overlay has some problems with scalability, limiting the scalability of the app. There are also some problems with the UI when the network is under load, as it can't update the network information.

.. _code-coverage:

Code coverage
=============
It is quite hard to get a good idea of the code coverage for Android projects. This is due to the fact that there are two types of tests for Android. The instrumented (Android) tests and regular Unit tests. The instrumented tests are run on devices and emulators and can make use of the Android framework. Therefore they can be used to tests the UI and other parts which require a device. In our case the usage of the LibSodium cryptographic library requires us to run most cryptography related tests as an instrumented test, because the `library only get's loaded when the app is run on an actual device <https://github.com/joshjdevl/libsodium-jni/issues/95>`_. This is appears to be a bug/feature in Android. In some cases this can be solved by mocking the crypto related objects, however generally it severely limits the ability to write unit tests.

Unfortunately we haven't been able to get codecov to work with the AndroidTests, due to a combination of build errors and difficulties with getting AndroidTests to properly run on Travis. Therefore the codecov report below only reflects part of the coverage that can be done with unit tests. However, it must be said that adding the coverage of the AndroidTests wouldn't spectacularly increase the coverage.

.. _coverage-grid:
.. figure:: https://codecov.io/gh/klikooo/CS4160-trustchain-android/branch/56_tests/graphs/tree.svg
	:width: 400px
	:alt: Coverage Grid

	Coverage Grid, click on the grid (external link) and hover on a block to see which file it is

=========================	=====	===	=======	======	========
Files                    	lines	hit	partial	missed	coverage
=========================	=====	===	=======	======	========
block                    	218  	65	4      	149   	29.82%
chainExplorer            	165  	0	0      	165   	0.00%
crypto                   	106  	0	0      	106   	0.00%
funds                    	323  	0	0      	323   	0.00%
inbox                    	117  	17	3      	97    	14.53%
main                     	462  	5	0      	457   	1.08%
network                  	267  	0	0      	267   	0.00%
offline                  	316  	0	0      	316   	0.00%
passport                 	979  	80	6      	893   	8.17%
peer                     	203  	55	6      	142   	27.09%
peersummary              	289  	0	0      	289   	0.00%
storage                  	291  	0	0      	291   	0.00%
util                    	208  	69	4      	135   	33.17%
message/MessageProto.java	3,637	207	64     	3,366 	5.69%
Project Totals (70 files)	7,581	498	87     	6,996 	6.57%
=========================	=====	===	=======	======	========

Latest coverage table can be found at `codecov <https://codecov.io/gh/klikooo/CS4160-trustchain-android/tree/master/app/src/main/java/nl/tudelft/cs4160/trustchain_android>`_
