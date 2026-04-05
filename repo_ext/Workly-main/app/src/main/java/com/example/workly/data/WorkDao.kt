package com.example.workly.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDao {
    @Insert
    suspend fun insert(workLog: WorkLog)

    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Delete
    suspend fun delete(workLog: WorkLog)
}
