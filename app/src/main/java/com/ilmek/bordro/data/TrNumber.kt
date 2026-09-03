package com.ilmek.bordro.data

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Turkish-locale number parsing/formatting, ported from code.html's
 * parseTRNumber / formatTRNumber (e.g. "25.111,86" <-> 25111.86).
 */
object TrNumber {

    fun parse(raw: String?): Double {
        if (raw.isNullOrBlank()) return 0.0
        var clean = raw.trim().replace("TL", "", ignoreCase = true).replace("₺", "").trim()
        val negative = clean.startsWith("-")
        clean = clean.removePrefix("-")
        clean = clean.replace(".", "").replace(",", ".")
        val num = clean.toDoubleOrNull() ?: return 0.0
        return if (negative) -num else num
    }

    fun format(value: Double, decimals: Int = 2): String {
        var n = value
        if (n.isNaN()) n = 0.0
        if (abs(n) < 0.000001) n = 0.0
        val negative = n < 0
        n = abs(n)

        val factor = Math.pow(10.0, decimals.toDouble())
        val roundedTotal = (n * factor).roundToLong()
        val intPart = roundedTotal / factor.toLong()
        val fracPart = roundedTotal % factor.toLong()

        val intStr = intPart.toString()
        val grouped = StringBuilder()
        for ((index, c) in intStr.reversed().withIndex()) {
            if (index != 0 && index % 3 == 0) grouped.append('.')
            grouped.append(c)
        }
        val intGrouped = grouped.reverse().toString()
        val fracStr = fracPart.toString().padStart(decimals, '0')

        val result = if (decimals > 0) "$intGrouped,$fracStr" else intGrouped
        return if (negative) "-$result" else result
    }
}
