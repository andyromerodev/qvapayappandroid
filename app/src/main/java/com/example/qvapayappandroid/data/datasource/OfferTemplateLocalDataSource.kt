package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.database.entities.OfferTemplateEntity
import kotlinx.coroutines.flow.Flow

interface OfferTemplateLocalDataSource {
    
    fun getAllTemplates(): Flow<List<OfferTemplateEntity>>
    
    suspend fun getTemplateById(id: Long): OfferTemplateEntity?
    
    suspend fun getTemplateByName(name: String): OfferTemplateEntity?
    
    fun getTemplatesByType(type: String): Flow<List<OfferTemplateEntity>>
    
    suspend fun saveTemplate(template: OfferTemplateEntity): Long
    
    suspend fun updateTemplate(template: OfferTemplateEntity)
    
    suspend fun deleteTemplate(template: OfferTemplateEntity)
    
    suspend fun deleteTemplateById(id: Long)
    
    suspend fun deleteAllTemplates()
    
    suspend fun getTemplateCount(): Int
    
    fun searchTemplates(searchQuery: String): Flow<List<OfferTemplateEntity>>
}