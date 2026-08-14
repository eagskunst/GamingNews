package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

// The Context.topicsDataStore delegate caches a single DataStore instance for the whole test
// class run, so topics persist across test methods; clear them before each test rather than
// relying on a pristine on-disk store.
@RunWith(RobolectricTestRunner::class)
class TopicsLocalDataSourceTest {

    private val dataSource = TopicsLocalDataSource(RuntimeEnvironment.getApplication())

    @Before
    fun clearStoredTopics() = runTest {
        dataSource.topics.first().forEach { dataSource.removeTopic(it) }
    }

    @Test
    fun `given nothing stored when topics then returns empty list`() = runTest {
        assertEquals(emptyList<Topic>(), dataSource.topics.first())
    }

    @Test
    fun `given addTopic when topics then contains the added topic`() = runTest {
        dataSource.addTopic(Topic("RPG"))

        assertEquals(listOf(Topic("RPG")), dataSource.topics.first())
    }

    @Test
    fun `given multiple added topics when topics then sorted alphabetically`() = runTest {
        dataSource.addTopic(Topic("RPG"))
        dataSource.addTopic(Topic("Action"))
        dataSource.addTopic(Topic("Indie"))

        assertEquals(
            listOf(Topic("Action"), Topic("Indie"), Topic("RPG")),
            dataSource.topics.first()
        )
    }

    @Test
    fun `given removeTopic when topics then no longer contains the removed topic`() = runTest {
        dataSource.addTopic(Topic("RPG"))
        dataSource.addTopic(Topic("Action"))

        dataSource.removeTopic(Topic("RPG"))

        assertEquals(listOf(Topic("Action")), dataSource.topics.first())
    }
}
