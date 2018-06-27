#!/usr/bin/python

import matplotlib.pyplot as plt
import time
import numpy as np
import sys
import math
from functools import reduce

FILE_NAME = '25nodes-lg_g4-720sec.txt'
LEGEND_LOC = 'upper center'

# runtime,messagesSent,messagesReceived,introductionRequestsSent,introductionRequestsReceived,
# introductionResponsesSent,introductionResponsesReceived,puncturesSent,puncturesReceived,
# punctureRequestsSent,punctureRequestsReceived,blockMessagesSent,blockMessagesReceived,
# bytesSentCount,bytesReceivedCount,activeConnections,newConnections

time = []
def tally(acc, line):
	if 'stress_test_user_0' in line:
		return acc + 1
	else:
		return acc

with open(FILE_NAME, 'r') as datafile:
	header = next(datafile).split(': ')[1].rstrip().split(',')
	lines = datafile.readlines()
	entries = reduce(tally, lines, 0)
	data = np.zeros(shape=(entries,len(sys.argv[1:])))

	dataIndices = []
	# Find indices for the requested data
	for word in sys.argv[1:]:
		dataIndices.append(header.index(word))

	# Fill data arrays
	datamap_arr = {}
	for i in range(0, len(lines)):
		line = lines[i].split(' I/')[1]
		datapoints = line.split(': ')[1]
		name = line.split(': ')[0]
		if 'stress_test_user_0' in name:
			time.append(int(datapoints.split(',')[0])/1000)
		for j in range(0, len(dataIndices)):
			index = dataIndices[j]
			# data[i][j] = int(datapoints.split(',')[index])
			if not name in datamap_arr.keys():
				datamap_arr[name] = np.zeros(shape=(entries,len(sys.argv[1:])))
			datamap_arr[name][len(time)-1][j] = int(datapoints.split(',')[index])

f, (ax1, ax2) = plt.subplots(2, sharex=True, sharey=False)
ax1.set_title('Averages')
ax2.set_title('Standard Deviations')
for i in range(0, len(sys.argv[1:])):
	deviations = []
	averages = []
	data_arr = []
	for name in datamap_arr.keys():
		data_arr.append(datamap_arr[name][:,i])

	for j in range(0, len(data_arr[0])):
		entrylist = []
		for k in range(0, len(datamap_arr.keys())):
			entrylist.append(data_arr[k][j])

		deviations.append(np.std(entrylist, ddof=1))
		averages.append(np.average(entrylist))

	# Make plot for each argument
	name = sys.argv[1:][i]
	ax2.plot(time, deviations, '.-', label=name)
	ax1.plot(time, averages, '.-', label=name)

ax1.legend(loc=LEGEND_LOC, shadow=False, fontsize='large')
plt.ylim(ymin=0)
plt.xlim(xmin=0)
plt.xlabel("time (s)")
plt.ylabel("number")

plt.show()
