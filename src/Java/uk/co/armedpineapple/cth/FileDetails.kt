package uk.co.armedpineapple.cth

import java.util.*

data class FileDetails(val fileName: String, val directory: String, private val lastModified: Date) : Comparable<FileDetails> {

    override fun compareTo(other: FileDetails): Int {
        if (lastModified == other.lastModified) {
            return 0
        }

        return if (lastModified.after(other.lastModified)) 1 else -1

    }
}
