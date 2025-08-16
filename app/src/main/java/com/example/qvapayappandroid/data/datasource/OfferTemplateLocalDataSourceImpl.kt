package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.database.dao.OfferTemplateDao
import com.example.qvapayappandroid.data.database.entities.OfferTemplateEntity
import kotlinx.coroutines.flow.Flow

class OfferTemplateLocalDataSourceImpl(
    private val offerTemplateDao: OfferTemplateDao
) : OfferTemplateLocalDataSource {
    
    override fun getAllTemplates(): Flow<List<OfferTemplateEntity>> {
        return offerTemplateDao.getAllTemplates()
    }
    
    override suspend fun getTemplateById(id: Long): OfferTemplateEntity? {
        return offerTemplateDao.getTemplateById(id)
    }
    
    override suspend fun getTemplateByName(name: String): OfferTemplateEntity? {
        return offerTemplateDao.getTemplateByName(name)
    }
    
    override fun getTemplatesByType(type: String): Flow<List<OfferTemplateEntity>> {
        return offerTemplateDao.getTemplatesByType(type)
    }
    
    override suspend fun saveTemplate(template: OfferTemplateEntity): Long {
        return offerTemplateDao.insertTemplate(template)
    }
    
    override suspend fun updateTemplate(template: OfferTemplateEntity) {
        offerTemplateDao.updateTemplate(template.copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun deleteTemplate(template: OfferTemplateEntity) {
        offerTemplateDao.deleteTemplate(template)
    }
    
    override suspend fun deleteTemplateById(id: Long) {
        offerTemplateDao.deleteTemplateById(id)
    }
    
    override suspend fun deleteAllTemplates() {
        offerTemplateDao.deleteAllTemplates()
    }
    
    override suspend fun getTemplateCount(): Int {
        return offerTemplateDao.getTemplateCount()
    }
    
    override fun searchTemplates(searchQuery: String): Flow<List<OfferTemplateEntity>> {
        return offerTemplateDao.searchTemplates(searchQuery)
    }
}