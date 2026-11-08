package com.example.ivydentalapp.data.repository

import com.example.ivydentalapp.data.local.dao.ServiceDao
import com.example.ivydentalapp.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

class ServiceRepository(private val serviceDao: ServiceDao) {
    suspend fun getActiveServices(): List<ServiceEntity> {
        return serviceDao.getActiveServices()
    }

    suspend fun getServiceById(serviceId: Int): ServiceEntity? {
        return serviceDao.getServiceById(serviceId)
    }

    suspend fun insertService(service: ServiceEntity) {
        serviceDao.insertService(service)
    }

    suspend fun updateService(service: ServiceEntity) {
        serviceDao.updateService(service)
    }

    suspend fun deleteService(serviceId: Int) {
        serviceDao.deleteService(serviceId)
    }

    suspend fun initializeDefaultServices() {
        // Initialize with default services if database is empty
        val existingServices = serviceDao.getActiveServices()
        // Only insert if no services exist
        if (existingServices.isNotEmpty()) {
            return
        }
        
        val defaultServices = listOf(
            ServiceEntity(
                serviceId = 0,
                serviceName = "General Checkup",
                description = "Routine dental examination and cleaning",
                basePrice = 500.0,
                durationMinutes = 30,
                isActive = true
            ),
            ServiceEntity(
                serviceId = 0,
                serviceName = "Teeth Cleaning",
                description = "Professional teeth cleaning and polishing",
                basePrice = 800.0,
                durationMinutes = 45,
                isActive = true
            ),
            ServiceEntity(
                serviceId = 0,
                serviceName = "Extraction",
                description = "Tooth extraction procedure",
                basePrice = 1200.0,
                durationMinutes = 60,
                isActive = true
            ),
            ServiceEntity(
                serviceId = 0,
                serviceName = "Root Canal",
                description = "Root canal treatment",
                basePrice = 5000.0,
                durationMinutes = 90,
                isActive = true
            ),
            ServiceEntity(
                serviceId = 0,
                serviceName = "Braces Consultation",
                description = "Orthodontic consultation and assessment",
                basePrice = 1500.0,
                durationMinutes = 30,
                isActive = true
            ),
            ServiceEntity(
                serviceId = 0,
                serviceName = "Dental X-Ray",
                description = "Diagnostic dental x-ray for treatment planning",
                basePrice = 1000.0,
                durationMinutes = 30,
                isActive = true
            ),
            ServiceEntity(
                serviceId = 0,
                serviceName = "Braces Attachments",
                description = "Placement of braces attachments after required diagnostics",
                basePrice = 8000.0,
                durationMinutes = 60,
                isActive = true
            )
        )
        try {
            serviceDao.insertAll(defaultServices)
        } catch (e: Exception) {
            // Ignore if services already exist (due to race condition)
            android.util.Log.d("ServiceRepository", "Services may already exist", e)
        }
    }
}

