package com.eagskunst.emmanuel.gamingnews.core.domain.usecase

import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import com.eagskunst.emmanuel.gamingnews.testutil.Fixtures
import com.eagskunst.emmanuel.gamingnews.testutil.fakes.FakeTopicsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicsUseCasesTest {

    private val fakeRepository = FakeTopicsRepository()

    @Test
    fun `given a list of topics when getTopicsUseCase is called then it returns the list`() = runTest {
        fakeRepository.topicsFlow.value = listOf(
            Fixtures.topic("RPG"),
            Fixtures.topic("FPS")
        )

        val useCase = GetTopicsUseCase(fakeRepository)

        assertEquals(
            listOf(
                Fixtures.topic("RPG"),
                Fixtures.topic("FPS")
            ),
            useCase().first()
        )
    }

    @Test
    fun `given a topic when addTopicUseCase is called then it is added to the repository`() = runTest {
        val useCase = AddTopicUseCase(fakeRepository)

        useCase(Fixtures.topic("RPG"))

        assertEquals(listOf(Fixtures.topic("RPG")), fakeRepository.topicsFlow.value)
    }

    @Test
    fun `given an existing topic when removeTopicUseCase is called then it is removed from the repository`() = runTest {
        fakeRepository.topicsFlow.value = listOf(
            Fixtures.topic("RPG"),
            Fixtures.topic("FPS")
        )

        val useCase = RemoveTopicUseCase(fakeRepository)

        useCase(Fixtures.topic("RPG"))

        assertEquals(listOf(Fixtures.topic("FPS")), fakeRepository.topicsFlow.value)
    }
}
