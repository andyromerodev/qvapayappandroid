package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository

class UpdateOfferTemplateUseCase(
    private val offerTemplateRepository: OfferTemplateRepository
) {
    suspend operator fun invoke(template: OfferTemplate): Result<Unit> {
        return try {
            offerTemplateRepository.updateTemplate(template)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}