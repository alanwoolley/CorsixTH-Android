package uk.co.armedpineapple.cth.persistence

import androidx.room.TypeConverter
import java.time.LocalDateTime


/**
 * Converters for database types
 */
class Converters {

    /**
     * Converts from a timestamp in string format, to a LocalDateTime
     *
     * @param value the timestamp
     * @return An equivalent LocalDateTime
     */
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    /**
     * Converts from a LocalDateTime to a string representation.
     *
     * @param date The LocalDateTime
     * @return A string representing this.
     */
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}