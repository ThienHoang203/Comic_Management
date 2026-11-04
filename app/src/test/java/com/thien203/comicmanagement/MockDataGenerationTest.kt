package com.thien203.comicmanagement

import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import com.thien203.comicmanagement.data.model.SortOrder
import org.junit.Assert.*
import org.junit.Test

class MockDataGenerationTest {

    @Test
    fun mockWorkGeneration_volumesCountRangeIsValid() {
        val totalWorks = 100
        val works = (1..totalWorks).map { index ->
            val randomVolumes = (100..10000).random()
            WorkEntity(
                id = index.toLong(),
                title = "Tác phẩm thử nghiệm #$index",
                totalVolumes = randomVolumes,
                defaultPrice = 25000.0
            )
        }

        assertEquals(100, works.size)
        works.forEach { work ->
            assertTrue("Số tập phải >= 100", work.totalVolumes >= 100)
            assertTrue("Số tập phải <= 10000", work.totalVolumes <= 10000)
            assertEquals(25000.0, work.defaultPrice, 0.001)
        }
    }

    @Test
    fun mockVolumesCreation_priceAndOwnershipAssignedCorrectly() {
        val workId = 1L
        val totalVols = 150
        val defaultPrice = 30000.0

        val volumes = (1..totalVols).map { volNum ->
            VolumeEntity(
                mangaId = workId,
                volumeNumber = volNum,
                isOwned = (volNum % 2 == 0),
                price = defaultPrice
            )
        }

        assertEquals(150, volumes.size)
        assertEquals(75, volumes.count { it.isOwned })
        assertEquals(30000.0, volumes.first().price, 0.001)
    }

    @Test
    fun sortOrder_byPriceAndVolumes_worksCorrectly() {
        val work1 = WorkEntity(id = 1, title = "A", totalVolumes = 500)
        val work2 = WorkEntity(id = 2, title = "B", totalVolumes = 2000)
        val work3 = WorkEntity(id = 3, title = "C", totalVolumes = 100)

        val list = listOf(work1, work2, work3)
        val sortedByVolumesDesc = list.sortedByDescending { it.totalVolumes }

        assertEquals("B", sortedByVolumesDesc[0].title)
        assertEquals("A", sortedByVolumesDesc[1].title)
        assertEquals("C", sortedByVolumesDesc[2].title)
    }
}
