package com.coffee.order.feature.admin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditMenuItemBottomSheet(
    initialName: String,
    initialCategory: String,
    initialPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, price: Double) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var priceText by remember { mutableStateOf(initialPrice.toInt().toString()) }
    var priceError by remember { mutableStateOf(false) }

    val parsedPrice = priceText.toDoubleOrNull()
    val isValidPrice = parsedPrice != null && parsedPrice > 0
    val canSubmit = name.isNotBlank() && category.isNotBlank() && isValidPrice

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Chỉnh Sửa Món",
            style = MaterialTheme.typography.titleLarge
        )

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            errorTextColor = Color.Red
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên món") },
            singleLine = true,
            colors = textFieldColors
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = category,
            onValueChange = { category = it },
            label = { Text("Danh mục") },
            singleLine = true,
            colors = textFieldColors
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
            colors = textFieldColors,
            isError = priceError || (priceText.isNotBlank() && !isValidPrice),
            supportingText = {
                if (priceError || (priceText.isNotBlank() && !isValidPrice)) {
                    Text(
                        text = "Vui lòng nhập giá hợp lệ",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Huỷ")
            }
            Spacer(modifier = Modifier.size(12.dp))
            Button(
                onClick = {
                    val safePrice = parsedPrice ?: return@Button
                    onConfirm(name.trim(), category.trim(), safePrice)
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cập Nhật", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

