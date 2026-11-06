package com.example.loopatempo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAll(): List<HabitEntity>

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Insert
    suspend fun insertAll(habits: List<HabitEntity>)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()
}

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins")
    fun observeAll(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE day = :day")
    fun observeForDay(day: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins")
    suspend fun getAll(): List<CheckInEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(checkIns: List<CheckInEntity>)

    @Query("DELETE FROM check_ins WHERE habitId = :habitId AND day = :day")
    suspend fun delete(habitId: Long, day: Long)

    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 0")
    fun observe(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: SettingsEntity)

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}