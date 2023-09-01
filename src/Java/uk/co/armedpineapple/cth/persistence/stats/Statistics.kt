package uk.co.armedpineapple.cth.persistence.stats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "campaignlevels", primaryKeys = ["level", "completed_on"])
data class CampaignLevelCompleted(
    @ColumnInfo(name = "level") val level : Int,

    @ColumnInfo(name = "completed_on") var completedOn: LocalDateTime,
)

@Entity(tableName="countablestats")
data class CountableStat(
    @PrimaryKey val type: String,

    @ColumnInfo var count : Long
)