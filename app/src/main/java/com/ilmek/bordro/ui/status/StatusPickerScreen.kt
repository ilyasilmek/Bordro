package com.ilmek.bordro.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilmek.bordro.data.EmployeeStatus
import com.ilmek.bordro.ui.theme.Bordro

/**
 * Shown every time a new payslip is started, since the app now serves both the
 * Gazi/özel statü profile it was built around and regular public workers, and
 * the two need different rows/rates (see DefaultTemplates.applyStatus).
 */
@Composable
fun StatusPickerScreen(onSelect: (EmployeeStatus) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Bordro.DocBg).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Bordro Türünü Seçin",
            fontFamily = Bordro.Mono, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Bordro.ValueText,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Yasal kesinti oranları ve bazı hakediş kalemleri statüye göre değişir.",
            fontFamily = Bordro.Mono, fontSize = 12.sp, color = Bordro.LabelText,
        )
        Spacer(Modifier.height(28.dp))

        StatusCard(
            title = "Gazi / Özel Statü",
            description = "GŞT %10 ve Vergiden Muaf kalemleri dahil. SSK Primi İşçi %9, İşveren %14,25. " +
                "İşsizlik Sigortası muaf.",
            onClick = { onSelect(EmployeeStatus.GAZI) },
        )
        Spacer(Modifier.height(16.dp))
        StatusCard(
            title = "Normal Kamu İşçisi",
            description = "GŞT ve Vergiden Muaf kalemleri yok. Standart 2026 SGK oranları: " +
                "SSK Primi İşçi %14, İşveren %21,75. İşsizlik Sigortası %1 / %2.",
            onClick = { onSelect(EmployeeStatus.NORMAL) },
        )
    }
}

@Composable
private fun StatusCard(title: String, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Bordro.FieldBg, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(title, fontFamily = Bordro.Mono, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Bordro.Emerald)
        Spacer(Modifier.height(6.dp))
        Text(description, fontFamily = Bordro.Mono, fontSize = 11.sp, color = Bordro.LabelText)
    }
}
