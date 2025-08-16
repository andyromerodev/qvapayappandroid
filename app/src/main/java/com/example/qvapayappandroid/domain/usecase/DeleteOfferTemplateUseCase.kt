package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository

class DeleteOfferTemplateUseCase(
    private val offerTemplateRepository: OfferTemplateRepository
) {
    suspend operator fun invoke(template: OfferTemplate): Result<Unit> {
        return try {
            offerTemplateRepository.deleteTemplate(template)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteById(id: Long): Result<Unit> {
        return try {
            offerTemplateRepository.deleteTemplateById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}