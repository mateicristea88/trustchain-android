Stress testing and statistics
*****************************
The stress testing feature allows to spin up any desired number of nodes. These nodes start in the :base-repo:`StressTestPeer <stresstest/StressTestPeer.java>` class, they use their own instances of the `Network` class and provide the `StressTestNode` instance as a PeerListener, instead of the normal `OverviewconnectionActivity`.
They act just like a normal node, except having no visual displays and generating a new temporary keypair.

Statistics
==========
The Network class logs all its sent and received messages into the singleton :base-repo:`StatisticsServer <statistics/StatisticsServer.java>` class. Messages are logged by type and separate statistics are kept for al running nodes. Additional logged data is the number of sent and received bytes and the number of active and new connections.
All this data is tallied and shown on the StressTestActivity, updated periodically.

- Statistics are NOT displayed on phones running API 23 or less, as tallying uses a reduce function not available on API 23 or less.
- Note that running many nodes may slow the phone down dramatically and statistic updates may come in extremely slowly or not at all.

Logging and graph generation
-----------------------------
The StatisticsServer class runs a logging task that prints all statistics, in csv format, to the console at a fixed time interval.
This csv data can be used to generate graphs to visually show the data using generategraph.py from the python folder, using the following steps:

1. Get data of the desired node by filtering the log output on ``Statistics-<desired username>``
2. Copy the data to a text file in the python folder (the timestamps can be left in) and change the filename in the python script
3. Run ``python generategraph.py <column name>`` to generate a graph for the given node and column

example: ``python generategraph.py messagesSent messagesReceived``

In order to take all nodes into account generategraph_aggregate.py is provided.
This script takes a data file that contains the logs of an arbitrary number of nodes, and calculates the averages and standard deviations of the requested columns.

- Please note that this script is more hacky than generategraph.py, may contain bugs and does not offer custom ticks on the x and y axis.
- Please also note that this script has 'stress_test_user_0' hardcoded as node name and (ab)uses this on order to fill the array of x values. In order for this script to work reliably, the hardcoded username value should always be the first log in each series of log updates.
- Make sure that the resulting file used to create the graph contains only one header line containing the column names.

The steps to create graphs are the same as above, except that the recommended filter is 'Statistics-stress_test' in order to filter out the main node (including it will cause some issues since this node is started much earlier than the rest)

Links to code
=============

* :base-repo:`Statistics package <statistics>`
* :base-repo:`Stress testing package <stresstest>`
* :repo:`Python scripts <tree/master/python>`

