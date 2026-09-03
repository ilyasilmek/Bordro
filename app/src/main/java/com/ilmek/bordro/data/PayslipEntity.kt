package com.ilmek.bordro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.Json

@Entity(tableName = "payslips")
data class PayslipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int,
    val dataJson: String,
)

data class Payslip(val id: Long = 0, val data: PayslipData)

fun Payslip.toEntity(): PayslipEntity = PayslipEntity(
    id = id,
    year = data.year,
    month = data.month,
    dataJson = PayslipJson.json.encodeToString(PayslipData.serializer(), data),
)

fun PayslipEntity.toDomain(): Payslip = Payslip(
    id = id,
    data = PayslipJson.json.decodeFromString(PayslipData.serializer(), dataJson),
)

object PayslipJson {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
}
