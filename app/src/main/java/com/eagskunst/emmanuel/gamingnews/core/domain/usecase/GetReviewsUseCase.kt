package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredReviewFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteContext
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteFeedFilter
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteRule
import com.eagskunst.emmanuel.gamingnews.core.domain.model.RulesLoad
import com.eagskunst.emmanuel.gamingnews.core.domain.model.asRulesLoad
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.ReviewsRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

/**
 * Streams the reviews feed filtered by the search query and — only when the user opted in via
 * `applyGlobalMuteRulesToReviews` — the global (Everywhere) mute rules. Selected-tab rules never
 * apply to Reviews, and failed-source metadata is preserved untouched.
 */
class GetReviewsUseCase(
    private val repository: ReviewsRepository,
    private val muteRulesRepository: MuteRulesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dispatchers: DispatcherProvider
) {
    operator fun invoke(
        forceRefresh: Boolean = false,
        searchQuery: Flow<String> = flowOf(""),
        revealMuted: Flow<Boolean> = flowOf(false)
    ): Flow<Result<FilteredReviewFeed>> = combine(
        repository.reviewsStream(forceRefresh),
        muteRulesRepository.observeRules().asRulesLoad(),
        userPreferencesRepository.userPreferences,
        searchQuery,
        revealMuted
    ) { result, rulesLoad, preferences, query, reveal ->
        when (rulesLoad) {
            is RulesLoad.Failed -> Result.Error(rulesLoad.error)
            is RulesLoad.Loaded -> when (result) {
                is Result.Success -> {
                    val rules: List<MuteRule> =
                        if (preferences.applyGlobalMuteRulesToReviews) rulesLoad.rules
                        else emptyList()
                    Result.Success(
                        FilteredReviewFeed(
                            feed = MuteFeedFilter.filter(
                                articles = result.data.articles,
                                rules = rules,
                                context = MuteContext.Reviews,
                                searchQuery = query,
                                revealMuted = reveal
                            ),
                            failedSources = result.data.failedSources
                        )
                    )
                }
                is Result.Error -> Result.Error(result.exception)
                Result.Loading -> Result.Loading
            }
        }
    }.flowOn(dispatchers.default)
}
