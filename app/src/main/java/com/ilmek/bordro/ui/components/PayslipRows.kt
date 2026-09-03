package com.ilmek.bordro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilmek.bordro.data.DeductionItem
import com.ilmek.bordro.data.DeductionKind
import com.ilmek.bordro.data.EarningItem
import com.ilmek.bordro.data.EarningRole
import com.ilmek.bordro.data.EarningRule
import com.ilmek.bordro.data.TrNumber
import com.ilmek.bordro.ui.edit.EnumDropdown
import com.ilmek.bordro.ui.theme.Bordro
import com.ilmek.bordro.ui.theme.dottedBottom

/** "Label : value-box" row, matching code.html's `<label>…</label><span>:</span><input>` pattern. */
@Composable
fun LabelRow(label: String, badge: String? = null, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = Bordro.Mono, fontSize = Bordro.labelSize, color = Bordro.LabelText)
            if (badge != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    badge, fontFamily = Bordro.Mono, fontSize = 9.sp, color = Bordro.Sky,
                    modifier = Modifier
                        .background(Bordro.SkyBg, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                )
            }
        }
        Text(" : ", fontFamily = Bordro.Mono, fontSize = Bordro.labelSize, color = Bordro.LabelText)
        content()
    }
}

@Composable
fun ReadOnlyValue(text: String, bold: Boolean = false, color: androidx.compose.ui.graphics.Color = Bordro.ValueText) {
    Text(
        text,
        fontFamily = Bordro.Mono,
        fontSize = Bordro.valueSize,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
    )
}

private fun EarningRule.shortLabel() = when (this) {
    EarningRule.BASE -> "Saat x (Ücret+Zam)"
    EarningRule.GST10 -> "Saat x Ücret x %10"
    EarningRule.VARDIYA10 -> "Saat x (Ücret+Zam) x %10"
    EarningRule.GECE15 -> "Saat x (Ücret+Zam) x %15"
    EarningRule.MESAI200 -> "Saat x (Ücret+Zam) x %200"
    EarningRule.GMS24 -> "Saat x (Ücret+Zam) x %24"
    EarningRule.RATE -> "Saat/Gün/Yıl x Katsayı"
    EarningRule.MANUAL -> "Elle Tutar Gir"
}

private fun DeductionKind.shortLabel() = when (this) {
    DeductionKind.INCOME_ADD -> "Gelire Ekle (+GELİR)"
    DeductionKind.DEDUCT_ABS -> "Kesinti (Mutlak)"
    DeductionKind.DEDUCT_SIGNED -> "Kesinti (İşaretli/İade)"
    DeductionKind.EXEMPT_ONLY -> "Sadece Matrah İstisnası"
}

/** One "HAKEDİŞ" line: name, saat, tutar - matching the reference's 3-cell row. Advanced
 * settings (calc rule, rate, İaşe Günü flag) are tucked behind the gear icon so the default
 * view stays as plain as the printed payslip. */
@Composable
fun EarningLine(
    item: EarningItem,
    saatUcr: Double,
    emkZam: Double,
    onChange: (EarningItem) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().dottedBottom().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            DocTextField(
                value = item.name,
                onValueChange = { onChange(item.copy(name = it)) },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(6.dp))
            DocNumberField(
                item.hours, { onChange(item.copy(hours = it)) },
                width = 56.dp, enabled = item.rule != EarningRule.MANUAL,
            )
            Spacer(Modifier.width(6.dp))
            ReadOnlyValue(TrNumber.format(item.amount(saatUcr, emkZam)), bold = true)
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.width(32.dp)) {
                Icon(
                    if (expanded) Icons.Filled.Close else Icons.Filled.Settings,
                    contentDescription = "Ayarlar", tint = Bordro.LabelText,
                    modifier = Modifier.width(16.dp),
                )
            }
        }
        if (expanded) {
            Column(Modifier.padding(top = 4.dp, start = 2.dp)) {
                EnumDropdown(
                    label = "Hesap Kuralı",
                    options = EarningRule.entries,
                    selected = item.rule,
                    labelOf = { it.shortLabel() },
                    onSelected = { onChange(item.copy(rule = it)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (item.rule == EarningRule.RATE || item.rule == EarningRule.MANUAL) {
                    LabelRow(if (item.rule == EarningRule.MANUAL) "Tutar" else "Katsayı") {
                        DocNumberField(item.rate, { onChange(item.copy(rate = it)) }, width = 96.dp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.role == EarningRole.IASE_GUNU,
                        onCheckedChange = { c -> onChange(item.copy(role = if (c) EarningRole.IASE_GUNU else EarningRole.NORMAL)) },
                    )
                    Text("İaşe Günü satırı (SSK muafiyeti)", fontFamily = Bordro.Mono, fontSize = 10.sp, color = Bordro.LabelText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Sil", tint = Bordro.Red) }
                    Text("Kalemi sil", fontFamily = Bordro.Mono, fontSize = 10.sp, color = Bordro.Red)
                }
            }
        }
    }
}

/** One "ÖZEL KESİNTİ / GELİR" line: name + tutar, advanced kind/tax-base flag behind the gear. */
@Composable
fun DeductionLine(
    item: DeductionItem,
    onChange: (DeductionItem) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val badge = when (item.kind) {
        DeductionKind.INCOME_ADD -> "+GELİR"
        DeductionKind.EXEMPT_ONLY -> "İSTİSNA"
        else -> null
    }
    Column(Modifier.fillMaxWidth().dottedBottom().padding(vertical = 4.dp)) {
        LabelRow(label = item.name.ifBlank { "(isimsiz)" }, badge = badge) {
            DocNumberField(item.amount, { onChange(item.copy(amount = it)) }, width = 90.dp, bold = true)
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.width(30.dp)) {
                Icon(
                    if (expanded) Icons.Filled.Close else Icons.Filled.Settings,
                    contentDescription = "Ayarlar", tint = Bordro.LabelText, modifier = Modifier.width(16.dp),
                )
            }
        }
        if (expanded) {
            Column(Modifier.padding(top = 4.dp, start = 2.dp)) {
                DocTextField(item.name, { onChange(item.copy(name = it)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.width(4.dp))
                EnumDropdown(
                    label = "Tür",
                    options = DeductionKind.entries,
                    selected = item.kind,
                    labelOf = { it.shortLabel() },
                    onSelected = { onChange(item.copy(kind = it)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.reducesTaxBase, onCheckedChange = { onChange(item.copy(reducesTaxBase = it)) })
                    Text("Gelir vergisi matrahını azaltır", fontFamily = Bordro.Mono, fontSize = 10.sp, color = Bordro.LabelText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.scalesWithRaise, onCheckedChange = { onChange(item.copy(scalesWithRaise = it)) })
                    Text("Zam Uygula ile birlikte artar (TİS'e bağlı yardım/aidat)", fontFamily = Bordro.Mono, fontSize = 10.sp, color = Bordro.LabelText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Sil", tint = Bordro.Red) }
                    Text("Kalemi sil", fontFamily = Bordro.Mono, fontSize = 10.sp, color = Bordro.Red)
                }
            }
        }
    }
}
