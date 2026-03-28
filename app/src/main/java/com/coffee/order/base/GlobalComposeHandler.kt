package com.coffee.order.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog

object GlobalComposeHandler {
    data class BottomSheetState(
        val showBottomSheet: Boolean = false,
        val bottomSheetContent: (@Composable () -> Unit)? = null,
    )

    data class DialogState(
        val showDialog: Boolean = false,
        val dialogContent: (@Composable () -> Unit)? = null,
    )

    private val dialogState: MutableState<DialogState> = mutableStateOf(DialogState())
    private val bottomSheetState: MutableState<BottomSheetState> =
        mutableStateOf(BottomSheetState())

    fun showGlobalDialog(content: @Composable () -> Unit) {
        dialogState.value = DialogState(
            showDialog = true, dialogContent = content
        )
    }

    fun hideGlobalDialog() {
        dialogState.value = DialogState(showDialog = false, dialogContent = null)
    }

    fun showGlobalBottomSheet(content: @Composable () -> Unit) {
        bottomSheetState.value = BottomSheetState(
            showBottomSheet = true, bottomSheetContent = content
        )
    }

    fun hideGlobalBottomSheet() {
        bottomSheetState.value =
            BottomSheetState(showBottomSheet = false, bottomSheetContent = null)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GlobalComposeContent() {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            if (dialogState.value.showDialog) {
                Dialog(onDismissRequest = { hideGlobalDialog() }) {
                    dialogState.value.dialogContent?.invoke()
                }
            }

            // BottomSheet
            if (bottomSheetState.value.showBottomSheet) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { hideGlobalBottomSheet() }, sheetState = sheetState
                ) {
                    bottomSheetState.value.bottomSheetContent?.invoke()
                }
            }
        }
    }
}