package com.ilmek.bordro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PayslipDao {

    @Query("SELECT * FROM payslips ORDER BY year DESC, month DESC")
    fun observeAll(): Flow<List<PayslipEntity>>

    @Query("SELECT * FROM payslips WHERE id = :id")
    suspend fun getById(id: Long): PayslipEntity?

    @Query("SELECT * FROM payslips WHERE (year * 12 + month) < (:year * 12 + :month) ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getPreviousMonth(year: Int, month: Int): PayslipEntity?

    @Query("SELECT * FROM payslips ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getMostRecent(): PayslipEntity?

    @Insert
    suspend fun insert(entity: PayslipEntity): Long

    @Update
    suspend fun update(entity: PayslipEntity)

    @Delete
    suspend fun delete(entity: PayslipEntity)
}
