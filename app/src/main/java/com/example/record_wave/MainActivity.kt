package com.example.record_wave

import HeadphoneReceiver
import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.record_wave.ui.theme.Record_WaveTheme
import com.example.record_wave.utils.AudioRecordUtil
import com.example.record_wave.utils.RequestPermissionUtil
import com.example.record_wave.utils.SnackbarUtil
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val headphoneReceiver = HeadphoneReceiver(
        onHeadphonePlugged = {
            isHeadphonePlugged.value = true
        },
        onHeadphoneUnplugged = {
            isHeadphonePlugged.value = false
        }
    )

    private val isHeadphonePlugged = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            //初始化全局 snackbar
            SnackbarUtil.init()

            val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
            registerReceiver(headphoneReceiver, filter)

            //获取权限
            RequestPermissionUtil(
                listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                Record_WaveTheme {

                    Scaffold(
                        snackbarHost = { SnackbarHost(SnackbarUtil.getSnackbarHostState()) },
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier.padding(paddingValues),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainPage(isHeadphonePlugged.value)
                        }
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(headphoneReceiver)
    }
}

var audioRecordUtil = AudioRecordUtil()
private var isRecording = mutableStateOf(false)
private var canSave = mutableStateOf(false)


var amplitudes =  mutableStateListOf<Short>(0)

@Composable
fun MainPage(isHeadphonePlugged: Boolean, modifier: Modifier = Modifier) {


    //开始录制就根据一定频率获取数据
    LaunchedEffect(isRecording.value) {
        while (isRecording.value) {
            val data = audioRecordUtil.getBufferedData() // 获取缓冲的录制数据
            amplitudes.addAll(data)
            delay(500)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        key(amplitudes.hashCode().toLong()) {
            AudioWaveform(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(7f),
                backgroundColor = Color.Gray,
                waveformColor = Color.Green,
                waveformPoints = amplitudes,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (isRecording.value) {
                Button(
                    modifier = Modifier,
                    onClick = {
                        isRecording.value = false
                        audioRecordUtil.stopRecording()
                    }) {
                    Text(text = "结束录制")
                }
            } else {
                Button(
                    modifier = Modifier,
                    onClick = {
                        isRecording.value = true
                        audioRecordUtil.startRecording()
                    }) {
                    Text(text = "开始录制")
                }
            }

            Button(
                modifier = Modifier.clickable(enabled = canSave.value) {},
                onClick = { /*TODO*/ }) {
                Text(text = "保存本次录制")
            }
        }
    }
}


@Preview(showBackground = true, device = Devices.TABLET)
@Composable
fun GreetingPreview() {
    Record_WaveTheme {
        MainPage(false)
    }
}


