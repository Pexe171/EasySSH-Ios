package com.easyssh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {
    @Query("SELECT * FROM machine_profiles ORDER BY updatedAt DESC")
    fun observeProfiles(): Flow<List<MachineEntity>>

    @Query("SELECT * FROM machine_profiles WHERE id = :id")
    suspend fun getProfile(id: Long): MachineEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MachineEntity): Long

    @Update
    suspend fun update(entity: MachineEntity)

    @Delete
    suspend fun delete(entity: MachineEntity)

    @Query("UPDATE machine_profiles SET hostKeyFingerprint = :fingerprint, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateHostKeyFingerprint(id: Long, fingerprint: String, updatedAt: Long)
}

