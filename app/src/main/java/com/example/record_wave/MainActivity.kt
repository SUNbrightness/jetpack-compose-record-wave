package com.example.record_wave

import HeadphoneReceiver
import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.record_wave.ui.theme.Record_WaveTheme
import com.example.record_wave.utils.AudioRecordUtil
import com.example.record_wave.utils.FileUtil
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

            FileUtil.init(this)

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
private var isSaving = mutableStateOf(false)


var amplitudes = mutableStateListOf<Short>(0)

@Composable
fun MainPage(isHeadphonePlugged: Boolean, modifier: Modifier = Modifier) {
    var recordingStartTime = remember { mutableStateOf(0L) }
    var timeText = remember { mutableStateOf("00:00") }
    var audioWaveColor = remember { mutableStateOf(Color.Gray) }

    //监听耳机插入
    LaunchedEffect(isHeadphonePlugged) {
        // 计算录制时间,
        if (isHeadphonePlugged) {

            //清空振幅轴
            amplitudes.clear()
            //清空上次的缓存
            audioRecordUtil.cleanBuffer()

            //开始监听
            audioRecordUtil.startListen(bufferSecond = 5)
        }
        while (isHeadphonePlugged) {
            // 获取最近的数据
            val data = audioRecordUtil.getBuffer()
            amplitudes.clear()
            amplitudes.addAll(data)
            delay(100)
        }

    }

    LaunchedEffect(isSaving.value) {

        if (isSaving.value){
            // 每次重新录制等于现在
            recordingStartTime.value = System.currentTimeMillis()
            //变颜色
            audioWaveColor.value =Color(0xFF5aa4ae)
        }else{
            //变颜色
            audioWaveColor.value =Color.Gray
        }

        while (isSaving.value){
            val recordingTime = System.currentTimeMillis() - recordingStartTime.value
            val formattedTime = formatRecordingTime(recordingTime)
            // 更新录制时间文本
            timeText.value = formattedTime

            delay(500)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        key(amplitudes.hashCode().toLong()) {
            AudioWaveform(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(7f),
                backgroundColor =audioWaveColor.value,
                waveformColor = Color.Green,
                waveformPoints = amplitudes,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSaving.value) {
                Button(
                    modifier = Modifier,
                    onClick = {
                        isSaving.value = false
                        var savePath =  audioRecordUtil.stopSaving()
                        SnackbarUtil.show("保存成功:${savePath}")
                    }) {
                    Text(text = "结束录制")
                }
            } else {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHeadphonePlugged) MaterialTheme.colorScheme.primary else Color.Gray
                    ),
                    enabled = isHeadphonePlugged,
                    onClick = {
                        isSaving.value = true

                        var currentTime = System.currentTimeMillis()
                        currentTime += (8 * 60 * 60 * 1000).toLong()
                        val savePath =
                            "${FileUtil.bikeSavePath}/${formatRecordingTime(currentTime)}.pcm";
                        //开始保存文件
                        audioRecordUtil.startSaving(saveFile= savePath)
                    }) {
                    Text(text = "开始录制")
                }
            }

            // 显示录制时间文本
            Text(
                text = timeText.value, // 调整间距
            )
        }
    }
}

private fun formatRecordingTime(timeMillis: Long): String {
    val seconds = (timeMillis / 1000) % 60
    val minutes = (timeMillis / (1000 * 60)) % 60
    val hours = (timeMillis / (1000 * 60 * 60)) % 24
    return String.format("%02d_%02d_%02d", hours, minutes, seconds)
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
fun GreetingPreview() {
    Record_WaveTheme {
        MainPage(false)
    }
}


