package com.ilmek.bordro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PayslipEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun payslipDao(): PayslipDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context, scope: CoroutineScope): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bordro.db",
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            val dao = get(context, scope).payslipDao()
                            SeedData.payslips().forEach { data ->
                                dao.insert(Payslip(data = data).toEntity())
                            }
                        }
                    }
                }).build().also { instance = it }
            }
    }
}
