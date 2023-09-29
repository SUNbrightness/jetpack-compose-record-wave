package com.example.record_wave.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton

class DialogManager {

    private var showDialog by mutableStateOf(false)
    private var dialog: (@Composable () -> Unit)? = null

    fun showDialog(composable: @Composable () -> Unit) {
        dialog = composable
        showDialog = true
    }

    fun dismiss() {
        showDialog = false
    }

    @Composable
    fun GlobalDialogWrapper() {
        if (showDialog) {
            dialog?.invoke()
        }
    }


    // 成功对话框
    fun showSuccessDialog(
        message: String,
        title: String = "成功",
        iconColor: Color = Color.Green,
        onConfirm: (() -> Unit)? = null
    ) {
        showDialog(DialogComponent(message, title, Icons.Default.Check, iconColor, onConfirm))
    }

    // 警告对话框
    fun showWarningDialog(
        message: String,
        title: String = "警告",
        iconColor: Color = Color.Yellow,
        onConfirm: (() -> Unit)? = null
    ) {
        showDialog(DialogComponent(message, title, Icons.Default.Warning, iconColor, onConfirm))
    }

    // 错误对话框
    fun showErrorDialog(
        message: String,
        title: String = "错误",
        iconColor: Color = Color.Red,
        onConfirm: (() -> Unit)? = null
    ) {
        showDialog(DialogComponent(message, title, Icons.Default.Error, iconColor, onConfirm))
    }

    // 信息对话框
    fun showInfoDialog(
        message: String,
        title: String = "信息",
        iconColor: Color = Color.Blue,
        onConfirm: (() -> Unit)? = null
    ) {
        showDialog(DialogComponent(message, title, Icons.Default.Info, iconColor, onConfirm))
    }

    private fun DialogComponent(
        message: String,
        title: String,
        icon: ImageVector,
        iconColor: Color,
        onConfirm: (() -> Unit)?
    ): @Composable () -> Unit = {
        AlertDialog(
            onDismissRequest = { dismiss() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = iconColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title)
                }
            },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm?.invoke()
                    dismiss()
                }) {
                    Text("确认")
                }
            }
        )
    }
}

