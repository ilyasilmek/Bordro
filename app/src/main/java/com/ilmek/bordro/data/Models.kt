package com.ilmek.bordro.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * How an earning row's amount is derived from its "hours" (which may really mean
 * hours, days, or years depending on the row) and the payslip's base rates.
 *
 * Verified against the 4 real TCDD payslips (05-08/2026) by cross-checking every
 * row's amount/hours ratio across months where saatUcr/emkZam changed:
 *  - BASE, GST10, GMS24 already existed in code.html but GMS24 was a hardcoded
 *    ratio (15067.12/155) that doesn't scale if saatUcr/emkZam change; here it's
 *    a real formula (effectiveRate * 0.24) confirmed exact on 3 of 4 months.
 *  - VARDIYA10, GECE15 and MESAI200 are new: code.html had no rule for the
 *    "Vardiya Prim", "Gece Çalışma" and "Fzl Mes %100" rows that appear on the
 *    May/June/July slips (its fixed 9-row template only covers August's rows).
 */
@Serializable
enum class EarningRule {
    BASE,       // hours * (saatUcr + emkZam)
    GST10,      // hours * saatUcr * 0.10
    VARDIYA10,  // hours * (saatUcr + emkZam) * 0.10
    GECE15,     // hours * (saatUcr + emkZam) * 0.15
    MESAI200,   // hours * (saatUcr + emkZam) * 2.0  (%100 fazla mesai zammı -> 2x taban)
    GMS24,      // hours * (saatUcr + emkZam) * 0.24
    RATE,       // hours * item's own editable rate (İaşe Günü, Hizmet Zammı, custom rows)
    MANUAL      // item's own "rate" field IS the amount directly; hours ignored
}

/** Marks the row used for the SSK meal-allowance day count (see PayslipCalculator). */
@Serializable
enum class EarningRole { NORMAL, IASE_GUNU }

@Serializable
data class EarningItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hours: Double = 0.0,
    val rule: EarningRule = EarningRule.BASE,
    val rate: Double = 0.0,
    val role: EarningRole = EarningRole.NORMAL,
) {
    fun amount(saatUcr: Double, emkZam: Double): Double {
        val effectiveRate = saatUcr + emkZam
        return when (rule) {
            EarningRule.BASE -> hours * effectiveRate
            EarningRule.GST10 -> hours * saatUcr * 0.10
            EarningRule.VARDIYA10 -> hours * effectiveRate * 0.10
            EarningRule.GECE15 -> hours * effectiveRate * 0.15
            EarningRule.MESAI200 -> hours * effectiveRate * 2.0
            EarningRule.GMS24 -> hours * effectiveRate * 0.24
            EarningRule.RATE -> hours * rate
            EarningRule.MANUAL -> rate
        }
    }
}

/**
 * INCOME_ADD:   added into Gelir Toplamı (e.g. Birleştirilmiş, Terfi Fark-İ).
 * DEDUCT_ABS:   abs(amount) subtracted in Kesinti Toplamı (e.g. Sendika, Mahsup Kesintisi).
 * DEDUCT_SIGNED:amount (as-is, can be negative/a refund) subtracted in Kesinti Toplamı.
 * EXEMPT_ONLY:  not part of any total; only reduces the income-tax base when
 *               [reducesTaxBase] is set (e.g. Vergiden Muaf).
 */
@Serializable
enum class DeductionKind { INCOME_ADD, DEDUCT_ABS, DEDUCT_SIGNED, EXEMPT_ONLY }

@Serializable
data class DeductionItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double = 0.0,
    val kind: DeductionKind = DeductionKind.DEDUCT_ABS,
    val reducesTaxBase: Boolean = false,
)

@Serializable
enum class GelirVergisiMode { OTO, FIXED, MANUAL }

@Serializable
data class PayslipData(
    val year: Int,
    val month: Int,
    val periodLabel: String,

    val adi: String = "İlyas",
    val soyadi: String = "İLMEK",
    val sicilNo: String = "084857",
    val persNo: String = "11000867",
    val sskNo: String = "3408199916012",
    val unvani: String = "VAGON İMAL VE TAMİRC",
    val derKad: String = "001/ 01",
    val islYS: String = "01/0104/03",
    val isySsk: String = "13317020211372650410",

    val kidemYili: Double = 15.0,
    val hizmetZammiYili: Double = 15.0,
    val saatUcr: Double = 385.98,
    val emkZam: Double = 19.05,
    val calistigiGun: Double = 31.0,
    val sskGunu: Double = 30.0,

    val earnings: List<EarningItem> = emptyList(),
    val deductions: List<DeductionItem> = emptyList(),

    val gelirVergisiMode: GelirVergisiMode = GelirVergisiMode.MANUAL,
    val gelirVergisiFixedPct: Double = 0.0,
    val gelirVergisiManual: Double = 0.0,

    /** Cumulative Yıllık Glr.VM carried in from the END of the previous month. */
    val yillikGlrVMOnceki: Double = 0.0,

    /** null => auto-computed via Gelir Toplamı - (sskIaseMuafiyetGunluk * İaşe Günü). */
    val sskMatrahiManual: Double? = null,
    val sskIsciOrani: Double = 0.09,
    val sskIsvereniOrani: Double = 0.1425,
    val sskIaseMuafiyetGunluk: Double = 300.0,

    val issizlikSigortasiMuaf: Boolean = true,

    /** null => auto-computed via SSK Matrahı * (damgaVergisiOranBinde / 1000). */
    val damgaVergisiManual: Double? = null,
    val damgaVergisiOranBinde: Double = 5.687,

    /** Informational only (see PayslipCalculator note) - not summed into any total. */
    val mahsupFarki: Double = 0.0,

    val notlar: String = "",
)

data class TaxBracket(val limit: Double, val rate: Double)

data class PayslipResult(
    val earningAmounts: Map<String, Double>,
    val baseEarningsTotal: Double,
    val specialIncomeTotal: Double,
    val totalEarnings: Double,

    val sskMatrahi: Double,
    val sskMatrahiIsAuto: Boolean,
    val sskPrimIsci: Double,
    val sskPrimIsvereni: Double,
    val issSigortasiIsci: Double,
    val issSigortasiIsvereni: Double,

    val aylikGlrVM: Double,
    val yillikGlrVM: Double,
    val gelirVergisi: Double,
    val efektifOranPct: Double,
    val vergiDilimBilgi: String,

    val damgaVergisi: Double,
    val damgaVergisiIsAuto: Boolean,

    val totalDeductions: Double,
    val netOdeme: Double,
)
