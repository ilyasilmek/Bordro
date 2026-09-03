package com.ilmek.bordro.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ilmek.bordro.data.TrNumber

/**
 * A numeric field edited as a plain decimal string (dot separator, easiest for
 * an on-screen numeric keyboard) but displayed/committed as a Double; results
 * elsewhere in the app are shown Turkish-formatted via [TrNumber.format].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrNumberField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var text by rememberSaveable(value) { mutableStateOf(if (value == 0.0) "" else formatPlain(value)) }
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            text = new
            new.replace(',', '.').toDoubleOrNull()?.let(onValueChange) ?: run {
                if (new.isBlank()) onValueChange(0.0)
            }
        },
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

private fun formatPlain(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

@Composable
fun ReadOnlyAmount(label: String, value: Double, emphasize: Boolean = false) {
    Text(
        text = "$label: ${TrNumber.format(value)} ₺",
        style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
