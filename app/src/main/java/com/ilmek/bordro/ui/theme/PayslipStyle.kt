package com.ilmek.bordro.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colors lifted straight from code.html's Tailwind palette (slate/emerald/sky/amber). */
object Bordro {
    val PaperBg = Color(0xFFF1F5F9)      // bg-slate-100 (outer page)
    val DocBg = Color(0xFFFFFFFF)        // white payslip surface
    val BorderStrong = Color(0xFF334155) // border-slate-700 (dashed outer/section rules)
    val BorderSoft = Color(0xFFCBD5E1)   // border-slate-300 (input boxes)
    val DividerDotted = Color(0xFF94A3B8) // border-slate-400 (dotted sub-dividers)
    val LabelText = Color(0xFF475569)    // text-slate-600
    val ValueText = Color(0xFF0F172A)    // text-slate-900/950
    val FieldBg = Color(0xFFF8FAFC)      // bg-slate-50/80
    val Emerald = Color(0xFF047857)
    val EmeraldBg = Color(0xFFECFDF5)
    val EmeraldBorder = Color(0xFF6EE7B7)
    val Sky = Color(0xFF0369A1)
    val SkyBg = Color(0xFFF0F9FF)
    val Amber = Color(0xFF92400E)
    val AmberBg = Color(0xFFFFFBEB)
    val Red = Color(0xFFB91C1C)
    val RedBg = Color(0xFFFEF2F2)

    val Mono = FontFamily.Monospace
    val labelSize = 12.sp
    val valueSize = 12.sp
}

fun Modifier.dashedOutline(color: Color = Bordro.BorderStrong, strokeWidth: Dp = 1.dp) = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f),
    )
    drawRect(color = color, style = stroke)
}

fun Modifier.dashedBottom(color: Color = Bordro.BorderStrong, strokeWidth: Dp = 1.5.dp) = this.drawBehind {
    val y = size.height - strokeWidth.toPx() / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f),
    )
}

fun Modifier.dottedBottom(color: Color = Bordro.DividerDotted, strokeWidth: Dp = 1.dp) = this.drawBehind {
    val y = size.height - strokeWidth.toPx() / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 3f), 0f),
    )
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = Bordro.Mono,
        fontSize = 11.sp,
        color = Bordro.LabelText,
        modifier = Modifier.padding(bottom = 4.dp).dottedBottom(),
    )
}

val KeyboardDecimal = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
val RowGap = Arrangement.spacedBy(2.dp)
