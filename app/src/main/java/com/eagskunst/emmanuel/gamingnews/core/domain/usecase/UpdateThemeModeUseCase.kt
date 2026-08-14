package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.ThemeMode
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository

class UpdateThemeModeUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.updateThemeMode(mode)
}
