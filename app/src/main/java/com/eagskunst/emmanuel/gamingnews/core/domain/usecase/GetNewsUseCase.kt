package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.common.DispatcherProvider
import com.eagskunst.emmanuel.gamingnews.core.common.Result
import com.eagskunst.emmanuel.gamingnews.core.domain.model.FilteredFeed
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteContext
import com.eagskunst.emmanuel.gamingnews.core.domain.model.MuteFeedFilter
import com.eagskunst.emmanuel.gamingnews.core.domain.model.RulesLoad
import com.eagskunst.emmanuel.gamingnews.core.domain.model.asRulesLoad
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.MuteRulesRepository
import com.eagskunst.emmanuel.gamingnews.core.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn


class GetNewsUseCase(
    private val repository: NewsRepository,
    private val muteRulesRepository: MuteRulesRepository,
    private val dispatchers: DispatcherProvider
) {
    operator fun invoke(
        urls: List<String>,
        forceRefresh: Boolean = false,
        context: MuteContext = MuteContext.Global,
        searchQuery: Flow<String> = flowOf(""),
        revealMuted: Flow<Boolean> = flowOf(false)
    ): Flow<Result<FilteredFeed>> = combine(
        repository.newsStream(urls, forceRefresh),
        muteRulesRepository.observeRules().asRulesLoad(),
        searchQuery,
        revealMuted
    ) { result, rulesLoad, query, reveal ->
        when (rulesLoad) {
            is RulesLoad.Failed -> Result.Error(rulesLoad.error)
            is RulesLoad.Loaded -> when (result) {
                is Result.Success -> Result.Success(
                    MuteFeedFilter.filter(
                        articles = result.data,
                        rules = rulesLoad.rules,
                        context = context,
                        searchQuery = query,
                        revealMuted = reveal
                    )
                )
                is Result.Error -> Result.Error(result.exception)
                Result.Loading -> Result.Loading
            }
        }
    }.flowOn(dispatchers.default)
}
