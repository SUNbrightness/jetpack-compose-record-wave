package com.example.record_wave.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import cn.hutool.core.util.ByteUtil
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList


class AudioRecordUtil {
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    //监听中
    private var isListening = false

    //播放中
    private var isPlaying = false

    //录制保存中
    private val saveLock = Any()
    private var saveName = ""
    private var fileOutputStream: BufferedOutputStream? = null


    private var playbackEnabled = false


    private val buffer = LinkedList<Short>()


    // 获取最近时间的录音
    fun getBuffer(): List<Short> {
        synchronized(this.buffer) {
            return this.buffer.toList()
        }
    }


    @SuppressLint("MissingPermission")
    fun startListen(
        playbackEnabled: Boolean = false,
        bufferSecond: Int = 6,
        callback: ((ShortArray) -> Unit)? = null
    ) {
        if (isListening) {
            return
        }

        this.playbackEnabled = playbackEnabled

        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()

        isListening = true

        Thread {
            val data = ShortArray(bufferSize)

            while (isListening) {
                audioRecord?.read(data, 0, bufferSize)
                if (playbackEnabled && isPlaying) {
                    audioTrack?.write(data, 0, bufferSize)
                }

                //添加进缓存
                this.addBuffer(data, bufferSecond = bufferSecond)

                //判读是否保存成文件
                this.fileOutputStream?.let {
                    writeToFile(data)
                }
                callback?.invoke(data)
            }

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            isListening = false
        }.start()

    }


    private fun addBuffer(bufferData: ShortArray, bufferSecond: Int = 6) {
        //单声道 16bit一秒刚好是 sampleRate 个short
        var dataLen = bufferSecond * this.sampleRate

        synchronized(buffer) {
            //添加进缓存
            buffer.addAll(bufferData.toList())

            //如果此时超过了指定时长的缓存则删除早期的
            while (buffer.size > dataLen) {
                buffer.removeFirst()
            }
        }
    }

    fun stopListen() {
        isListening = false
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


    //将数据写入文件中
    private fun writeToFile(bufferData: ShortArray) {
        synchronized(this.saveLock) {
            val byteBuffer = ByteArray(bufferData.size * 2) // 16-bit PCM 需要2个字节表示一个 short
            for (i in bufferData.indices) {
                val shortValue = bufferData[i]
                val shortToBytes = ByteUtil.shortToBytes(shortValue)
                byteBuffer[i * 2] =  shortToBytes[0]
                byteBuffer[i * 2 + 1] = shortToBytes[1]
            }
            fileOutputStream?.write(byteBuffer)

        }
    }


    fun stopSaving(): String {

        synchronized(this.saveLock) {
            //停止录制
            fileOutputStream?.close()
            fileOutputStream = null
        }

        return this.saveName
    }

    fun startSaving(saveFile: String) {
        val file = File(saveFile)
        val parentDir = file.parentFile
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        this.saveName = saveFile

        synchronized(this.saveLock) {
            //创建输入流允许追加
            this.fileOutputStream = BufferedOutputStream(FileOutputStream(saveFile, true))
        }
    }


}