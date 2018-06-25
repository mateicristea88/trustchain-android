#!/usr/bin/python

import matplotlib.pyplot as plt
import time
import numpy as np
import sys
import math

# runtime,messagesSent,messagesReceived,introductionRequestsSent,introductionRequestsReceived,
# introductionResponsesSent,introductionResponsesReceived,puncturesSent,puncturesReceived,
# punctureRequestsSent,punctureRequestsReceived,blockMessagesSent,blockMessagesReceived,
# bytesSentCount,bytesReceivedCount,activeConnections,newConnections

time = []

with open('testdata1.txt', 'r') as datafile:
	header = next(datafile).split(': ')[1].rstrip().split(',')
	lines = datafile.readlines()
	data = np.zeros(shape=(len(lines),len(sys.argv[1:])))

	dataIndices = []
	# Find indices for the requested data
	for word in sys.argv[1:]:
		dataIndices.append(header.index(word))

	# Fill data arrays
	for i in range(0, len(lines)):
		line = lines[i]
		datapoints = line.split(': ')[1]
		time.append(int(datapoints.split(',')[0])/1000)
		for j in range(0, len(dataIndices)):
			index = dataIndices[j]
			data[i][j] = (int(datapoints.split(',')[index]))

fig, ax = plt.subplots()

# Make plot for each argument
for i in range(0, len(sys.argv[1:])):
	name = sys.argv[1:][i]
	ax.plot(time, data[:,i], '.-', label=name)

# Set some sane x and y axis ticks
starty, endy = ax.get_ylim()
firstdigit = data.max() // 10 ** int(math.log(data.max(), 10))
num_zeros = len(str(int(data.max()))) - 1
maxnum_rounded = firstdigit * (10 ** num_zeros)
ax.yaxis.set_ticks(np.arange(0, endy, maxnum_rounded/8))

startx, endx = ax.get_xlim()
firstdigit = max(time) // 10 ** int(math.log(max(time), 10))
num_zeros = len(str(int(max(time)))) - 1
maxnum_rounded = firstdigit * (10 ** num_zeros)
ax.xaxis.set_ticks(np.arange(0, endx, maxnum_rounded/8))

plt.legend(loc='upper center', shadow=False, fontsize='large')
plt.ylim(ymin=0)
plt.xlim(xmin=0)
plt.xlabel("time (s)")
plt.ylabel(sys.argv[1])

plt.show()
