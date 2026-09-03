package com.ilmek.bordro.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilmek.bordro.calc.PayslipCalculator
import com.ilmek.bordro.data.Payslip
import com.ilmek.bordro.data.PayslipRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PayslipRow(val payslip: Payslip, val netOdeme: Double)

class PayslipListViewModel(private val repository: PayslipRepository) : ViewModel() {

    val rows: StateFlow<List<PayslipRow>> = repository.observeAll()
        .map { list -> list.map { PayslipRow(it, PayslipCalculator.calculate(it.data).netOdeme) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(payslip: Payslip) {
        viewModelScope.launch { repository.delete(payslip) }
    }
}
