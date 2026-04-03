package com.coffee.order.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coffee.order.R

@Composable
fun AddMenuItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, price: Double) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf(false) }

    val parsedPrice = priceText.toDoubleOrNull()
    val isValidPrice = parsedPrice != null && parsedPrice > 0
    val canSubmit = name.isNotBlank() && category.isNotBlank() && isValidPrice

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thêm Món Mới",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = {
                    Text("Tên món")
                },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = category,
                onValueChange = { category = it },
                label = { Text("Danh mục") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = priceText,
                onValueChange = {
                    priceText = it
                    priceError = it.isNotBlank() && it.toDoubleOrNull() == null
                },
                label = { Text("Giá") },
                singleLine = true,
                isError = priceError || (priceText.isNotBlank() && !isValidPrice),
                supportingText = {
                    if (priceError || (priceText.isNotBlank() && !isValidPrice)) {
                        Text(
                            text = "Vui lòng nhập giá hợp lệ (số dương)",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Huỷ")
                }
                Button(
                    onClick = {
                        val safePrice = parsedPrice ?: return@Button
                        onConfirm(name.trim(), category.trim(), safePrice)
                    },
                    enabled = canSubmit
                ) {
                    Text("Thêm")
                }
            }
        }
    }
}

