package de.robolab.client.utils

import de.robolab.client.updater.Downloader
import java.io.File
import java.io.InputStream
import java.util.*

class ProgressFileReader(
    private val file: File,
    private val listener: Downloader.ProgressListener
) {

    fun lineSequence(): Sequence<String> {
        val contentLength = file.length()

        val progressStream = ProgressInputStream(file.inputStream().buffered(DEFAULT_BUFFER_SIZE * 1024))

        return ProgressLinesSequence(progressStream, contentLength, listener)
    }

    private class ProgressLinesSequence(
        private val stream: ProgressInputStream,
        private val contentLength: Long,
        private val listener: Downloader.ProgressListener
    ) : Sequence<String> {

        private val reader = stream.bufferedReader()

        override fun iterator(): Iterator<String> {
            return object : Iterator<String> {
                private var nextValue: String? = null
                private var done = false

                override fun hasNext(): Boolean {
                    if (nextValue == null && !done) {
                        nextValue = reader.readLine()
                        if (nextValue == null) done = true

                        listener.update(stream.bytesRead, contentLength, done)
                    }
                    return nextValue != null
                }

                override fun next(): String {
                    if (!hasNext()) {
                        throw NoSuchElementException()
                    }
                    val answer = nextValue
                    nextValue = null
                    return answer!!
                }
            }
        }
    }

    class ProgressInputStream(
        val source: InputStream,
    ) : InputStream() {

        private var index = 0L

        val bytesRead: Long
            get() = index

        override fun read(): Int {
            index += 1
            return source.read()
        }
    }
}

fun File.printProgressReader(): ProgressFileReader {
    var lastProgress = -1L
    return ProgressFileReader(this) { bytesRead, contentLength, done ->
        if (done) {
            println("Progress: Finished!")
        } else {
            val progress = bytesRead * 100 / contentLength
            if (progress != lastProgress) {
                lastProgress = progress
                println("Progress: $progress%")
            }
        }
    }
}

fun File.progressReader(progressListener: Downloader.ProgressListener): ProgressFileReader {
    return ProgressFileReader(this, progressListener)
}
