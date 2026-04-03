package com.coffee.order.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.coffee.order.base.GlobalComposeHandler

@Composable
fun EditMenuItemDialog(
    initialName: String,
    initialCategory: String,
    initialPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, price: Double) -> Unit,
) {
    // Khởi tạo state với giá trị ban đầu của món cần sửa
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var priceText by remember {
        mutableStateOf(
            initialPrice.toInt().toString()
        )
    } // Giả định giá là số nguyên cho dễ nhập
    var priceError by remember { mutableStateOf(false) }

    val parsedPrice = priceText.toDoubleOrNull()
    val isValidPrice = parsedPrice != null && parsedPrice > 0
    val canSubmit = name.isNotBlank() && category.isNotBlank() && isValidPrice

    // Sử dụng Dialog để bao quanh Card giúp nó hiển thị đúng dạng popup
    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        Button(
            onClick = {
                val safePrice = parsedPrice ?: return@Button
                onConfirm(name.trim(), category.trim(), safePrice)
            }, enabled = canSubmit
        ) {
            Text("Xác nhận")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Huỷ")
        }
    }, title = {
        Text(
            text = "Chỉnh sửa món", style = MaterialTheme.typography.titleLarge
        )
    }, text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên món") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = category,
                onValueChange = { category = it },
                label = { Text("Loại món") },
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
                        Text(text = "Giá phải là số dương")
                    }
                })
        }
    })
}

@Composable
fun ConfirmDeleteMenuItemDialog(
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Xác nhận xóa") },
        text = { Text("Bạn có chắc chắn muốn xóa món này không?") },
        confirmButton = {
            TextButton(onClick = {
                onConfirmDelete()
            }
            ) {
                Text("Xóa")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                GlobalComposeHandler.hideGlobalDialog()
            }) {
                Text("Hủy")
            }
        }
    )
}