package com.thien203.comicmanagement.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thien203.comicmanagement.data.local.dao.FormatDao
import com.thien203.comicmanagement.data.local.dao.GenreDao
import com.thien203.comicmanagement.data.local.dao.WorkDao
import com.thien203.comicmanagement.data.local.dao.VolumeDao
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity
import com.thien203.comicmanagement.data.local.entity.WorkEntity
import com.thien203.comicmanagement.data.local.entity.WorkGenreCrossRef
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WorkEntity::class,
        VolumeEntity::class,
        GenreEntity::class,
        WorkGenreCrossRef::class,
        FormatEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workDao(): WorkDao
    abstract fun volumeDao(): VolumeDao
    abstract fun genreDao(): GenreDao
    abstract fun formatDao(): FormatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "comic_management_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                populateInitialData()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                populateInitialData()
            }

            private fun populateInitialData() {
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialGenres(database.genreDao())
                        populateInitialFormats(database.formatDao())
                    }
                }
            }

            private suspend fun populateInitialGenres(genreDao: GenreDao) {
                val defaultGenres = listOf(
                    GenreEntity(name = "Hành động (Action)", shortName = "Action"),
                    GenreEntity(name = "Phiêu lưu (Adventure)", shortName = "Adventure"),
                    GenreEntity(name = "Hài hước (Comedy)", shortName = "Comedy"),
                    GenreEntity(name = "Tình cảm (Romance)", shortName = "Romance"),
                    GenreEntity(name = "Kỳ ảo (Fantasy)", shortName = "Fantasy"),
                    GenreEntity(name = "Viễn tưởng (Sci-Fi)", shortName = "Sci-Fi"),
                    GenreEntity(name = "Trinh thám (Mystery)", shortName = "Mystery"),
                    GenreEntity(name = "Giật gân / Tâm lý (Thriller)", shortName = "Thriller"),
                    GenreEntity(name = "Kinh dị (Horror)", shortName = "Horror"),
                    GenreEntity(name = "Cuộc sống (Slice of Life)", shortName = "Slice of Life"),
                    GenreEntity(name = "Học đường (School Life)", shortName = "School"),
                    GenreEntity(name = "Thể thao (Sports)", shortName = "Sports"),
                    GenreEntity(name = "Dị giới / Chuyển sinh (Isekai)", shortName = "Isekai"),
                    GenreEntity(name = "Trùng sinh / Hồi sinh (Regression)", shortName = "Regression"),
                    GenreEntity(name = "Siêu nhiên (Supernatural)", shortName = "Supernatural"),
                    GenreEntity(name = "Lịch sử (Historical)", shortName = "Historical"),
                    GenreEntity(name = "Võ thuật / Chưởng (Martial Arts)", shortName = "Martial Arts"),
                    GenreEntity(name = "Khí chất / Tu tiên (Xianxia)", shortName = "Xianxia"),
                    GenreEntity(name = "Mecha / Robot", shortName = "Mecha"),
                    GenreEntity(name = "Âm nhạc / Nghệ thuật (Music)", shortName = "Music"),
                    GenreEntity(name = "Trò chơi / Game (Gaming)", shortName = "Gaming"),
                    GenreEntity(name = "Ẩm thực / Nấu ăn (Cooking)", shortName = "Cooking"),
                    GenreEntity(name = "Y học / Bác sĩ (Medical)", shortName = "Medical"),
                    GenreEntity(name = "Shonen", shortName = "Shonen"),
                    GenreEntity(name = "Shojo", shortName = "Shojo"),
                    GenreEntity(name = "Seinen", shortName = "Seinen"),
                    GenreEntity(name = "Josei", shortName = "Josei"),
                    GenreEntity(name = "Đa vũ trụ / Hệ thống (System)", shortName = "System"),
                    GenreEntity(name = "Kịch tính / Drama", shortName = "Drama"),
                    GenreEntity(name = "Đời thường / Sinh tồn (Survival)", shortName = "Survival"),
                    GenreEntity(name = "Manga (Truyện Nhật)", shortName = "Manga"),
                    GenreEntity(name = "Manhwa (Truyện Hàn)", shortName = "Manhwa"),
                    GenreEntity(name = "Manhua (Truyện Trung)", shortName = "Manhua"),
                    GenreEntity(name = "Comic (Truyện Âu Mỹ)", shortName = "Comic")
                )
                genreDao.insertDefaultGenres(defaultGenres)
            }

            private suspend fun populateInitialFormats(formatDao: FormatDao) {
                val defaultFormats = listOf(
                    FormatEntity(name = "Light Novel (Truyện chữ Nhật)", shortName = "Light Novel"),
                    FormatEntity(name = "Webtoon (Truyện tranh màu)", shortName = "Webtoon"),
                    FormatEntity(name = "Artbook / Sách ảnh", shortName = "Artbook"),
                    FormatEntity(name = "Novel / Tiểu thuyết chữ", shortName = "Novel"),
                    FormatEntity(name = "Fanbook / DataBook", shortName = "Fanbook"),
                    FormatEntity(name = "One-shot (Truyện ngắn 1 tập)", shortName = "One-shot"),
                    FormatEntity(name = "Doujinshi (Ấn phẩm tự xuất bản)", shortName = "Doujinshi"),
                    FormatEntity(name = "Bìa mềm / Paperback (Bản thường)", shortName = "Bìa mềm"),
                    FormatEntity(name = "Bìa cứng / Hardcover", shortName = "Hardcover"),
                    FormatEntity(name = "Bản đặc biệt / Special Edition", shortName = "Special Ed.")
                )
                formatDao.insertDefaultFormats(defaultFormats)
            }
        }
    }
}
