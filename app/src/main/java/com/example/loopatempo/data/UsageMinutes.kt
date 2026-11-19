package com.example.loopatempo.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "usage_minutes", primaryKeys = ["packageName", "day"])
data class UsageMinutesEntity(
    val packageName: String,
    val day: Long,
    val millis: Long
)

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage_minutes")
    fun observeAll(): Flow<List<UsageMinutesEntity>>

    @Query("SELECT * FROM usage_minutes")
    suspend fun getAll(): List<UsageMinutesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(rows: List<UsageMinutesEntity>)

    @Query("DELETE FROM usage_minutes WHERE packageName = :packageName")
    suspend fun deleteFor(packageName: String)

    @Query("DELETE FROM usage_minutes")
    suspend fun deleteAll()
}

data class AppMinutes(val habit: HabitEntity, val totalMillis: Long)

fun buildAppMinutes(habits: List<HabitEntity>, usage: List<UsageMinutesEntity>): List<AppMinutes> {
    val totals = usage.groupBy { it.packageName }.mapValues { entry -> entry.value.sumOf { it.millis } }
    return habits
        .filter { it.packageName != null }
        .map { AppMinutes(it, totals[it.packageName] ?: 0L) }
        .filter { it.totalMillis > 0L }
        .sortedByDescending { it.totalMillis }
}