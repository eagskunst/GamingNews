package com.eagskunst.emmanuel.gamingnews.ui.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    getTopicsUseCase: GetTopicsUseCase,
    private val addTopicUseCase: AddTopicUseCase,
    private val removeTopicUseCase: RemoveTopicUseCase
) : ViewModel() {

    val topics: StateFlow<List<Topic>> = getTopicsUseCase()
        .map { it.sortedBy { topic -> topic.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addTopic(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch { addTopicUseCase(Topic(trimmed)) }
        }
    }

    fun removeTopic(topic: Topic) {
        viewModelScope.launch { removeTopicUseCase(topic) }
    }
}
