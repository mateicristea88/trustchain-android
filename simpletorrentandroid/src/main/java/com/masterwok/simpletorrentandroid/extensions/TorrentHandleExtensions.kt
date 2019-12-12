package com.masterwok.simpletorrentandroid.extensions

import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import com.masterwok.simpletorrentandroid.models.TorrentSessionBuffer


/**
 * Get the bencode of the [TorrentHandle].
 */
internal fun TorrentHandle.getBencode(): ByteArray = torrentFile()
        ?.bencode()
        ?: ByteArray(0)

/**
 * Get the seeder count of the [TorrentHandle].
 */
internal fun TorrentHandle.getSeederCount(): Int = status().numSeeds()

/**
 * Get the upload rate of the [TorrentHandle] in bytes/second.
 */
internal fun TorrentHandle.getUploadRate(): Int = status().uploadRate()

/**
 * Get the download rate of the [TorrentHandle] in bytes/second.
 */
internal fun TorrentHandle.getDownloadRate(): Int = status().downloadRate()

/**
 * Get the total bytes wanted (to be downloaded) of the [TorrentHandle].
 */
internal fun TorrentHandle.getTotalWanted(): Long = status().totalWanted()

/**
 * Get the total bytes done (downloaded) of the [TorrentHandle].
 */
internal fun TorrentHandle.getTotalDone(): Long = status().totalDone()

/**
 * Get the progress of the [TorrentHandle].
 */
internal fun TorrentHandle.getProgress(): Float = status().progress()

/**
 * Set the priorities of the pieces of the [TorrentHandle] so that pieces are downloaded
 * as close to in-order as possible. For example, let n equal some number less than or
 * equal to [bufferSize] where n represents the number of pieces set to the highest
 * download priority. Each of the n pieces will be a piece between the first non-downloaded,
 * non-ignored piece index + [bufferSize].
 */
internal fun TorrentHandle.setBufferPriorities(
        torrentSessionBuffer: TorrentSessionBuffer
) = setPiecePriorities(
        torrentSessionBuffer.bufferHeadIndex
        , torrentSessionBuffer.bufferTailIndex
)

internal fun TorrentHandle.setPiecePriorities(
        startIndex: Int
        , endIndex: Int
) = (startIndex..endIndex).forEach {
    piecePriority(it, Priority.SEVEN)
    setPieceDeadline(it, 1000)
}
