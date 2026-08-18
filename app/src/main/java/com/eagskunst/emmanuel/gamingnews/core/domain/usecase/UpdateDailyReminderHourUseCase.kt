package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository

class UpdateDailyReminderHourUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(hour: Int) = repository.updateDailyReminderHour(hour)
}
