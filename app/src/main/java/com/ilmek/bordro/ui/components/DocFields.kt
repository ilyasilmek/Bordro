package com.ilmek.bordro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilmek.bordro.ui.theme.Bordro
import com.ilmek.bordro.ui.theme.KeyboardDecimal

/** A small bordered input box matching code.html's `bg-slate-50/80 border rounded` inputs. */
@Composable
fun DocTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    align: TextAlign = TextAlign.Start,
    bold: Boolean = false,
    enabled: Boolean = true,
    numeric: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = Bordro.Mono,
            fontSize = Bordro.valueSize,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            textAlign = align,
            color = Bordro.ValueText,
        ),
        keyboardOptions = if (numeric) KeyboardDecimal else androidx.compose.foundation.text.KeyboardOptions.Default,
        modifier = (if (width != null) modifier.width(width) else modifier)
            .background(if (enabled) Bordro.FieldBg else Bordro.DocBg, RoundedCornerShape(3.dp))
            .border(1.dp, Bordro.BorderSoft, RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
    )
}

/** Same box, but bound to a Double via plain-decimal text (dot separator while editing). */
@Composable
fun DocNumberField(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 76.dp,
    bold: Boolean = false,
    enabled: Boolean = true,
) {
    // A plain remember (not rememberSaveable keyed on value) plus a LaunchedEffect that
    // re-syncs whenever the upstream value changes - e.g. via applyRaise's batch update to
    // a whole list of items - regardless of how LazyColumn happens to key/recycle this row.
    // rememberSaveable(value) alone left this stale after such batch updates in testing.
    var text by remember { mutableStateOf(if (value == 0.0) "" else plain(value)) }
    LaunchedEffect(value) {
        val currentAsDouble = text.replace(',', '.').toDoubleOrNull() ?: 0.0
        if (currentAsDouble != value) {
            text = if (value == 0.0) "" else plain(value)
        }
    }
    DocTextField(
        value = text,
        onValueChange = { new ->
            text = new
            new.replace(',', '.').toDoubleOrNull()?.let(onValueChange) ?: if (new.isBlank()) onValueChange(0.0) else Unit
        },
        modifier = modifier,
        width = width,
        align = TextAlign.End,
        bold = bold,
        enabled = enabled,
        numeric = true,
    )
}

private fun plain(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
