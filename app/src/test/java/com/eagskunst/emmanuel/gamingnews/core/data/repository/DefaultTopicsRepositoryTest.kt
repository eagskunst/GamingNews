package com.eagskunst.emmanuel.gamingnews.core.data.repository

import com.eagskunst.emmanuel.gamingnews.core.data.source.local.TopicsLocalDataSource
import com.eagskunst.emmanuel.gamingnews.core.data.source.remote.FirebaseTopicsDataSource
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTopicsRepositoryTest {

    private val localDataSource: TopicsLocalDataSource = mockk(relaxed = true)
    private val firebaseDataSource: FirebaseTopicsDataSource = mockk(relaxed = true)
    private val repository = DefaultTopicsRepository(localDataSource, firebaseDataSource)

    @Test
    fun `when addTopic is called then the name is normalized and delegated to local and firebase`() = runTest {
        coEvery { localDataSource.addTopic(any()) } returns Unit
        coEvery { firebaseDataSource.subscribeToTopic(any()) } returns Unit


        repository.addTopic(Topic("  rpg games "))

        coVerify { localDataSource.addTopic(Topic("RPG_GAMES")) }
        coVerify { firebaseDataSource.subscribeToTopic("RPG_GAMES") }
    }

    @Test
    fun `when removeTopic is called then the name is normalized and delegated to local and firebase`() = runTest {
        coEvery { localDataSource.removeTopic(any()) } returns Unit
        coEvery { firebaseDataSource.unsubscribeFromTopic(any()) } returns Unit

        repository.removeTopic(Topic("Action RPG"))

        coVerify { localDataSource.removeTopic(Topic("ACTION_RPG")) }
        coVerify { firebaseDataSource.unsubscribeFromTopic("ACTION_RPG") }
    }

    @Test
    fun `when topicsStream is called then it emits the local data source topics`() = runTest {
        val expected = listOf(Topic("RPG"), Topic("FPS"))
        every { localDataSource.topics } returns flowOf(expected)

        val result = repository.topicsStream().toList()

        assertEquals(listOf(expected), result)
    }
}
