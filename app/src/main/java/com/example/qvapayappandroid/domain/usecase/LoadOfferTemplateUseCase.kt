package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository

class LoadOfferTemplateUseCase(
    private val offerTemplateRepository: OfferTemplateRepository
) {
    suspend operator fun invoke(id: Long): Result<OfferTemplate?> {
        return try {
            val template = offerTemplateRepository.getTemplateById(id)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getByName(name: String): Result<OfferTemplate?> {
        return try {
            val template = offerTemplateRepository.getTemplateByName(name)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}