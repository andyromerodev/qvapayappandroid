package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository

class SaveOfferTemplateUseCase(
    private val offerTemplateRepository: OfferTemplateRepository
) {
    suspend operator fun invoke(template: OfferTemplate): Result<Long> {
        return try {
            val id = offerTemplateRepository.saveTemplate(template)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}