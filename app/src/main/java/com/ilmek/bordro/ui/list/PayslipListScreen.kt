package com.ilmek.bordro.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilmek.bordro.data.TrNumber

private val MONTH_NAMES = listOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipListScreen(
    viewModel: PayslipListViewModel,
    onAddNew: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val rows by viewModel.rows.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("TCDD Bordro") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) { Icon(Icons.Filled.Add, contentDescription = "Yeni Bordro") }
        },
    ) { padding ->
        if (rows.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Henüz bordro kaydı yok. + ile yeni ekleyin.", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            items(rows, key = { it.payslip.id }) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    onClick = { onOpen(row.payslip.id) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            val data = row.payslip.data
                            Text(
                                "${MONTH_NAMES.getOrElse(data.month - 1) { data.month.toString() }} ${data.year}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(data.periodLabel, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${TrNumber.format(row.netOdeme)} ₺",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            IconButton(onClick = { viewModel.delete(row.payslip) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Sil")
                            }
                        }
                    }
                }
            }
        }
    }
}
