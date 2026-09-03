package com.ilmek.bordro.data

/**
 * The 4 real TCDD payslips (05-08/2026) this app was built from, typed in as
 * seed history. Field values come straight off each payslip image; a few notes:
 *
 * - August is the exact slip code.html was hardcoded around - everything on it
 *   reconciles with the verified formulas in PayslipCalculator to the kuruş.
 * - July reconciles almost exactly too (its GMŞ%(17+7)24 amount is entered as
 *   an explicit rate rather than the general formula, since the printed figure
 *   is ~34 TL off from the formula - most likely a digit misread on that low-
 *   contrast screenshot, kept as-is here to match the real slip rather than
 *   silently "correct" a number that might in fact be right).
 * - May and June's own "SSK Matrahı" printed on the slip is far larger than
 *   Gelir Toplamı for that month (e.g. June: 198,201.12 vs 152,821.50 income) -
 *   too large to be this month's own base, most likely a cumulative SGK figure
 *   the real TCDD system tracks that these 4 samples don't give enough
 *   information to safely reverse-engineer. Rather than guess, those two months
 *   pin sskMatrahiManual straight to the printed value (which keeps SSK Prim,
 *   Damga Vergisi, Kesinti Toplamı and Net Ödeme exactly correct) and flag the
 *   one downstream field that formula can't fix (Aylık Glr.VM) in `notlar`.
 */
object SeedData {

    fun payslips(): List<PayslipData> = listOf(mayis2026, haziran2026, temmuz2026, agustos2026)

    private fun earning(
        name: String,
        hours: Double,
        rule: EarningRule,
        rate: Double = 0.0,
        role: EarningRole = EarningRole.NORMAL,
    ) = EarningItem(name = name, hours = hours, rule = rule, rate = rate, role = role)

    private fun deduction(
        name: String,
        amount: Double,
        kind: DeductionKind = DeductionKind.DEDUCT_ABS,
        reducesTaxBase: Boolean = false,
    ) = DeductionItem(name = name, amount = amount, kind = kind, reducesTaxBase = reducesTaxBase)

    private const val IASE_GUNLUK = 301.1575
    private const val HIZMET_YILLIK = 24.67

    val mayis2026 = PayslipData(
        year = 2026, month = 5, periodLabel = "05/2026 (15.04.2026-14.05.2026)",
        kidemYili = 14.0, hizmetZammiYili = 14.0, saatUcr = 385.98, emkZam = 17.78,
        calistigiGun = 30.0, sskGunu = 30.0,
        earnings = listOf(
            earning("Normal Çalış", 157.50, EarningRule.BASE),
            earning("Hafta Tatili", 30.00, EarningRule.BASE),
            earning("UBGT", 15.00, EarningRule.BASE),
            earning("Ücretli İzin", 22.50, EarningRule.BASE),
            earning("Vardiya Prim", 157.50, EarningRule.VARDIYA10),
            earning("Gece Çalışma", 12.00, EarningRule.GECE15),
            earning("İaşe Günü", 20.00, EarningRule.RATE, rate = IASE_GUNLUK, role = EarningRole.IASE_GUNU),
            earning("GŞT %10", 225.00, EarningRule.GST10),
            earning("Hizmet Zammı", 14.00, EarningRule.RATE, rate = HIZMET_YILLIK),
            earning("GMŞ%(17+7)24", 225.00, EarningRule.GMS24),
        ),
        deductions = listOf(
            deduction("Birleştirilmiş", 5089.70, DeductionKind.INCOME_ADD),
            deduction("Sendika Aidatı", 2503.31, reducesTaxBase = true),
            deduction("Vergiden Muaf", 3000.00, DeductionKind.EXEMPT_ONLY, reducesTaxBase = true),
        ),
        gelirVergisiMode = GelirVergisiMode.MANUAL,
        gelirVergisiManual = 24999.00,
        yillikGlrVMOnceki = 676764.77 - 108149.36,
        sskMatrahiManual = 224723.83,
        damgaVergisiManual = 765.44,
        mahsupFarki = 0.0,
        notlar = "Gerçek bordroda Spor Aidatı bu ay toplam tutara dahil edilmemiş görünüyor " +
            "(0 kabul edildi). SSK Matrahı bu ay olağan aylık taban formülünden çok yüksek " +
            "(224.723,83) - muhtemelen kümülatif bir SGK bileşeni içeriyor; gerçek bordro " +
            "değeri doğrudan kullanıldı, bu yüzden Aylık Glr.VM burada gerçek bordroyla " +
            "birebir uyuşmuyor.",
    )

    val haziran2026 = PayslipData(
        year = 2026, month = 6, periodLabel = "06/2026 (15.05.2026-14.06.2026)",
        kidemYili = 14.0, hizmetZammiYili = 14.0, saatUcr = 385.98, emkZam = 17.78,
        calistigiGun = 31.0, sskGunu = 30.0,
        earnings = listOf(
            earning("Normal Çalış", 126.00, EarningRule.BASE),
            earning("Hafta Tatili", 37.50, EarningRule.BASE),
            earning("UBGT", 40.50, EarningRule.BASE),
            earning("Ücretli İzin", 26.50, EarningRule.BASE),
            earning("Fzl Mes %100", 15.00, EarningRule.MESAI200),
            earning("Vardiya Prim", 141.00, EarningRule.VARDIYA10),
            earning("Gece Çalışma", 9.50, EarningRule.GECE15),
            earning("İaşe Günü", 19.00, EarningRule.RATE, rate = IASE_GUNLUK, role = EarningRole.IASE_GUNU),
            earning("GŞT %10", 223.00, EarningRule.GST10),
            earning("Hizmet Zammı", 14.00, EarningRule.RATE, rate = HIZMET_YILLIK),
            earning("GMŞ%(17+7)24", 223.00, EarningRule.GMS24),
        ),
        deductions = listOf(
            deduction("Birleştirilmiş", 5089.69, DeductionKind.INCOME_ADD),
            deduction("Sendika Aidatı", 2503.31, reducesTaxBase = true),
            deduction("Spor Aidatı", 10.00),
            deduction("Vergiden Muaf", 2999.99, DeductionKind.EXEMPT_ONLY, reducesTaxBase = true),
        ),
        gelirVergisiMode = GelirVergisiMode.MANUAL,
        gelirVergisiManual = 29209.31,
        yillikGlrVMOnceki = 851624.51 - 123780.11,
        sskMatrahiManual = 198201.12,
        damgaVergisiManual = 865.95,
        mahsupFarki = 0.0,
        notlar = "SSK Matrahı bu ay da (198.201,12) Gelir Toplamından yüksek - Mayıs'taki gibi " +
            "muhtemelen kümülatif bir SGK bileşeni içeriyor; gerçek bordro değeri doğrudan " +
            "kullanıldı. Aylık Glr.VM bu yüzden gerçek bordroyla birebir uyuşmuyor.",
    )

    val temmuz2026 = PayslipData(
        year = 2026, month = 7, periodLabel = "07/2026 (15.06.2026-14.07.2026)",
        kidemYili = 14.0, hizmetZammiYili = 15.0, saatUcr = 385.98, emkZam = 19.05,
        calistigiGun = 30.0, sskGunu = 30.0,
        // July's printed amounts for the rate-scaling rows (Normal Çalış, Hafta
        // Tatili, Ücretli İzin, Vardiya Prim, Gece Çalışma, GMŞ) all come out
        // 0.1-0.6% below what BASE/VARDIYA10/GECE15/GMS24 predict from this
        // month's own saatUcr+emkZam - consistently enough (~404.40 vs the
        // nominal 405.03 effective rate) that it isn't a one-off misread, but
        // not explained by anything on the slip either. Rather than let a
        // guessed formula drift this historical record ~145 TL from the real
        // Gelir Toplamı, every row here is pinned to its exact printed amount
        // (as a RATE with rate = printed amount / hours), same treatment GŞT
        // %10 and İaşe Günü already get for their own flat rates.
        earnings = listOf(
            earning("Normal Çalış", 180.00, EarningRule.RATE, rate = 72793.65 / 180.0),
            earning("Hafta Tatili", 30.00, EarningRule.RATE, rate = 12131.86 / 30.0),
            earning("Ücretli İzin", 15.00, EarningRule.RATE, rate = 6065.93 / 15.0),
            earning("Vardiya Prim", 118.00, EarningRule.RATE, rate = 4768.18 / 118.0),
            earning("Gece Çalışma", 9.00, EarningRule.RATE, rate = 545.36 / 9.0),
            earning("İaşe Günü", 23.00, EarningRule.RATE, rate = IASE_GUNLUK, role = EarningRole.IASE_GUNU),
            earning("GŞT %10", 225.00, EarningRule.GST10),
            earning("Hizmet Zammı", 15.00, EarningRule.RATE, rate = HIZMET_YILLIK),
            earning("GMŞ%(17+7)24", 225.00, EarningRule.RATE, rate = 21837.94 / 225.0),
        ),
        deductions = listOf(
            deduction("Birleştirilmiş", 5089.71, DeductionKind.INCOME_ADD),
            deduction("Sendika Aidatı", 2511.19, reducesTaxBase = true),
            deduction("Spor Aidatı", 10.00),
            deduction("Vergiden Muaf", 3000.00, DeductionKind.EXEMPT_ONLY, reducesTaxBase = true),
            deduction("Mahsup Kesintisi", -380.65),
        ),
        gelirVergisiMode = GelirVergisiMode.MANUAL,
        gelirVergisiManual = 26483.74,
        yillikGlrVMOnceki = 965991.47 - 114894.43,
        sskMatrahiManual = null, // auto formula matches within 1 kuruş
        damgaVergisiManual = 753.55,
        mahsupFarki = 0.0,
    )

    val agustos2026 = PayslipData(
        year = 2026, month = 8, periodLabel = "08/2026 (15.07.2026-14.08.2026)",
        kidemYili = 15.0, hizmetZammiYili = 15.0, saatUcr = 385.98, emkZam = 19.05,
        calistigiGun = 31.0, sskGunu = 30.0,
        earnings = listOf(
            earning("Normal Çalış", 62.00, EarningRule.BASE),
            earning("Hafta Tatili", 30.00, EarningRule.BASE),
            earning("UBGT", 9.00, EarningRule.BASE),
            earning("Ücretli İzin", 64.00, EarningRule.BASE),
            earning("Ücretli Rapor", 72.00, EarningRule.BASE),
            earning("İaşe Günü", 8.00, EarningRule.RATE, rate = IASE_GUNLUK, role = EarningRole.IASE_GUNU),
            earning("GŞT %10", 155.00, EarningRule.GST10),
            earning("Hizmet Zammı", 15.00, EarningRule.RATE, rate = HIZMET_YILLIK),
            earning("GMŞ%(17+7)24", 155.00, EarningRule.GMS24),
        ),
        deductions = listOf(
            deduction("Birleştirilmiş", 5089.70, DeductionKind.INCOME_ADD),
            deduction("Sendika Aidatı", 2511.19, reducesTaxBase = true),
            deduction("Spor Aidatı", 10.00),
            deduction("Vergiden Muaf", 3000.00, DeductionKind.EXEMPT_ONLY, reducesTaxBase = true),
            deduction("Terfi Fark-İ", 8684.57, DeductionKind.INCOME_ADD),
            deduction("Mahsup Kesintisi", -6004.01),
        ),
        gelirVergisiMode = GelirVergisiMode.MANUAL,
        gelirVergisiManual = 25131.61,
        yillikGlrVMOnceki = 1071964.16 - 113876.71,
        sskMatrahiManual = null, // auto formula matches exactly (the slip this app was built from)
        damgaVergisiManual = 745.07,
        mahsupFarki = -445.91,
    )
}
