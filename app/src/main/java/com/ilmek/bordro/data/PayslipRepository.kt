package com.ilmek.bordro.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PayslipRepository(private val dao: PayslipDao) {

    fun observeAll(): Flow<List<Payslip>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun get(id: Long): Payslip? = dao.getById(id)?.toDomain()

    /** End-of-month cumulative Yıllık Glr.VM for the month right before (year, month), if saved. */
    suspend fun suggestPreviousCumulative(year: Int, month: Int): Double {
        val prev = dao.getPreviousMonth(year, month)?.toDomain() ?: return 0.0
        return com.ilmek.bordro.calc.PayslipCalculator.calculate(prev.data).yillikGlrVM
    }

    suspend fun save(payslip: Payslip): Long =
        if (payslip.id == 0L) dao.insert(payslip.toEntity())
        else {
            dao.update(payslip.toEntity())
            payslip.id
        }

    suspend fun delete(payslip: Payslip) = dao.delete(payslip.toEntity())
}
