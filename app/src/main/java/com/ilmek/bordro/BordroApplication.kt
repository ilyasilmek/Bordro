package com.ilmek.bordro

import android.app.Application
import com.ilmek.bordro.data.AppDatabase
import com.ilmek.bordro.data.PayslipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BordroApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val repository: PayslipRepository by lazy {
        PayslipRepository(AppDatabase.get(this, applicationScope).payslipDao())
    }
}
