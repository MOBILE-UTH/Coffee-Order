package com.coffee.order.domain

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coffee.order.domain.model.HistoryOrder
import com.coffee.order.domain.model.MenuItem
import com.coffee.order.domain.model.TableInfo
import com.coffee.order.network.TokenManager
import com.coffee.order.repository.CoffeeOrderRepository
import com.coffee.shared.dto.DashboardStatsDto
import com.coffee.shared.dto.OrderItemDto
import com.coffee.shared.dto.UserDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    data class AppUiState(
        val tableInfoList: List<TableInfo> = emptyList(),
        val historyOrders: List<HistoryOrder> = emptyList(),
        val menuItems: List<MenuItem> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val staffList: List<UserDto> = emptyList(),
        val dashboardStats: DashboardStatsDto? = null
    )

    private val coffeeOrderRepository = CoffeeOrderRepository()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    val isOwner: Boolean get() = TokenManager.isOwner()

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchTables(force = true)
        fetchMenuItems(force = true)
        fetchHistoryOrders(force = true)
        if (isOwner) {
            fetchStaff(force = true)
            fetchStats(force = true)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val startTime = System.currentTimeMillis()
            try {
                block()
            } finally {
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                if (duration < 1000) {
                    delay(1000 - duration)
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ─── Tables ──────────────────────────────────────────────────────

    fun fetchTables(force: Boolean = false) {
        if (!force && _uiState.value.tableInfoList.isNotEmpty()) return

        launchWithLoading {
            coffeeOrderRepository.getAllTable().onSuccess { tables ->
                _uiState.update { state ->
                    state.copy(tableInfoList = tables.map { dto ->
                        TableInfo(
                            tableId = dto.id,
                            tableName = dto.name,
                            maxPeople = dto.maxPeople,
                            orderItems = dto.activeOrder?.items?.map { item ->
                                TableInfo.OrderItem(
                                    item.menuItemId,
                                    item.quantity
                                )
                            } ?: emptyList())
                    })
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun addTableInfo(tableInfo: TableInfo) {
        launchWithLoading {
            coffeeOrderRepository.addTable(tableInfo.tableName, tableInfo.maxPeople).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        tableInfoList = state.tableInfoList + TableInfo(
                            tableId = it.id,
                            tableName = it.name,
                            maxPeople = it.maxPeople,
                            orderItems = emptyList()
                        )
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun updateTableInfo(tableInfo: TableInfo) {
        // Update local state immediately for responsive UI
        _uiState.update { state ->
            val currentList = state.tableInfoList.toMutableList()
            val index = currentList.indexOfFirst { it.tableId == tableInfo.tableId }
            if (index != -1) {
                currentList[index] = tableInfo
                state.copy(tableInfoList = currentList)
            } else {
                state
            }
        }
    }

    // ─── Menu Items ──────────────────────────────────────────────────

    fun fetchMenuItems(force: Boolean = false) {
        if (!force && _uiState.value.menuItems.isNotEmpty()) return

        launchWithLoading {
            coffeeOrderRepository.getAll().onSuccess { items ->
                _uiState.update { state ->
                    state.copy(menuItems = items.map { dto ->
                        MenuItem(
                            menuItemId = dto.id,
                            name = dto.name,
                            category = dto.category,
                            price = dto.price,
                            imageUrl = "${TokenManager.getServerUrl()}/${dto.imagePath}"
                        )
                    })
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun addMenuItem(menuItem: MenuItem) {
        launchWithLoading {
            coffeeOrderRepository.addMenuItem(menuItem.name, menuItem.category, menuItem.price)
                .onSuccess {
                    fetchMenuItems(force = true)
                }.onFailure {
                    _uiState.update { it.copy(errorMessage = it.errorMessage) }
                }
        }
    }

    /**
     * Thêm món mới và upload ảnh (nếu có) sau khi tạo thành công.
     */
    fun addMenuItemWithImage(name: String, category: String, price: Int, imageUri: Uri?) {
        launchWithLoading {
            coffeeOrderRepository.addMenuItem(name, category, price).onSuccess { dto ->
                // Nếu có ảnh được chọn, upload ngay sau khi tạo món
                _uiState.update { state ->
                    state.copy(
                        menuItems = state.menuItems + MenuItem(
                            menuItemId = dto.id,
                            name = dto.name,
                            category = dto.category,
                            price = dto.price,
                            imageUrl = null
                        )
                    )
                }
                if (imageUri != null) {
                    try {
                        val context = getApplication<Application>().applicationContext
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        val imageBytes = inputStream?.readBytes()
                        val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                        val ext = mimeType.substringAfterLast('/', "jpg")
                        if (imageBytes != null) {
                            coffeeOrderRepository.uploadImage(
                                menuItemId = dto.id,
                                imageBytes = imageBytes,
                                mimeType = mimeType,
                                fileName = "menu_${dto.id}.$ext"
                            ).onFailure {
                                _uiState.update { it.copy(errorMessage = "Tải ảnh thất bại: ${it.errorMessage}") }
                            }.onSuccess { uploadDto ->
                                _uiState.update { state ->
                                    state.copy(menuItems = state.menuItems.map { item ->
                                        if (item.menuItemId == dto.id) {
                                            item.copy(imageUrl = "${TokenManager.getServerUrl()}/${uploadDto.imagePath}")
                                        } else {
                                            item
                                        }
                                    })
                                }
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Lỗi đọc ảnh: ${e.message}") }
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun updateMenuItem(menuItem: MenuItem) {
        launchWithLoading {
            coffeeOrderRepository.update(
                id = menuItem.menuItemId,
                name = menuItem.name,
                category = menuItem.category,
                price = menuItem.price
            ).onSuccess {
                _uiState.update { state ->
                    val currentList = state.menuItems.toMutableList()
                    val index = currentList.indexOfFirst { it.menuItemId == menuItem.menuItemId }
                    if (index != -1) {
                        currentList[index] = menuItem
                        state.copy(menuItems = currentList)
                    } else {
                        state
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun deleteMenuItem(menuItemId: Long) {
        launchWithLoading {
            coffeeOrderRepository.deleteMenuItem(menuItemId).onSuccess {
                _uiState.update { state ->
                    state.copy(menuItems = state.menuItems.filterNot { it.menuItemId == menuItemId })
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    // ─── Orders ──────────────────────────────────────────────────────

    fun fetchHistoryOrders(force: Boolean = false) {
        if (!force && _uiState.value.historyOrders.isNotEmpty()) return

        launchWithLoading {
            coffeeOrderRepository.getAll(isPaid = true).onSuccess { orders ->
                _uiState.update { state ->
                    state.copy(historyOrders = orders.map { dto ->
                        HistoryOrder(
                            orderId = dto.id,
                            staffName = dto.staffName,
                            tableId = dto.tableId,
                            menuItems = dto.items.map { item ->
                                MenuItem(
                                    menuItemId = item.menuItemId,
                                    name = item.menuItemName,
                                    category = "",
                                    price = item.unitPrice
                                )
                            },
                            totalPrice = dto.totalPrice,
                            orderTime = dto.createdAt
                        )
                    })
                }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun submitOrder(tableId: Long, items: Map<Long, Int>, onSuccess: () -> Unit) {
        launchWithLoading {
            val orderItems = items.map { (menuItemId, quantity) ->
                OrderItemDto(menuItemId, quantity)
            }
            coffeeOrderRepository.createOrUpdateOrder(tableId, orderItems).onSuccess {
                fetchTables(force = true)
                onSuccess()
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun clearTable(tableId: Long) {
        launchWithLoading {
            _uiState.update { state ->
                state.copy(tableInfoList = state.tableInfoList.map { tableInfo ->
                    if (tableInfo.tableId == tableId) {
                        tableInfo.copy(orderItems = emptyList())
                    } else {
                        tableInfo
                    }
                })
            }
        }
    }
    fun payOrder(tableId: Long, onSuccess: () -> Unit) {
        launchWithLoading {
            // First find the active order for this table
            coffeeOrderRepository.getAll(isPaid = false).onSuccess { orders ->
                val order = orders.find { it.tableId == tableId }
                if (order != null) {
                    coffeeOrderRepository.pay(order.id).onSuccess {
                        fetchTables(force = true)
                        fetchHistoryOrders(force = true)
                        onSuccess()
                    }
                        .onFailure { _uiState.update { state -> state.copy(errorMessage = it.message) } }
                } else {
                    _uiState.update { it.copy(errorMessage = "Không tìm thấy đơn hàng cho bàn này") }
                }
            }.onFailure { _uiState.update { state -> state.copy(errorMessage = it.message) } }
        }
    }

    // ── Staff management (OWNER only) ─────────────────────────────────────────

    fun fetchStaff(force: Boolean = false) {
        if (!force && _uiState.value.staffList.isNotEmpty()) return

        launchWithLoading {
            coffeeOrderRepository.getEmployees().onSuccess { staff ->
                _uiState.update { it.copy(staffList = staff) }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun createEmployee(
        username: String, password: String, displayName: String, onSuccess: () -> Unit
    ) {
        launchWithLoading {
            coffeeOrderRepository.createEmployee(username, password, displayName).onSuccess {
                fetchStaff(force = true)
                onSuccess()
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun deleteEmployee(id: Long) {
        launchWithLoading {
            coffeeOrderRepository.deleteEmployee(id).onSuccess {
                fetchStaff(force = true)
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }

    fun fetchStats(force: Boolean = false) {
        if (!isOwner) return
        if (!force && _uiState.value.dashboardStats != null) return

        launchWithLoading {
            coffeeOrderRepository.getDashboardStats().onSuccess { stats ->
                _uiState.update { it.copy(dashboardStats = stats) }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = it.errorMessage) }
            }
        }
    }
}
