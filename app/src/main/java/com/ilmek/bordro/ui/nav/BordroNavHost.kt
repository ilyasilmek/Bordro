package com.ilmek.bordro.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ilmek.bordro.data.EmployeeStatus
import com.ilmek.bordro.data.PayslipRepository
import com.ilmek.bordro.ui.LambdaViewModelFactory
import com.ilmek.bordro.ui.edit.PayslipEditScreen
import com.ilmek.bordro.ui.edit.PayslipEditViewModel
import com.ilmek.bordro.ui.list.PayslipListScreen
import com.ilmek.bordro.ui.list.PayslipListViewModel
import com.ilmek.bordro.ui.status.StatusPickerScreen

private const val ROUTE_LIST = "list"
private const val ROUTE_STATUS_PICKER = "status_picker"
private const val ROUTE_EDIT = "edit/{payslipId}?status={status}"

@Composable
fun BordroNavHost(repository: PayslipRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_LIST) {
        composable(ROUTE_LIST) {
            val viewModel: PayslipListViewModel = viewModel(
                factory = LambdaViewModelFactory { PayslipListViewModel(repository) },
            )
            PayslipListScreen(
                viewModel = viewModel,
                onAddNew = { navController.navigate(ROUTE_STATUS_PICKER) },
                onOpen = { id -> navController.navigate("edit/$id") },
            )
        }
        composable(ROUTE_STATUS_PICKER) {
            StatusPickerScreen(
                onSelect = { status -> navController.navigate("edit/0?status=${status.name}") },
            )
        }
        composable(
            ROUTE_EDIT,
            arguments = listOf(
                navArgument("payslipId") { type = NavType.LongType },
                navArgument("status") { type = NavType.StringType; defaultValue = EmployeeStatus.GAZI.name },
            ),
        ) { backStackEntry ->
            val payslipId = backStackEntry.arguments?.getLong("payslipId") ?: 0L
            val status = backStackEntry.arguments?.getString("status")
                ?.let { runCatching { EmployeeStatus.valueOf(it) }.getOrNull() }
                ?: EmployeeStatus.GAZI
            val viewModel: PayslipEditViewModel = viewModel(
                key = "edit-$payslipId",
                factory = LambdaViewModelFactory { PayslipEditViewModel(repository, payslipId, status) },
            )
            PayslipEditScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
