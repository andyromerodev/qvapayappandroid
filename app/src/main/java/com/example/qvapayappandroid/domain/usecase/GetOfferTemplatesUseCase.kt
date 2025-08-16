package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository
import kotlinx.coroutines.flow.Flow

class GetOfferTemplatesUseCase(
    private val offerTemplateRepository: OfferTemplateRepository
) {
    operator fun invoke(): Flow<List<OfferTemplate>> {
        return offerTemplateRepository.getAllTemplates()
    }
    
    fun getByType(type: String): Flow<List<OfferTemplate>> {
        return offerTemplateRepository.getTemplatesByType(type)
    }
    
    fun search(query: String): Flow<List<OfferTemplate>> {
        return offerTemplateRepository.searchTemplates(query)
    }
}