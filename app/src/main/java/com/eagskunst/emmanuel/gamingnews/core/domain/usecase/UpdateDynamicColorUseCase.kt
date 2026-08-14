package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository

class UpdateDynamicColorUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.updateDynamicColor(enabled)
}
