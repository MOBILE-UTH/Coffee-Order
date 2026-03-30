package com.coffee.order.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.coffee.order.viewmodel.model.HistoryOrder
import com.coffee.order.viewmodel.model.MenuItem
import com.coffee.order.viewmodel.model.TableInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList


class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val _tableInfoList = MutableStateFlow<List<TableInfo>>(emptyList())
    val tableInfoList: StateFlow<List<TableInfo>> = _tableInfoList

    private val _historyOrders = MutableStateFlow<List<HistoryOrder>>(emptyList())
    val historyOrders: StateFlow<List<HistoryOrder>> = _historyOrders

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    init {
        _tableInfoList.value = listOf(
            TableInfo(1, "Bàn 1", TableInfo.Status.EMPTY),
            TableInfo(2, "Bàn 2", TableInfo.Status.OCCUPIED),
            TableInfo(3, "Bàn 3", TableInfo.Status.WAITING_FOR_PAYMENT),
        )
        _menuItems.value = listOf(
            MenuItem(1, "Cà phê sữa đá", "Coffee", 30000.0),
            MenuItem(2, "Cà phê đen đá", "Coffee", 25000.0),
            MenuItem(3, "Trà sữa trân châu", "Coffee", 35000.0),
        )
        _historyOrders.value = listOf(
            HistoryOrder(
                orderId = 1,
                staffName = "Nhân viên A",
                tableId = 2,
                menuItems = listOf(_menuItems.value[0], _menuItems.value[1]),
                totalPrice = 55000.0,
                orderTime = "2024-06-01 10:00:00"
            ),
            HistoryOrder(
                orderId = 2,
                staffName = "Nhân viên B",
                tableId = 3,
                menuItems = listOf(_menuItems.value[2]),
                totalPrice = 35000.0,
                orderTime = "2024-06-01 11:00:00"
            ),
        )
    }

    fun addTableInfo(tableInfo: TableInfo) {
        val currentList = _tableInfoList.value.toMutableList()
        currentList.add(tableInfo)
        _tableInfoList.value = currentList
    }

    fun updateTableInfo(tableInfo: TableInfo) {
        val currentList = _tableInfoList.value.toMutableList()
        val index = currentList.indexOfFirst { it.tableId == tableInfo.tableId }
        if (index != -1) {
            currentList[index] = tableInfo
            _tableInfoList.value = currentList
        }
    }

    fun addHistoryOrder(historyOrder: HistoryOrder) {
        val currentList = _historyOrders.value.toMutableList()
        currentList.add(historyOrder)
        _historyOrders.value = currentList
    }

    fun addMenuItem(menuItem: MenuItem) {
        val currentList = _menuItems.value.toMutableList()
        currentList.add(menuItem)
        _menuItems.value = currentList
    }
}

