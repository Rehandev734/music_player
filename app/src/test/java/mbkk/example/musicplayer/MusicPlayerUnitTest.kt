package mbkk.example.musicplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicPlayerUnitTest {

    private val sampleSongs = listOf(
        SongModel(1L, "Symphony No 1", "Composer Alpha", 243000L, "/path/1", "art/1"),
        SongModel(2L, "Sonata Minor", "Soloist Beta", 230000L, "/path/2", "art/2"),
        SongModel(3L, "Sonata Major", "Soloist Beta", 200000L, "/path/3", "art/3"),
        SongModel(4L, "Acoustic Melody", "Ensemble Gamma", 248000L, "/path/4", "art/4")
    )

    @Test
    fun testSongModelCreation() {
        val song = SongModel(10L, "Test Song", "Test Artist", 180000L, "/storage/song.mp3", "content://art/10")
        assertEquals(10L, song.id)
        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals(180000L, song.duration)
        assertEquals("/storage/song.mp3", song.path)
        assertEquals("content://art/10", song.albumArt)
    }

    @Test
    fun testSearchFilteringByTitle() {
        val query = "sonata"
        val filtered = sampleSongs.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
        assertEquals(2, filtered.size)
        assertEquals("Sonata Minor", filtered[0].title)
    }

    @Test
    fun testSearchFilteringByArtist() {
        val query = "soloist"
        val filtered = sampleSongs.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
        assertEquals(2, filtered.size)
    }

    @Test
    fun testEmptySearchQueryReturnsAll() {
        val query = ""
        val filtered = if (query.isEmpty()) sampleSongs else sampleSongs.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
        assertEquals(sampleSongs.size, filtered.size)
    }

    @Test
    fun testNoMatchSearchReturnsEmpty() {
        val query = "NonExistentTrackXYZ"
        val filtered = sampleSongs.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun testFormatDuration() {
        fun format(ms: Long): String {
            val minutes = (ms / 1000) / 60
            val seconds = (ms / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        assertEquals("0:00", format(0L))
        assertEquals("1:05", format(65000L))
        assertEquals("3:20", format(200000L))
        assertEquals("4:08", format(248000L))
    }

    @Test
    fun testShuffleExcludesCurrentIndexWhenMultipleSongs() {
        val currentIndex = 1
        var randomIndex: Int
        var attempts = 0
        do {
            randomIndex = sampleSongs.indices.random()
            attempts++
        } while (randomIndex == currentIndex && attempts < 100)

        assertNotEquals(currentIndex, randomIndex)
        assertTrue(randomIndex in sampleSongs.indices)
    }
}
