package com.ilmek.bordro.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilmek.bordro.calc.PayslipCalculator
import com.ilmek.bordro.data.DeductionItem
import com.ilmek.bordro.data.DefaultTemplates
import com.ilmek.bordro.data.EarningItem
import com.ilmek.bordro.data.EmployeeStatus
import com.ilmek.bordro.data.Payslip
import com.ilmek.bordro.data.PayslipData
import com.ilmek.bordro.data.PayslipRepository
import com.ilmek.bordro.data.EarningRule
import com.ilmek.bordro.data.PayslipResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class EditUiState(
    val id: Long = 0,
    val data: PayslipData = defaultNewPayslip(),
    val loading: Boolean = true,
    val saved: Boolean = false,
) {
    val result: PayslipResult get() = PayslipCalculator.calculate(data)
}

private fun defaultNewPayslip(): PayslipData {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return PayslipData(
        year = year, month = month, periodLabel = "%02d/%d".format(month, year),
        earnings = DefaultTemplates.standardEarnings(),
        deductions = DefaultTemplates.standardDeductions(),
    )
}

class PayslipEditViewModel(
    private val repository: PayslipRepository,
    private val payslipId: Long,
    private val initialStatus: EmployeeStatus = EmployeeStatus.GAZI,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState(id = payslipId))
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (payslipId != 0L) {
                repository.get(payslipId)?.let { payslip ->
                    _state.update { it.copy(data = payslip.data, loading = false) }
                    return@launch
                }
            }
            val base = defaultNewPayslip()
            val prevCumulative = repository.suggestPreviousCumulative(base.year, base.month)
            val mostRecent = repository.mostRecent()?.data
            val carried = if (mostRecent != null) {
                base.copy(
                    adi = mostRecent.adi, soyadi = mostRecent.soyadi, sicilNo = mostRecent.sicilNo,
                    persNo = mostRecent.persNo, sskNo = mostRecent.sskNo, unvani = mostRecent.unvani,
                    derKad = mostRecent.derKad, islYS = mostRecent.islYS, isySsk = mostRecent.isySsk,
                    kidemYili = mostRecent.kidemYili, hizmetZammiYili = mostRecent.hizmetZammiYili,
                    // Wage-linked rates carry forward as-is (a raise is applied explicitly via
                    // applyRaise, not silently), so a new month never resets to the seed's
                    // hardcoded baseline once real payslips exist.
                    saatUcr = mostRecent.saatUcr, emkZam = mostRecent.emkZam,
                    // Always start from every standard TCDD earning row (not just whichever
                    // subset the previous month happened to use - a real month may skip
                    // Vardiya Prim/Gece Çalışma/Fzl Mes some periods), carrying over only a
                    // RATE row's own coefficient (so a raise already applied to İaşe Günü/
                    // Hizmet Zammı isn't lost) plus any custom rows the user added by hand.
                    earnings = run {
                        val template = DefaultTemplates.standardEarnings()
                        val recentByName = mostRecent.earnings.associateBy { it.name }
                        val templateNames = template.map { it.name }.toSet()
                        val mergedTemplate = template.map { tmpl ->
                            val recent = recentByName[tmpl.name]
                            if (recent != null && tmpl.rule == EarningRule.RATE) tmpl.copy(rate = recent.rate) else tmpl
                        }
                        val customRows = mostRecent.earnings
                            .filter { it.name !in templateNames }
                            .map { it.copy(id = UUID.randomUUID().toString(), hours = 0.0) }
                        mergedTemplate + customRows
                    },
                    // Deductions/income-add rows tend to stay flat month to month (union dues,
                    // disability exemption) - carry the last amount forward as a starting guess.
                    deductions = mostRecent.deductions.map { it.copy(id = UUID.randomUUID().toString()) },
                    sskIsciOrani = mostRecent.sskIsciOrani, sskIsvereniOrani = mostRecent.sskIsvereniOrani,
                    sskIaseMuafiyetGunluk = mostRecent.sskIaseMuafiyetGunluk,
                    issizlikSigortasiMuaf = mostRecent.issizlikSigortasiMuaf,
                    damgaVergisiOranBinde = mostRecent.damgaVergisiOranBinde,
                    asgariUcretAylikMatrah = mostRecent.asgariUcretAylikMatrah,
                )
            } else {
                base
            }
            val statused = DefaultTemplates.applyStatus(carried, initialStatus)
            _state.update { it.copy(data = statused.copy(yillikGlrVMOnceki = prevCumulative), loading = false) }
        }
    }

    fun update(transform: (PayslipData) -> PayslipData) {
        _state.update { it.copy(data = transform(it.data)) }
    }

    fun addEarning() = update { it.copy(earnings = it.earnings + EarningItem(name = "Yeni Kalem")) }

    fun updateEarning(id: String, transform: (EarningItem) -> EarningItem) = update { data ->
        data.copy(earnings = data.earnings.map { if (it.id == id) transform(it) else it })
    }

    fun removeEarning(id: String) = update { data -> data.copy(earnings = data.earnings.filterNot { it.id == id }) }

    fun addDeduction() = update { it.copy(deductions = it.deductions + DeductionItem(name = "Yeni Kesinti")) }

    fun updateDeduction(id: String, transform: (DeductionItem) -> DeductionItem) = update { data ->
        data.copy(deductions = data.deductions.map { if (it.id == id) transform(it) else it })
    }

    fun removeDeduction(id: String) = update { data -> data.copy(deductions = data.deductions.filterNot { it.id == id }) }

    /**
     * Applies a wage raise (e.g. a TİS zammı) by [percent]: Saat Ücr, Emk. Zam, every earning
     * row on a fixed RATE coefficient (İaşe Günü, Hizmet Zammı, or any custom flat-rate row),
     * and every deduction/gelir row marked [DeductionItem.scalesWithRaise] (Birleştirilmiş,
     * Sendika Aidatı by default - see TCDD's 31. Dönem TİS Madde 69 and Madde 18) all scale up
     * by the same percentage. Rows using BASE/GST10/VARDIYA10/GECE15/MESAI200/GMS24 already
     * derive their amount from Saat Ücr + Emk. Zam, so they scale automatically and are left
     * alone. Legally-set figures (asgari ücret matrahı, SSK/damga oranları, Vergiden Muaf) are
     * untouched - a wage raise doesn't change those.
     *
     * TCDD's pay period runs 15th-to-14th, so the month a raise takes effect on the 1st
     * straddles it: [daysAtOldRate] days of the period were still at the old rate and
     * [daysAtNewRate] at the new one. Rather than track hour-by-hour when each hour was
     * worked (which the app has no data for), each rate is blended as a day-weighted
     * average of its old and new value - the standard way payroll systems prorate a
     * mid-period rate change. Passing only [daysAtNewRate] (default) reduces to the full
     * raise, since the weighted average of 0 old-rate days is just the new rate.
     */
    fun applyRaise(percent: Double, daysAtOldRate: Double = 0.0, daysAtNewRate: Double = 1.0) {
        val totalDays = daysAtOldRate + daysAtNewRate
        if (totalDays <= 0.0) return
        fun blended(old: Double): Double {
            val new = old * (1.0 + percent / 100.0)
            return (daysAtOldRate * old + daysAtNewRate * new) / totalDays
        }
        update { data ->
            data.copy(
                saatUcr = blended(data.saatUcr),
                emkZam = blended(data.emkZam),
                earnings = data.earnings.map { item ->
                    if (item.rule == EarningRule.RATE) item.copy(rate = blended(item.rate)) else item
                },
                deductions = data.deductions.map { item ->
                    if (item.scalesWithRaise) item.copy(amount = blended(item.amount)) else item
                },
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            val current = _state.value
            repository.save(Payslip(id = current.id, data = current.data))
            _state.update { it.copy(saved = true) }
        }
    }
}
