package com.eagskunst.emmanuel.gamingnews.ui.topics

import app.cash.turbine.test
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.AddTopicUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.GetTopicsUseCase
import com.eagskunst.emmanuel.gamingnews.core.domain.usecase.RemoveTopicUseCase
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.MainDispatcherRule
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeTopicsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TopicsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeTopicsRepository()

    private fun createViewModel(): TopicsViewModel = TopicsViewModel(
        getTopicsUseCase = GetTopicsUseCase(fakeRepository),
        addTopicUseCase = AddTopicUseCase(fakeRepository),
        removeTopicUseCase = RemoveTopicUseCase(fakeRepository)
    )

    @Test
    fun `given unsorted topics when collected then topics are sorted by name`() = runTest {
        fakeRepository.topicsFlow.value = listOf(Fixtures.topic("RPG"), Fixtures.topic("Action"))

        val viewModel = createViewModel()

        viewModel.topics.test {
            val topics = expectMostRecentItem()
            assertEquals(listOf(Fixtures.topic("Action"), Fixtures.topic("RPG")), topics)
        }
    }

    @Test
    fun `given a valid name when addTopic is called then repository adds the trimmed topic`() = runTest {
        val viewModel = createViewModel()

        viewModel.addTopic("  Shooter  ")

        assertEquals(listOf(Fixtures.topic("Shooter")), fakeRepository.topicsFlow.value)
    }

    @Test
    fun `given a blank name when addTopic is called then it is ignored`() = runTest {
        val viewModel = createViewModel()

        viewModel.addTopic("   ")

        assertEquals(emptyList<Topic>(), fakeRepository.topicsFlow.value)
    }

    @Test
    fun `given an existing topic when removeTopic is called then repository removes it`() = runTest {
        val topic = Fixtures.topic("Action")
        fakeRepository.topicsFlow.value = listOf(topic)
        val viewModel = createViewModel()

        viewModel.removeTopic(topic)

        assertEquals(emptyList<Topic>(), fakeRepository.topicsFlow.value)
    }
}
