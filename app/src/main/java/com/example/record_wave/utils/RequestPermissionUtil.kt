package com.example.record_wave.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.dawidraszka.composepermissionhandler.core.ExperimentalPermissionHandlerApi
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHost
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHostState
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionHandlerApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RequestPermissionUtil(permissionList: List<String>,content: @Composable () -> Unit) {
    val allGranted = remember { mutableStateOf(false) }
    val dialogState = DialogManager()

    val permissionHandlerHostState = PermissionHandlerHostState(
        permissionList = permissionList
    )
    PermissionHandlerHost(hostState = permissionHandlerHostState)

    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
        when (permissionHandlerHostState.handlePermissions()) {
            PermissionHandlerResult.GRANTED -> {
                allGranted.value = true
            }

            PermissionHandlerResult.DENIED -> {
                allGranted.value = false
                dialogState.showErrorDialog("权限授予失败")
            }

            PermissionHandlerResult.DENIED_NEXT_RATIONALE -> { /* Permissions were denied, but
                there will be one more try with rationale. Usually, there's no need to do anything here. */
            }
        }
    }
    //如果权限都授予了,显示否则警告并停止
    if (allGranted.value) {
        content()
    } else {
        dialogState.GlobalDialogWrapper()
    }
}