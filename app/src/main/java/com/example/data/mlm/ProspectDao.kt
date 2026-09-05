package com.example.data.mlm

import androidx.room.*

@Dao
interface ProspectDao {
    @Query("SELECT * FROM network_prospects ORDER BY nextFollowUpTimestamp ASC")
    suspend fun getAllProspects(): List<ProspectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProspect(prospect: ProspectEntity)

    @Update
    suspend fun updateProspect(prospect: ProspectEntity)

    @Delete
    suspend fun deleteProspect(prospect: ProspectEntity)

    @Query("SELECT * FROM network_prospects WHERE stage = :stage")
    suspend fun getProspectsByStage(stage: ProspectStage): List<ProspectEntity>
}
