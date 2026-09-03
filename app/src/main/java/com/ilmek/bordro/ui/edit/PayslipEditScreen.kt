package com.ilmek.bordro.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilmek.bordro.data.DeductionItem
import com.ilmek.bordro.data.DeductionKind
import com.ilmek.bordro.data.EarningItem
import com.ilmek.bordro.data.EarningRole
import com.ilmek.bordro.data.EarningRule
import com.ilmek.bordro.data.GelirVergisiMode
import com.ilmek.bordro.data.TrNumber
import com.ilmek.bordro.ui.components.TrNumberField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipEditScreen(
    viewModel: PayslipEditViewModel,
    onBack: () -> Unit,
) {
    val ui by viewModel.state.collectAsState()

    LaunchedEffect(ui.saved) { if (ui.saved) onBack() }

    if (ui.loading) return

    val data = ui.data
    val result = ui.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${data.periodLabel}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Geri") }
                },
                actions = {
                    IconButton(onClick = viewModel::save) { Icon(Icons.Filled.Save, contentDescription = "Kaydet") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { NetOdemeCard(result.totalEarnings, result.totalDeductions, result.netOdeme) }

            item { SectionCard(title = "Personel / Dönem") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data.periodLabel, onValueChange = { v -> viewModel.update { it.copy(periodLabel = v) } },
                        label = { Text("Dönem") }, modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data.adi, onValueChange = { v -> viewModel.update { it.copy(adi = v) } },
                        label = { Text("Adı") }, modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = data.soyadi, onValueChange = { v -> viewModel.update { it.copy(soyadi = v) } },
                        label = { Text("Soyadı") }, modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = data.unvani, onValueChange = { v -> viewModel.update { it.copy(unvani = v) } },
                    label = { Text("Ünvanı") }, modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrNumberField("Saat Ücr", data.saatUcr, { v -> viewModel.update { it.copy(saatUcr = v) } }, Modifier.weight(1f))
                    TrNumberField("Emk. Zam", data.emkZam, { v -> viewModel.update { it.copy(emkZam = v) } }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrNumberField("Kıdem Yılı", data.kidemYili, { v -> viewModel.update { it.copy(kidemYili = v) } }, Modifier.weight(1f))
                    TrNumberField("Hzm.Zammı Yıl", data.hizmetZammiYili, { v -> viewModel.update { it.copy(hizmetZammiYili = v) } }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrNumberField("Çalıştığı Gün", data.calistigiGun, { v -> viewModel.update { it.copy(calistigiGun = v) } }, Modifier.weight(1f))
                    TrNumberField("SSK Günü", data.sskGunu, { v -> viewModel.update { it.copy(sskGunu = v) } }, Modifier.weight(1f))
                }
            } }

            item {
                Text("Hakedişler", style = MaterialTheme.typography.titleMedium)
            }
            items(data.earnings, key = { it.id }) { item ->
                EarningRow(
                    item = item,
                    saatUcr = data.saatUcr,
                    emkZam = data.emkZam,
                    onChange = { updated -> viewModel.updateEarning(item.id) { updated } },
                    onDelete = { viewModel.removeEarning(item.id) },
                )
            }
            item {
                TextButton(onClick = viewModel::addEarning) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text("Hakediş Kalemi Ekle")
                }
            }

            item { Text("Kesintiler ve Ek Gelirler", style = MaterialTheme.typography.titleMedium) }
            items(data.deductions, key = { it.id }) { item ->
                DeductionRow(
                    item = item,
                    onChange = { updated -> viewModel.updateDeduction(item.id) { updated } },
                    onDelete = { viewModel.removeDeduction(item.id) },
                )
            }
            item {
                TextButton(onClick = viewModel::addDeduction) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Kesinti / Gelir Kalemi Ekle")
                }
            }

            item {
                SectionCard(title = "Yasal Kesinti ve Sonuçlar") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SSK Matrahı Elle Gir", modifier = Modifier.weight(1f))
                        Switch(
                            checked = data.sskMatrahiManual != null,
                            onCheckedChange = { on ->
                                viewModel.update { it.copy(sskMatrahiManual = if (on) result.sskMatrahi else null) }
                            },
                        )
                    }
                    if (data.sskMatrahiManual != null) {
                        TrNumberField(
                            "SSK Matrahı", data.sskMatrahiManual, { v -> viewModel.update { it.copy(sskMatrahiManual = v) } },
                            Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text("SSK Matrahı (OTO): ${TrNumber.format(result.sskMatrahi)} ₺", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("SSK Prim İşçi (%${(data.sskIsciOrani * 100)}): ${TrNumber.format(result.sskPrimIsci)} ₺")
                    Text("SSK Prim İşveren (%${(data.sskIsvereniOrani * 100)}): ${TrNumber.format(result.sskPrimIsvereni)} ₺")

                    HorizontalDivider(Modifier.padding(vertical = 6.dp))

                    TrNumberField(
                        "Yıllık Glr.VM (Önceki Ay Sonu)", data.yillikGlrVMOnceki,
                        { v -> viewModel.update { it.copy(yillikGlrVMOnceki = v) } }, Modifier.fillMaxWidth(),
                    )
                    Text("Aylık Glr.VM: ${TrNumber.format(result.aylikGlrVM)} ₺")
                    Text("Yıllık Glr.VM (bu ay sonu): ${TrNumber.format(result.yillikGlrVM)} ₺")

                    EnumDropdown(
                        label = "Gelir Vergisi Modu",
                        options = GelirVergisiMode.entries,
                        selected = data.gelirVergisiMode,
                        labelOf = { it.turkishLabel() },
                        onSelected = { mode -> viewModel.update { it.copy(gelirVergisiMode = mode) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    when (data.gelirVergisiMode) {
                        GelirVergisiMode.MANUAL -> TrNumberField(
                            "Gelir Vergisi", data.gelirVergisiManual,
                            { v -> viewModel.update { it.copy(gelirVergisiManual = v) } }, Modifier.fillMaxWidth(),
                        )
                        GelirVergisiMode.FIXED -> TrNumberField(
                            "Sabit Oran (%)", data.gelirVergisiFixedPct,
                            { v -> viewModel.update { it.copy(gelirVergisiFixedPct = v) } }, Modifier.fillMaxWidth(),
                        )
                        GelirVergisiMode.OTO -> Text(
                            "Tahmini (${result.vergiDilimBilgi}): ${TrNumber.format(result.gelirVergisi)} ₺ " +
                                "- doğrulanmamış tahmindir, gerçek bordronuzdan kontrol edin.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text("Efektif Oran: %${TrNumber.format(result.efektifOranPct)}", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider(Modifier.padding(vertical = 6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Damga Vergisi Elle Gir", modifier = Modifier.weight(1f))
                        Switch(
                            checked = data.damgaVergisiManual != null,
                            onCheckedChange = { on ->
                                viewModel.update { it.copy(damgaVergisiManual = if (on) result.damgaVergisi else null) }
                            },
                        )
                    }
                    if (data.damgaVergisiManual != null) {
                        TrNumberField(
                            "Damga Vergisi", data.damgaVergisiManual,
                            { v -> viewModel.update { it.copy(damgaVergisiManual = v) } }, Modifier.fillMaxWidth(),
                        )
                    } else {
                        TrNumberField(
                            "Damga Vergisi Oranı (‰)", data.damgaVergisiOranBinde,
                            { v -> viewModel.update { it.copy(damgaVergisiOranBinde = v) } }, Modifier.fillMaxWidth(),
                        )
                        Text("Damga Vergisi (OTO): ${TrNumber.format(result.damgaVergisi)} ₺")
                    }

                    HorizontalDivider(Modifier.padding(vertical = 6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("İşsizlik Sigortası Muaf (Gazi/Terörle Mücadele)", modifier = Modifier.weight(1f))
                        Switch(
                            checked = data.issizlikSigortasiMuaf,
                            onCheckedChange = { v -> viewModel.update { it.copy(issizlikSigortasiMuaf = v) } },
                        )
                    }

                    TrNumberField(
                        "Mahsup Farkı / İade (bilgi amaçlı, toplama dahil değil)", data.mahsupFarki,
                        { v -> viewModel.update { it.copy(mahsupFarki = v) } }, Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                SectionCard(title = "Notlar") {
                    OutlinedTextField(
                        value = data.notlar,
                        onValueChange = { v -> viewModel.update { it.copy(notlar = v) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            }

            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

private fun GelirVergisiMode.turkishLabel() = when (this) {
    GelirVergisiMode.OTO -> "Otomatik (Tahmini Kademeli)"
    GelirVergisiMode.FIXED -> "Sabit Oran"
    GelirVergisiMode.MANUAL -> "Manuel Giriş"
}

private fun EarningRule.turkishLabel() = when (this) {
    EarningRule.BASE -> "Saat x (Ücret+Zam)"
    EarningRule.GST10 -> "Saat x Ücret x %10"
    EarningRule.VARDIYA10 -> "Saat x (Ücret+Zam) x %10"
    EarningRule.GECE15 -> "Saat x (Ücret+Zam) x %15"
    EarningRule.MESAI200 -> "Saat x (Ücret+Zam) x %200"
    EarningRule.GMS24 -> "Saat x (Ücret+Zam) x %24"
    EarningRule.RATE -> "Saat/Gün/Yıl x Katsayı"
    EarningRule.MANUAL -> "Elle Tutar Gir"
}

@Composable
private fun NetOdemeCard(gelirToplami: Double, kesintiToplami: Double, netOdeme: Double) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gelir Toplamı")
                Text("${TrNumber.format(gelirToplami)} ₺")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kesinti Toplamı")
                Text("${TrNumber.format(kesintiToplami)} ₺")
            }
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net Ödeme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${TrNumber.format(netOdeme)} ₺", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun EarningRow(
    item: EarningItem,
    saatUcr: Double,
    emkZam: Double,
    onChange: (EarningItem) -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onChange(item.copy(name = it)) },
                    label = { Text("Ad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Sil") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrNumberField(
                    "Saat/Gün/Yıl", item.hours, { onChange(item.copy(hours = it)) }, Modifier.weight(1f),
                    enabled = item.rule != EarningRule.MANUAL,
                )
                if (item.rule == EarningRule.RATE || item.rule == EarningRule.MANUAL) {
                    TrNumberField(
                        if (item.rule == EarningRule.MANUAL) "Tutar" else "Katsayı",
                        item.rate, { onChange(item.copy(rate = it)) }, Modifier.weight(1f),
                    )
                }
            }
            EnumDropdown(
                label = "Hesap Kuralı",
                options = EarningRule.entries,
                selected = item.rule,
                labelOf = { it.turkishLabel() },
                onSelected = { onChange(item.copy(rule = it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = item.role == EarningRole.IASE_GUNU,
                    onCheckedChange = { checked ->
                        onChange(item.copy(role = if (checked) EarningRole.IASE_GUNU else EarningRole.NORMAL))
                    },
                )
                Text("İaşe Günü satırı (SSK muafiyeti için gün sayısı)", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Tutar: ${TrNumber.format(item.amount(saatUcr, emkZam))} ₺",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DeductionRow(
    item: DeductionItem,
    onChange: (DeductionItem) -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onChange(item.copy(name = it)) },
                    label = { Text("Ad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Sil") }
            }
            TrNumberField("Tutar", item.amount, { onChange(item.copy(amount = it)) }, Modifier.fillMaxWidth())
            EnumDropdown(
                label = "Tür",
                options = DeductionKind.entries,
                selected = item.kind,
                labelOf = { it.turkishLabel() },
                onSelected = { onChange(item.copy(kind = it)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = item.reducesTaxBase,
                    onCheckedChange = { onChange(item.copy(reducesTaxBase = it)) },
                )
                Text("Gelir vergisi matrahını azaltır", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun DeductionKind.turkishLabel() = when (this) {
    DeductionKind.INCOME_ADD -> "Gelire Ekle (+GELİR)"
    DeductionKind.DEDUCT_ABS -> "Kesinti (Mutlak)"
    DeductionKind.DEDUCT_SIGNED -> "Kesinti (İşaretli/İade)"
    DeductionKind.EXEMPT_ONLY -> "Sadece Matrah İstisnası"
}
