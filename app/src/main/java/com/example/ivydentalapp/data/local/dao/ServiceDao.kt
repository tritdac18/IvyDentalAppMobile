package com.example.ivydentalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ivydentalapp.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE is_active = 1 ORDER BY service_name ASC")
    suspend fun getActiveServices(): List<ServiceEntity>

    @Query("SELECT * FROM services ORDER BY service_name ASC")
    suspend fun getAllServices(): List<ServiceEntity>
    
    @Query("SELECT * FROM services ORDER BY service_name ASC")
    suspend fun getAllServicesSync(): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE service_id = :serviceId")
    suspend fun getServiceById(serviceId: Int): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<ServiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @androidx.room.Update
    suspend fun updateService(service: ServiceEntity)

    @Query("DELETE FROM services WHERE service_id = :serviceId")
    suspend fun deleteService(serviceId: Int)
}

