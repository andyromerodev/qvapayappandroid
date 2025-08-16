package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.datasource.OfferTemplateLocalDataSource
import com.example.qvapayappandroid.data.database.entities.OfferTemplateEntity
import com.example.qvapayappandroid.data.model.P2PDetail
import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OfferTemplateRepositoryImpl(
    private val localDataSource: OfferTemplateLocalDataSource
) : OfferTemplateRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getAllTemplates(): Flow<List<OfferTemplate>> {
        return localDataSource.getAllTemplates().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getTemplateById(id: Long): OfferTemplate? {
        return localDataSource.getTemplateById(id)?.toDomainModel()
    }
    
    override suspend fun getTemplateByName(name: String): OfferTemplate? {
        return localDataSource.getTemplateByName(name)?.toDomainModel()
    }
    
    override fun getTemplatesByType(type: String): Flow<List<OfferTemplate>> {
        return localDataSource.getTemplatesByType(type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun saveTemplate(template: OfferTemplate): Long {
        return localDataSource.saveTemplate(template.toEntity())
    }
    
    override suspend fun updateTemplate(template: OfferTemplate) {
        localDataSource.updateTemplate(template.toEntity())
    }
    
    override suspend fun deleteTemplate(template: OfferTemplate) {
        localDataSource.deleteTemplate(template.toEntity())
    }
    
    override suspend fun deleteTemplateById(id: Long) {
        localDataSource.deleteTemplateById(id)
    }
    
    override suspend fun deleteAllTemplates() {
        localDataSource.deleteAllTemplates()
    }
    
    override suspend fun getTemplateCount(): Int {
        return localDataSource.getTemplateCount()
    }
    
    override fun searchTemplates(searchQuery: String): Flow<List<OfferTemplate>> {
        return localDataSource.searchTemplates(searchQuery).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    private fun OfferTemplateEntity.toDomainModel(): OfferTemplate {
        val details = try {
            json.decodeFromString<List<P2PDetail>>(detailsJson)
        } catch (e: Exception) {
            emptyList()
        }
        
        return OfferTemplate(
            id = id,
            name = name,
            description = description,
            type = type,
            coinId = coinId,
            coinName = coinName,
            coinTick = coinTick,
            amount = amount,
            receive = receive,
            details = details,
            onlyKyc = onlyKyc,
            private = isPrivate,
            promoteOffer = promoteOffer,
            onlyVip = onlyVip,
            message = message,
            webhook = webhook,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun OfferTemplate.toEntity(): OfferTemplateEntity {
        val detailsJson = json.encodeToString(details)
        
        return OfferTemplateEntity(
            id = id,
            name = name,
            description = description,
            type = type,
            coinId = coinId,
            coinName = coinName,
            coinTick = coinTick,
            amount = amount,
            receive = receive,
            detailsJson = detailsJson,
            onlyKyc = onlyKyc,
            isPrivate = private,
            promoteOffer = promoteOffer,
            onlyVip = onlyVip,
            message = message,
            webhook = webhook,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}