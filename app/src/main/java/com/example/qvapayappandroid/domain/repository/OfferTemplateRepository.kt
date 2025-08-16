package com.example.qvapayappandroid.domain.repository

import com.example.qvapayappandroid.domain.model.OfferTemplate
import kotlinx.coroutines.flow.Flow

interface OfferTemplateRepository {
    
    fun getAllTemplates(): Flow<List<OfferTemplate>>
    
    suspend fun getTemplateById(id: Long): OfferTemplate?
    
    suspend fun getTemplateByName(name: String): OfferTemplate?
    
    fun getTemplatesByType(type: String): Flow<List<OfferTemplate>>
    
    suspend fun saveTemplate(template: OfferTemplate): Long
    
    suspend fun updateTemplate(template: OfferTemplate)
    
    suspend fun deleteTemplate(template: OfferTemplate)
    
    suspend fun deleteTemplateById(id: Long)
    
    suspend fun deleteAllTemplates()
    
    suspend fun getTemplateCount(): Int
    
    fun searchTemplates(searchQuery: String): Flow<List<OfferTemplate>>
}