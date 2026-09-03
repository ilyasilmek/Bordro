package com.ilmek.bordro.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilmek.bordro.calc.PayslipCalculator
import com.ilmek.bordro.data.DeductionItem
import com.ilmek.bordro.data.EarningItem
import com.ilmek.bordro.data.Payslip
import com.ilmek.bordro.data.PayslipData
import com.ilmek.bordro.data.PayslipRepository
import com.ilmek.bordro.data.PayslipResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

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
    return PayslipData(year = year, month = month, periodLabel = "%02d/%d".format(month, year))
}

class PayslipEditViewModel(
    private val repository: PayslipRepository,
    private val payslipId: Long,
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
            _state.update { it.copy(data = base.copy(yillikGlrVMOnceki = prevCumulative), loading = false) }
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

    fun save() {
        viewModelScope.launch {
            val current = _state.value
            repository.save(Payslip(id = current.id, data = current.data))
            _state.update { it.copy(saved = true) }
        }
    }
}
