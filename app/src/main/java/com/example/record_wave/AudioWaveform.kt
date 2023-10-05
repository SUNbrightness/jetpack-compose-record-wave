package com.example.record_wave

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.record_wave.ui.theme.Record_WaveTheme
import kotlin.random.Random

@Composable
fun AudioWaveform(
    waveformPoints: List<Short>,
    backgroundColor: Color,
    waveformColor: Color,
    modifier: Modifier,
    ) {


    //找到最大值，代表了幅度最高
    val maxValue: Float = waveformPoints?.maxOrNull()?.toFloat() ?: 1.0f // 默认为1，以避免除零错误

    var showMaxPoint = waveformPoints.size


    Box(
        modifier = modifier
            .background(backgroundColor)
            .fillMaxSize()
            .padding(0.1.dp),
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize(),
            onDraw = {
                // 绘制背景
                drawRect(color = backgroundColor)

                // 绘制波形
                val strokeWidth = 2f
                val height = size.height
                val width = size.width



                //y轴中间位置
                val middleY = height / 2

                //每个point占几个像素
                val perPoint = width / showMaxPoint

                val path = Path()


                for (i in 0 until showMaxPoint) {

                    val x = i * perPoint


                    var y =  waveformPoints[i].toFloat() / maxValue * middleY


                    y = y + middleY

                    path.moveTo(x, middleY)

                    path.lineTo(x, y)
                }


                drawPath(path, color = waveformColor, style = Stroke(width = strokeWidth))
            }
        )


        Text(
            text = "MAX: ${maxValue.toString()}",
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 60.sp,
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
fun AudioWaveFormPreview() {
    Record_WaveTheme {
        var amplitudes by remember { mutableStateOf(mutableListOf<Short>(0, 0)) }

        val newAmplitudes = mutableListOf<Short>()
        newAmplitudes.addAll(generateRandomShortArray(40000, 500))
        amplitudes = newAmplitudes

        AudioWaveform(
            waveformPoints = amplitudes,
            backgroundColor = Color.Gray,
            waveformColor = Color.Green,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

fun generateRandomShortArray(size: Int, maxValue: Short): ArrayList<Short> {
    val random = Random(System.currentTimeMillis())
    val array = ArrayList<Short>(size)

    for (i in 0 until size) {
        val randomValue = random.nextInt(-maxValue.toInt(), maxValue.toInt() + 1).toShort()
        array.add(randomValue)
    }

    return array
}