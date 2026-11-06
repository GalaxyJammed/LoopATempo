package com.example.loopatempo.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "prefs")
data class PrefEntity(
    @PrimaryKey val prefKey: String,
    val prefValue: String
)

@Dao
interface PrefDao {
    @Query("SELECT prefValue FROM prefs WHERE prefKey = :key")
    fun observe(key: String): Flow<String?>

    @Query("SELECT * FROM prefs")
    suspend fun getAll(): List<PrefEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(pref: PrefEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(prefs: List<PrefEntity>)

    @Query("DELETE FROM prefs")
    suspend fun deleteAll()
}