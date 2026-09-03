package com.ilmek.bordro.calc

import com.ilmek.bordro.data.DeductionKind
import com.ilmek.bordro.data.EarningRole
import com.ilmek.bordro.data.GelirVergisiMode
import com.ilmek.bordro.data.PayslipData
import com.ilmek.bordro.data.PayslipResult
import com.ilmek.bordro.data.TaxBracket
import kotlin.math.max

/**
 * Ports code.html's `recalculatePayslip` engine to Kotlin, with two corrections
 * found by cross-checking the 4 real reference payslips (05-08/2026) against the
 * original JS:
 *
 * 1. SSK Matrahı: code.html only ever special-cased August's exact totalEarnings
 *    (`if (abs(totalEarnings-133595.50)<1) sskMatrah=131195.50`) instead of a real
 *    formula. Comparing July and August shows SSK Matrahı = Gelir Toplamı -
 *    (300.00 TL x İaşe Günü) exactly (to the kuruş) in both months - the SSK
 *    meal-allowance exemption is a flat 300 TL/day cap, not the full paid İaşe
 *    rate (~301.16 TL/day). That real formula replaces the hardcoded patch here.
 *
 * 2. Kesinti Toplamı: code.html sums "Mahsup Fark" (a signed refund field) into
 *    the deduction total. Recomputing August's and July's Kesinti Toplamı by hand
 *    from every line on the real payslip images shows the printed total does NOT
 *    include Mahsup Fark - only excluding it reproduces both months' real Kesinti
 *    Toplamı and Net Ödeme exactly. So here it is informational only.
 *
 * 3. Gelir Vergisi ("oto" mode): code.html's cumulative-bracket brackets
 *    (158.000/330.000/800.000/4.300.000 TL) turned out to be wrong - the real
 *    2026 GİB "ücret gelirleri" tariff (Gelir Vergisi Genel Tebliği No. 332,
 *    31.12.2025) is 190.000/400.000/1.500.000/5.300.000 TL at the same
 *    15/20/27/35/40% rates. On top of that, wages have carried an "asgari
 *    ücret gelir vergisi istisnası" since 2022: the tax that would fall on a
 *    minimum-wage earner's own cumulative base is credited back every month,
 *    computed on the SAME cumulative brackets. Applying both together against
 *    the 4 real payslips reproduces Gelir Vergisi for June, July and August to
 *    the kuruş and May within 10 TL (out of ~25.000) - confirmed by reverse-
 *    engineering the minimum wage's own monthly tax base (28.075,53 TL, which
 *    also independently reproduces GİB's published annual exemption total of
 *    57.881,23 TL to within 6 kuruş). See DefaultTemplates for that constant.
 */
object PayslipCalculator {

    /** 2026 GİB "ücret gelirleri" tarifesi (Gelir Vergisi Genel Tebliği No. 332). */
    val DEFAULT_TAX_BRACKETS = listOf(
        TaxBracket(190_000.0, 0.15),
        TaxBracket(400_000.0, 0.20),
        TaxBracket(1_500_000.0, 0.27),
        TaxBracket(5_300_000.0, 0.35),
        TaxBracket(Double.POSITIVE_INFINITY, 0.40),
    )

    private fun taxForBase(totalBase: Double, brackets: List<TaxBracket>): Double {
        if (totalBase <= 0.0) return 0.0
        var tax = 0.0
        var prevLimit = 0.0
        for (bracket in brackets) {
            if (totalBase > prevLimit) {
                val taxableInBracket = minOf(totalBase, bracket.limit) - prevLimit
                tax += taxableInBracket * bracket.rate
            }
            if (totalBase <= bracket.limit) break
            prevLimit = bracket.limit
        }
        return tax
    }

    fun calculate(
        data: PayslipData,
        brackets: List<TaxBracket> = DEFAULT_TAX_BRACKETS,
    ): PayslipResult {
        val earningAmounts = data.earnings.associate { it.id to it.amount(data.saatUcr, data.emkZam) }
        val baseEarningsTotal = earningAmounts.values.sum()

        val specialIncomeTotal = data.deductions
            .filter { it.kind == DeductionKind.INCOME_ADD }
            .sumOf { kotlin.math.abs(it.amount) }

        val totalEarnings = baseEarningsTotal + specialIncomeTotal

        val iaseGunSayisi = data.earnings
            .filter { it.role == EarningRole.IASE_GUNU }
            .sumOf { it.hours }

        val sskMatrahiAuto = max(0.0, totalEarnings - (data.sskIaseMuafiyetGunluk * iaseGunSayisi))
        val sskMatrahi = data.sskMatrahiManual ?: sskMatrahiAuto
        val sskMatrahiIsAuto = data.sskMatrahiManual == null

        val sskPrimIsci = sskMatrahi * data.sskIsciOrani
        val sskPrimIsvereni = sskMatrahi * data.sskIsvereniOrani

        val issSigIsci = if (data.issizlikSigortasiMuaf) 0.0 else sskMatrahi * 0.01
        val issSigIsv = if (data.issizlikSigortasiMuaf) 0.0 else sskMatrahi * 0.02

        val taxBaseReduction = data.deductions
            .filter { it.reducesTaxBase }
            .sumOf { kotlin.math.abs(it.amount) }
        val aylikGlrVM = max(0.0, sskMatrahi - sskPrimIsci - taxBaseReduction)

        val yillikGlrVM = data.yillikGlrVMOnceki + aylikGlrVM

        var gelirVergisi: Double
        var efektifOranPct = 0.0
        var vergiDilimBilgi: String

        when (data.gelirVergisiMode) {
            GelirVergisiMode.MANUAL -> {
                gelirVergisi = data.gelirVergisiManual
                efektifOranPct = if (aylikGlrVM > 0) gelirVergisi / aylikGlrVM * 100.0 else 0.0
                vergiDilimBilgi = "Manuel Giriş"
            }
            GelirVergisiMode.FIXED -> {
                gelirVergisi = aylikGlrVM * (data.gelirVergisiFixedPct / 100.0)
                efektifOranPct = data.gelirVergisiFixedPct
                vergiDilimBilgi = "Sabit %${data.gelirVergisiFixedPct} Oranı"
            }
            GelirVergisiMode.OTO -> {
                val startCumulative = data.yillikGlrVMOnceki
                val endCumulative = yillikGlrVM
                val taxAtEnd = taxForBase(endCumulative, brackets)
                val taxAtStart = taxForBase(startCumulative, brackets)
                val normalTax = max(0.0, taxAtEnd - taxAtStart)

                // Asgari ücret gelir vergisi istisnası: credit back the tax that
                // would fall on a minimum-wage earner's own cumulative base for
                // this same calendar month, using the same brackets.
                val minWageStart = (data.month - 1) * data.asgariUcretAylikMatrah
                val minWageEnd = data.month * data.asgariUcretAylikMatrah
                val minWageExemption = max(0.0, taxForBase(minWageEnd, brackets) - taxForBase(minWageStart, brackets))

                gelirVergisi = max(0.0, normalTax - minWageExemption)
                efektifOranPct = if (aylikGlrVM > 0) gelirVergisi / aylikGlrVM * 100.0 else 0.0
                val activeBracket = when {
                    endCumulative > 5_300_000.0 -> "%40"
                    endCumulative > 1_500_000.0 -> "%35"
                    endCumulative > 400_000.0 -> "%27"
                    endCumulative > 190_000.0 -> "%20"
                    else -> "%15"
                }
                vergiDilimBilgi = "Kademeli Dilim: $activeBracket (Asgari Ücret İstisnası Uygulandı)"
            }
        }

        val damgaVergisiAuto = sskMatrahi * (data.damgaVergisiOranBinde / 1000.0)
        val damgaVergisi = data.damgaVergisiManual ?: damgaVergisiAuto
        val damgaVergisiIsAuto = data.damgaVergisiManual == null

        val regularDeductions = data.deductions
            .filter { it.kind == DeductionKind.DEDUCT_ABS }
            .sumOf { kotlin.math.abs(it.amount) }
        val signedDeductions = data.deductions
            .filter { it.kind == DeductionKind.DEDUCT_SIGNED }
            .sumOf { it.amount }

        val totalDeductions = sskPrimIsci + gelirVergisi + damgaVergisi +
            issSigIsci + regularDeductions + signedDeductions

        val netOdeme = totalEarnings - totalDeductions

        return PayslipResult(
            earningAmounts = earningAmounts,
            baseEarningsTotal = baseEarningsTotal,
            specialIncomeTotal = specialIncomeTotal,
            totalEarnings = totalEarnings,
            sskMatrahi = sskMatrahi,
            sskMatrahiIsAuto = sskMatrahiIsAuto,
            sskPrimIsci = sskPrimIsci,
            sskPrimIsvereni = sskPrimIsvereni,
            issSigortasiIsci = issSigIsci,
            issSigortasiIsvereni = issSigIsv,
            aylikGlrVM = aylikGlrVM,
            yillikGlrVM = yillikGlrVM,
            gelirVergisi = gelirVergisi,
            efektifOranPct = efektifOranPct,
            vergiDilimBilgi = vergiDilimBilgi,
            damgaVergisi = damgaVergisi,
            damgaVergisiIsAuto = damgaVergisiIsAuto,
            totalDeductions = totalDeductions,
            netOdeme = netOdeme,
        )
    }
}
