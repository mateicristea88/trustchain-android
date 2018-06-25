#!/usr/bin/python

import matplotlib.pyplot as plt
import time
import numpy as np
import sys

# runtime,messagesSent,messagesReceived,introductionRequestsSent,introductionRequestsReceived,introductionResponsesSent,introductionResponsesReceived,puncturesSent,puncturesReceived,punctureRequestsSent,punctureRequestsReceived,blockMessagesSent,blockMessagesReceived,bytesSentCount,bytesReceivedCount,activeConnections,newConnections

x = []
y = []

with open('testdata1.txt', 'r') as datafile:
	header = next(datafile).split(': ')[1].split(',') # Skip line.
	dataIndex = header.index(sys.argv[1])
	for line in datafile:
		data = line.split(': ')[1]
		x.append(int(data.split(',')[0])/1000)
		y.append(int(data.split(',')[dataIndex]))
		# print(data)

plt.plot(x, y)

plt.ylim(ymin=0)
plt.xlim(xmin=0)
plt.xlabel("time (s)")
plt.ylabel(sys.argv[1])

plt.show()
