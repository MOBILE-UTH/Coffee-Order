package com.coffee.order.feature.admin.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coffee.order.R
import com.coffee.order.viewmodel.AppViewModel
import com.coffee.order.feature.admin.components.CreateEmployeeBottomSheet
import com.coffee.shared.dto.UserDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffScreen(viewModel: AppViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val staffList = uiState.staffList
    val isShowCreateEmployeeBottomSheet = remember { mutableStateOf(false) }

    val selectedStaffForDelete = remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchStaff()
    }

    val sheetState = rememberModalBottomSheetState()

    // Create Employee Bottom Sheet
    if (isShowCreateEmployeeBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { isShowCreateEmployeeBottomSheet.value = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CreateEmployeeBottomSheet(
                onDismiss = { isShowCreateEmployeeBottomSheet.value = false },
                onConfirm = { username: String, password: String, displayName: String ->
                    viewModel.createEmployee(username, password, displayName) {
                        isShowCreateEmployeeBottomSheet.value = false
                    }
                }
            )
        }
    }

    // Delete Confirmation Dialog
    selectedStaffForDelete.value?.let { staff ->
        AlertDialog(
            onDismissRequest = { selectedStaffForDelete.value = null },
            title = { Text(stringResource(R.string.x_a_t_i_kho_n), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    stringResource(
                        R.string.b_n_c_mu_n_x_a_nh_n_vi_n_kh_ng,
                        staff.displayName,
                        staff.username
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEmployee(staff.id)
                    selectedStaffForDelete.value = null
                }) {
                    Text(
                        "Xóa",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStaffForDelete.value = null }) {
                    Text("Hủy", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (staffList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chưa có nhân viên nào",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(staffList) { staff ->
                    StaffListItem(
                        staff = staff, onDelete = { selectedStaffForDelete.value = staff })
                }
            }
        }

        FloatingActionButton(
            onClick = { isShowCreateEmployeeBottomSheet.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.Add, contentDescription = stringResource(R.string.th_m_nh_n_vi_n)
            )
        }

    }
}

@Composable
fun StaffListItem(staff: UserDto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    staff.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "@${staff.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
