package com.example.record_wave.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object SnackbarUtil{

    private lateinit var _snackbarHostState: SnackbarHostState;
    private lateinit var  _snackScope: CoroutineScope;

    fun getSnackbarHostState(): SnackbarHostState {
        return _snackbarHostState
    }

    @Composable
    fun init() {
        _snackbarHostState = remember { SnackbarHostState() }
        _snackScope = rememberCoroutineScope()
    }



    fun show(message: kotlin.String, duration: SnackbarDuration = SnackbarDuration.Short){
        _snackScope.launch {
            _snackbarHostState
                .showSnackbar(message=message,duration=duration)
        }
    }

}


