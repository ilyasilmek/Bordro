package com.ilmek.bordro.data

/**
 * The union of every earning row seen across the 4 real reference payslips
 * (05-08/2026), so a new payslip starts with all of them pre-added (correct
 * name + calc rule already set) and the user only has to type in that month's
 * hours/days - never add rows by hand for the standard TCDD categories.
 */
object DefaultTemplates {
    const val IASE_GUNLUK = 301.1575
    const val HIZMET_YILLIK = 24.67

    fun standardEarnings(): List<EarningItem> = listOf(
        EarningItem(name = "Normal Çalış", rule = EarningRule.BASE),
        EarningItem(name = "Hafta Tatili", rule = EarningRule.BASE),
        EarningItem(name = "UBGT", rule = EarningRule.BASE),
        EarningItem(name = "Ücretli İzin", rule = EarningRule.BASE),
        EarningItem(name = "Ücretli Rapor", rule = EarningRule.BASE),
        EarningItem(name = "Vardiya Prim", rule = EarningRule.VARDIYA10),
        EarningItem(name = "Gece Çalışma", rule = EarningRule.GECE15),
        EarningItem(name = "Fzl Mes %100", rule = EarningRule.MESAI200),
        EarningItem(name = "İaşe Günü", rule = EarningRule.RATE, rate = IASE_GUNLUK, role = EarningRole.IASE_GUNU),
        EarningItem(name = "GŞT %10", rule = EarningRule.GST10),
        EarningItem(name = "Hizmet Zammı", rule = EarningRule.RATE, rate = HIZMET_YILLIK),
        EarningItem(name = "GMŞ%(17+7)24", rule = EarningRule.GMS24),
    )

    fun standardDeductions(): List<DeductionItem> = listOf(
        // 31. Dönem TİS Madde 69: Birleştirilmiş Sosyal Yardım, ücret zammıyla aynı
        // oranda artar. Sendika Aidatı (Madde 18 + 6356 s. Kanun) da gerçek
        // bordrolarda Saat Ücr + Emk. Zam ile birebir orantılı hareket ediyor.
        DeductionItem(name = "Birleştirilmiş", kind = DeductionKind.INCOME_ADD, scalesWithRaise = true),
        DeductionItem(name = "Terfi Fark-İ", kind = DeductionKind.INCOME_ADD),
        DeductionItem(name = "Sendika Aidatı", reducesTaxBase = true, scalesWithRaise = true),
        DeductionItem(name = "Spor Aidatı"),
        DeductionItem(name = "Vergiden Muaf", kind = DeductionKind.EXEMPT_ONLY, reducesTaxBase = true),
        DeductionItem(name = "Mahsup Kesintisi"),
    )

    private const val GST_ROW_NAME = "GŞT %10"
    private const val VERGIDEN_MUAF_NAME = "Vergiden Muaf"

    /**
     * Adjusts a payslip's rows and SGK rates for [status]. Gazi/özel statü carries
     * its documented reduced rates (verified against the 4 real reference slips):
     * SSK Primi İşçi %9 (MYÖ only, GSS employee share waived), SSK Primi İşveren
     * %14,25, and full İşsizlik Sigortası exemption - the GŞT %10 and Vergiden
     * Muaf (3. derece engelli/gazi istisnası) rows only apply to this status.
     *
     * A regular (Normal) public worker gets none of those special exemptions, so
     * both rows are removed and the standard 2026 4/a sigortalısı rates apply
     * instead: SSK Primi İşçi %14 (9% MYÖ + 5% GSS), SSK Primi İşveren %21,75
     * (12% MYÖ + 7,5% GSS + 2,25% kısa vadeli sigorta kolları), and İşsizlik
     * Sigortası at the standard %1 (işçi) / %2 (işveren) - Gelir Vergisi Genel
     * Tebliği-independent SGK rates, confirmed against SGK's 2026 published
     * table (see alomaliye.com "SGK Primleri Rehberi - 2026").
     */
    fun applyStatus(data: PayslipData, status: EmployeeStatus): PayslipData = when (status) {
        EmployeeStatus.GAZI -> data.copy(
            earnings = if (data.earnings.none { it.name == GST_ROW_NAME }) {
                data.earnings + EarningItem(name = GST_ROW_NAME, rule = EarningRule.GST10)
            } else data.earnings,
            deductions = if (data.deductions.none { it.name == VERGIDEN_MUAF_NAME }) {
                data.deductions + DeductionItem(name = VERGIDEN_MUAF_NAME, kind = DeductionKind.EXEMPT_ONLY, reducesTaxBase = true)
            } else data.deductions,
            sskIsciOrani = 0.09,
            sskIsvereniOrani = 0.1425,
            issizlikSigortasiMuaf = true,
        )
        EmployeeStatus.NORMAL -> data.copy(
            earnings = data.earnings.filterNot { it.name == GST_ROW_NAME },
            deductions = data.deductions.filterNot { it.name == VERGIDEN_MUAF_NAME },
            sskIsciOrani = 0.14,
            sskIsvereniOrani = 0.2175,
            issizlikSigortasiMuaf = false,
        )
    }
}

enum class EmployeeStatus { GAZI, NORMAL }
