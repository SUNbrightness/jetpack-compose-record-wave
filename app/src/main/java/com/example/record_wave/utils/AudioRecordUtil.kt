package com.example.record_wave.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.experimental.and

class AudioRecordUtil {
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private var isPlaying = false

    private var playbackEnabled = false


    private val buffer = ArrayList<Short>()


    // 获取最近时间的录音
    fun getRecentData(second: Int): List<Short> {
        //单声道 16bit一秒刚好是 sampleRate 个short
        var dataLen = second * this.sampleRate

        //不够5秒直接返回
        val bufferSize = buffer.size
        if (dataLen > bufferSize) {
            return buffer.toList()
        }

        synchronized(buffer) {
            val toList = buffer.subList(bufferSize - dataLen, bufferSize).toList()
            return toList
        }


    }


    @SuppressLint("MissingPermission")
    fun startRecording(playbackEnabled: Boolean = false, callback: ((ShortArray) -> Unit)? = null) {
        if (isRecording) {
            stopRecording()
        } else {
            this.playbackEnabled = playbackEnabled

            audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()

            isRecording = true

            Thread {
                val data = ShortArray(bufferSize)

                while (isRecording) {
                    audioRecord?.read(data, 0, bufferSize)
                    if (playbackEnabled && isPlaying) {
                        audioTrack?.write(data, 0, bufferSize)
                    }

                    synchronized(buffer) {
                        buffer.addAll(data.toList())
                    }


                    callback?.invoke(data)
                }

                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null

                isRecording = false
            }.start()
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    fun togglePlayback(playbackEnabled: Boolean = false) {
        if (playbackEnabled && !isPlaying) {
            if (audioTrack == null) {
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack?.play()

            isPlaying = true
        } else if (isPlaying && !playbackEnabled) {
            stopPlayback()
        }
    }

    private fun stopPlayback() {
        audioTrack?.apply {
            stop()
            flush()
            isPlaying = false
        }
    }

    fun cleanBuffer() {

        synchronized(buffer) {
            buffer.clear()
        }
    }

    fun saveToFile(fileName: String) {
        val data = buffer.toShortArray()

        val file = File(fileName)
        val parentDir = file.parentFile
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        try {
            val fileOutputStream = FileOutputStream(file)
            val byteBuffer = ByteArray(data.size * 2) // 16-bit PCM 需要2个字节表示一个 short
            for (i in data.indices) {
                val shortValue = data[i]
                byteBuffer[i * 2] = (shortValue and 0xFF).toByte() // 低位字节
                byteBuffer[i * 2 + 1] = (shortValue.toInt() shr 8 and 0xFF).toByte() // 高位字节
            }
            fileOutputStream.write(byteBuffer)
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}