package com.ilmek.bordro.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilmek.bordro.data.GelirVergisiMode
import com.ilmek.bordro.data.TrNumber
import com.ilmek.bordro.ui.components.DocNumberField
import com.ilmek.bordro.ui.components.DocTextField
import com.ilmek.bordro.ui.components.DeductionLine
import com.ilmek.bordro.ui.components.EarningLine
import com.ilmek.bordro.ui.components.LabelRow
import com.ilmek.bordro.ui.components.ReadOnlyValue
import com.ilmek.bordro.ui.theme.Bordro
import com.ilmek.bordro.ui.theme.SectionLabel
import com.ilmek.bordro.ui.theme.dashedBottom
import com.ilmek.bordro.ui.theme.dottedBottom

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
        containerColor = Bordro.DocBg,
        topBar = {
            TopAppBar(
                title = { Text(data.periodLabel, fontFamily = Bordro.Mono, fontSize = 14.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Geri") } },
                actions = { IconButton(onClick = viewModel::save) { Icon(Icons.Filled.Save, contentDescription = "Kaydet") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bordro.DocBg),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 14.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).dashedBottom()) {
                    Text(
                        "TCDD TAŞIMACILIK A.Ş. İŞÇİ AYLIĞI MAAŞ BORDROSU",
                        fontFamily = Bordro.Mono, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Bordro.ValueText,
                    )
                    Spacer(Modifier.height(2.dp))
                    LabelRow("Dönem") { DocTextField(data.periodLabel, { v -> viewModel.update { it.copy(periodLabel = v) } }, width = 190.dp) }
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).dashedBottom()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gelir Toplamı", fontFamily = Bordro.Mono, fontSize = 12.sp, color = Bordro.LabelText)
                        ReadOnlyValue("${TrNumber.format(result.totalEarnings)} ₺")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kesinti Toplamı", fontFamily = Bordro.Mono, fontSize = 12.sp, color = Bordro.LabelText)
                        ReadOnlyValue("${TrNumber.format(result.totalDeductions)} ₺")
                    }
                    Row(
                        Modifier.fillMaxWidth().background(Bordro.EmeraldBg).padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("NET ÖDEME", fontFamily = Bordro.Mono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Bordro.Emerald)
                        ReadOnlyValue("${TrNumber.format(result.netOdeme)} ₺", bold = true, color = Bordro.Emerald)
                    }
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    SectionLabel("ÖZLÜK / PERSONEL BİLGİSİ")
                    LabelRow("Adı") { DocTextField(data.adi, { v -> viewModel.update { it.copy(adi = v) } }, width = 150.dp) }
                    LabelRow("Soyadı") { DocTextField(data.soyadi, { v -> viewModel.update { it.copy(soyadi = v) } }, width = 150.dp) }
                    LabelRow("Ünvanı") { DocTextField(data.unvani, { v -> viewModel.update { it.copy(unvani = v) } }, width = 150.dp) }
                    LabelRow("Sicil No") { DocTextField(data.sicilNo, { v -> viewModel.update { it.copy(sicilNo = v) } }, width = 150.dp) }
                    LabelRow("Pers.No") { DocTextField(data.persNo, { v -> viewModel.update { it.copy(persNo = v) } }, width = 150.dp) }
                    LabelRow("Der/Kad.") { DocTextField(data.derKad, { v -> viewModel.update { it.copy(derKad = v) } }, width = 150.dp) }
                    LabelRow("Kıdem Yılı") { DocNumberField(data.kidemYili, { v -> viewModel.update { it.copy(kidemYili = v) } }) }
                    LabelRow("Hzm.Zammı Yıl") { DocNumberField(data.hizmetZammiYili, { v -> viewModel.update { it.copy(hizmetZammiYili = v) } }) }
                    LabelRow("Saat Ücr", badge = "BAZ") { DocNumberField(data.saatUcr, { v -> viewModel.update { it.copy(saatUcr = v) } }) }
                    LabelRow("Emk. Zam") { DocNumberField(data.emkZam, { v -> viewModel.update { it.copy(emkZam = v) } }) }
                    RaiseControl(onApply = viewModel::applyRaise)
                    LabelRow("Çalıştığı Gün") { DocNumberField(data.calistigiGun, { v -> viewModel.update { it.copy(calistigiGun = v) } }) }
                    LabelRow("SSK Günü") { DocNumberField(data.sskGunu, { v -> viewModel.update { it.copy(sskGunu = v) } }) }
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    SectionLabel("HAKEDİŞLER")
                }
            }
            items(data.earnings, key = { it.id }) { item ->
                EarningLine(
                    item = item, saatUcr = data.saatUcr, emkZam = data.emkZam,
                    onChange = { updated -> viewModel.updateEarning(item.id) { updated } },
                    onDelete = { viewModel.removeEarning(item.id) },
                )
            }
            item {
                TextButton(onClick = viewModel::addEarning) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.width(16.dp))
                    Text("Hakediş Kalemi Ekle", fontFamily = Bordro.Mono, fontSize = 11.sp)
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    SectionLabel("ÖZEL KESİNTİLER VE EK GELİRLER")
                }
            }
            items(data.deductions, key = { it.id }) { item ->
                DeductionLine(
                    item = item,
                    onChange = { updated -> viewModel.updateDeduction(item.id) { updated } },
                    onDelete = { viewModel.removeDeduction(item.id) },
                )
            }
            item {
                TextButton(onClick = viewModel::addDeduction) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.width(16.dp))
                    Text("Kesinti / Gelir Kalemi Ekle", fontFamily = Bordro.Mono, fontSize = 11.sp)
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    SectionLabel("YASAL KESİNTİ VE SONUÇLAR")

                    LabelRow("SSK Matrahı", badge = if (data.sskMatrahiManual == null) "OTO" else "ELLE") {
                        if (data.sskMatrahiManual != null) {
                            DocNumberField(data.sskMatrahiManual, { v -> viewModel.update { it.copy(sskMatrahiManual = v) } }, width = 96.dp)
                        } else {
                            ReadOnlyValue(TrNumber.format(result.sskMatrahi))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Text("elle gir", fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.LabelText)
                        Switch(
                            checked = data.sskMatrahiManual != null,
                            onCheckedChange = { on -> viewModel.update { it.copy(sskMatrahiManual = if (on) result.sskMatrahi else null) } },
                            modifier = Modifier.padding(start = 4.dp),
                            colors = SwitchDefaults.colors(checkedTrackColor = Bordro.Emerald),
                        )
                    }

                    LabelRow("SSK Prim İşçi") { ReadOnlyValue(TrNumber.format(result.sskPrimIsci)) }
                    LabelRow("SSK Prim(İşv)") { ReadOnlyValue(TrNumber.format(result.sskPrimIsvereni)) }
                    LabelRow("Yıllık Glr.VM (Önceki)", badge = "KÜM") {
                        DocNumberField(data.yillikGlrVMOnceki, { v -> viewModel.update { it.copy(yillikGlrVMOnceki = v) } }, width = 96.dp)
                    }
                    LabelRow("Aylık Glr.VM") { ReadOnlyValue(TrNumber.format(result.aylikGlrVM)) }

                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        LabelRow("Gelir Vergisi") {
                            when (data.gelirVergisiMode) {
                                GelirVergisiMode.MANUAL -> DocNumberField(
                                    data.gelirVergisiManual, { v -> viewModel.update { it.copy(gelirVergisiManual = v) } }, width = 96.dp,
                                )
                                GelirVergisiMode.FIXED -> DocNumberField(
                                    data.gelirVergisiFixedPct, { v -> viewModel.update { it.copy(gelirVergisiFixedPct = v) } }, width = 60.dp,
                                )
                                GelirVergisiMode.OTO -> ReadOnlyValue(TrNumber.format(result.gelirVergisi))
                            }
                        }
                        EnumDropdown(
                            label = "Vergi Modu",
                            options = GelirVergisiMode.entries,
                            selected = data.gelirVergisiMode,
                            labelOf = { it.turkishLabel() },
                            onSelected = { mode -> viewModel.update { it.copy(gelirVergisiMode = mode) } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (data.gelirVergisiMode == GelirVergisiMode.OTO) {
                            LabelRow("Asgari Ücret Aylık Matrahı", badge = "2026") {
                                DocNumberField(
                                    data.asgariUcretAylikMatrah,
                                    { v -> viewModel.update { it.copy(asgariUcretAylikMatrah = v) } },
                                    width = 96.dp,
                                )
                            }
                            Text(
                                "${result.vergiDilimBilgi} - 2026 GİB ücret tarifesi ve asgari ücret istisnası ile hesaplandı.",
                                fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.Sky,
                            )
                        }
                        Text("Efektif Oran: %${TrNumber.format(result.efektifOranPct)}", fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.LabelText)
                    }

                    LabelRow("Damga Vergisi", badge = if (data.damgaVergisiManual == null) "OTO" else "ELLE") {
                        if (data.damgaVergisiManual != null) {
                            DocNumberField(data.damgaVergisiManual, { v -> viewModel.update { it.copy(damgaVergisiManual = v) } }, width = 96.dp)
                        } else {
                            ReadOnlyValue(TrNumber.format(result.damgaVergisi))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Text("elle gir", fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.LabelText)
                        Switch(
                            checked = data.damgaVergisiManual != null,
                            onCheckedChange = { on -> viewModel.update { it.copy(damgaVergisiManual = if (on) result.damgaVergisi else null) } },
                            modifier = Modifier.padding(start = 4.dp),
                            colors = SwitchDefaults.colors(checkedTrackColor = Bordro.Emerald),
                        )
                    }

                    LabelRow("İşs.Sig. Muaf", badge = "MUAF") {
                        Switch(
                            checked = data.issizlikSigortasiMuaf,
                            onCheckedChange = { v -> viewModel.update { it.copy(issizlikSigortasiMuaf = v) } },
                            colors = SwitchDefaults.colors(checkedTrackColor = Bordro.Emerald),
                        )
                    }
                    LabelRow("Mahsup Fark", badge = "İADE") {
                        DocNumberField(data.mahsupFarki, { v -> viewModel.update { it.copy(mahsupFarki = v) } }, width = 96.dp)
                    }
                    Text(
                        "Mahsup Farkı bilgi amaçlıdır, hiçbir toplama dahil edilmez.",
                        fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.LabelText,
                        modifier = Modifier.padding(bottom = 4.dp).dottedBottom(),
                    )
                }
            }

            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    SectionLabel("NOTLAR")
                    DocTextField(data.notlar, { v -> viewModel.update { it.copy(notlar = v) } }, modifier = Modifier.fillMaxWidth())
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

private fun GelirVergisiMode.turkishLabel() = when (this) {
    GelirVergisiMode.OTO -> "Otomatik (Tahmini Kademeli)"
    GelirVergisiMode.FIXED -> "Sabit Oran"
    GelirVergisiMode.MANUAL -> "Manuel Giriş"
}

/**
 * A one-shot "apply a raise" action: typing a percent and tapping Uygula scales
 * Saat Ücr, Emk. Zam, and every fixed-rate hakediş kalemi (İaşe Günü, Hizmet
 * Zammı, ...) by that percentage. Rows derived from Saat Ücr/Emk. Zam (Normal
 * Çalış, GŞT %10, Vardiya Prim, ...) update automatically since their amount is
 * computed from those two fields - no separate scaling needed for them.
 */
@Composable
private fun RaiseControl(onApply: (percent: Double, daysAtOldRate: Double, daysAtNewRate: Double) -> Unit) {
    var percentText by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf(false) }
    var oldDaysText by remember { mutableStateOf("") }
    var newDaysText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        LabelRow("Zam Uygula (%)") {
            DocTextField(percentText, { percentText = it }, width = 70.dp, numeric = true)
            TextButton(onClick = {
                val pct = percentText.replace(',', '.').toDoubleOrNull() ?: return@TextButton
                if (partial) {
                    val oldDays = oldDaysText.replace(',', '.').toDoubleOrNull() ?: return@TextButton
                    val newDays = newDaysText.replace(',', '.').toDoubleOrNull() ?: return@TextButton
                    onApply(pct, oldDays, newDays)
                } else {
                    onApply(pct, 0.0, 1.0)
                }
                percentText = ""
            }) {
                Text("Uygula", fontFamily = Bordro.Mono, fontSize = 11.sp)
            }
        }
        Text(
            "Saat Ücr, Emk. Zam, sabit katsayılı hakediş kalemlerine (İaşe Günü, Hizmet Zammı vb.) ve " +
                "\"Zam Uygula ile artar\" işaretli kesinti/yardım kalemlerine (Birleştirilmiş, Sendika Aidatı) uygulanır.",
            fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.LabelText,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Checkbox(checked = partial, onCheckedChange = { partial = it })
            Text(
                "İlk ay: gün bazlı kısmi uygula (dönem, zam tarihine bölünüyorsa)",
                fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.LabelText,
            )
        }
        if (partial) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 2.dp)) {
                LabelRow("Eski Oran Gün") { DocTextField(oldDaysText, { oldDaysText = it }, width = 56.dp, align = TextAlign.End, numeric = true) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabelRow("Yeni Oran Gün") { DocTextField(newDaysText, { newDaysText = it }, width = 56.dp, align = TextAlign.End, numeric = true) }
            }
            Text(
                "Örn. 15 Ağustos-14 Eylül döneminde 1 Eylül'den geçerli zam için: Eski Oran Gün=17, Yeni Oran Gün=14.",
                fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.Sky,
            )
        }
    }
}
