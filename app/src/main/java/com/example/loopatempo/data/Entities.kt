package com.example.loopatempo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val packageName: String?,
    val thresholdMinutes: Int?,
    val createdAt: Long,
    val iconKey: String? = null,
    val reminderMinutes: Int? = null
)

@Entity(
    tableName = "check_ins",
    primaryKeys = ["habitId", "day"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class CheckInEntity(
    val habitId: Long,
    val day: Long
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val nickname: String = "",
    val defaultThresholdMinutes: Int = 5,
    val darkTheme: Boolean = true,
    @ColumnInfo(defaultValue = "0") val notificationsEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0") val keepAlive: Boolean = true,
    @ColumnInfo(defaultValue = "1") val reminderEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "1200") val reminderMinutes: Int = 20 * 60
)